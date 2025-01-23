package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.AllocateStack;
import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.Call;
import com.plasstech.lang.c.codegen.Cdq;
import com.plasstech.lang.c.codegen.Cmp;
import com.plasstech.lang.c.codegen.CondCode;
import com.plasstech.lang.c.codegen.DeallocateStack;
import com.plasstech.lang.c.codegen.Idiv;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Jmp;
import com.plasstech.lang.c.codegen.JmpCC;
import com.plasstech.lang.c.codegen.Label;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.Push;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;
import com.plasstech.lang.c.lex.TokenType;

/**
 * This is part of the "assembly generation" step, used by the top-level TackyToAsmCodeGen.
 * <p>
 * Input: TackyInstruction
 * <p>
 * Output: List<Instruction>
 */
class TackyInstructionToInstructionsVisitor implements TackyInstruction.Visitor<List<Instruction>> {
  private static final Imm ZERO = new Imm(0);

  private static Operand toOperand(TackyVal val) {
    return switch (val) {
      case TackyVar v -> new Pseudo(v.identifier());
      case TackyConstant ic -> new Imm(ic.val());
      default -> throw new IllegalArgumentException("Unexpected value: " + val);
    };
  }

  @Override
  public List<Instruction> visit(TackyUnary op) {
    List<Instruction> instructions = new ArrayList<>();
    Operand src = toOperand(op.src());
    Operand dst = toOperand(op.dst());
    if (op.operator() == TokenType.BANG) {
      // Page 86
      instructions.add(new Cmp(ZERO, src));
      instructions.add(new Mov(ZERO, dst));
      instructions.add(new SetCC(CondCode.E, dst));
    } else {
      instructions.add(new Mov(src, dst));
      instructions.add(new AsmUnary(op.operator(), dst));
    }
    return instructions;
  }

  @Override
  public List<Instruction> visit(TackyBinary op) {
    List<Instruction> instructions = new ArrayList<>();
    Operand src1 = toOperand(op.src1());
    Operand src2 = toOperand(op.src2());
    Operand dst = toOperand(op.dst());
    TokenType operator = op.operator();
    switch (operator) {
      case SLASH:
      case PERCENT:
        // mov (src1, register(ax))
        instructions.add(new Mov(src1, RegisterOperand.RAX));
        // cdq
        instructions.add(new Cdq());
        // idiv(src2)
        instructions.add(new Idiv(src2));
        if (operator == TokenType.SLASH) {
          // mov(reg(ax), dst)
          instructions.add(new Mov(RegisterOperand.RAX, dst));
        } else {
          // mov(reg(dx), dst)  for modulo
          instructions.add(new Mov(RegisterOperand.RDX, dst));
        }
        break;

      case EQEQ:
      case GT:
      case GEQ:
      case LT:
      case LEQ:
      case NEQ:
        // Page 86
        instructions.add(new Cmp(src2, src1));
        instructions.add(new Mov(ZERO, dst));
        instructions.add(new SetCC(CondCode.from(operator), dst));
        break;

      case PLUS:
      case MINUS:
      case STAR:
        // For +, -, *: 
        // First move src1 to dest
        instructions.add(new Mov(src1, dst));
        // Then use dest and src2 with the operator
        instructions.add(new AsmBinary(op.operator(), src2, dst));
        break;

      default:
        throw new IllegalStateException("Cannot generate code for " + operator.name());
    }
    return instructions;
  }

  @Override
  public List<Instruction> visit(TackyReturn tackyReturn) {
    Operand operand = toOperand(tackyReturn.val());
    return ImmutableList.of(
        new Mov(operand, RegisterOperand.RAX),
        new Ret());
  }

  @Override
  public List<Instruction> visit(TackyCopy op) {
    Operand src = toOperand(op.src());
    Operand dst = toOperand(op.dst());
    return ImmutableList.of(new Mov(src, dst));
  }

  @Override
  public List<Instruction> visit(TackyJump op) {
    return ImmutableList.of(new Jmp(op.target()));
  }

  @Override
  public List<Instruction> visit(TackyJumpZero op) {
    // Page 86
    Operand operand = toOperand(op.condition());
    return ImmutableList.of(
        new Cmp(ZERO, operand),
        new JmpCC(CondCode.E, op.target()));
  }

  @Override
  public List<Instruction> visit(TackyJumpNotZero op) {
    // Page 86
    Operand operand = toOperand(op.condition());
    return ImmutableList.of(
        new Cmp(ZERO, operand),
        new JmpCC(CondCode.NE, op.target()));
  }

  @Override
  public List<Instruction> visit(TackyLabel op) {
    return ImmutableList.of(new Label(op.target()));
  }

  @Override
  public List<Instruction> visit(TackyFunCall op) {
    // Page 197ff
    List<Instruction> instructions = new ArrayList<>();

    // Adjust stack alignment
    int numArgs = op.args().size();
    int numRegArgs = Math.min(6, numArgs);
    assert numRegArgs >= 0;
    int numStackArgs = numArgs - numRegArgs;
    assert numStackArgs >= 0;
    int stackPadding = 8 * (numStackArgs % 2); // MATH THAT SH*T
    if (stackPadding != 0) {
      instructions.add(new AllocateStack(stackPadding));
    }

    // Pass args in registers
    for (int i = 0; i < numRegArgs; ++i) {
      RegisterOperand register = RegisterOperand.ARG_REGISTERS.get(i);
      TackyVal arg = op.args().get(i);
      Operand argOp = toOperand(arg);
      instructions.add(new Mov(argOp, register));
    }

    // Pass args on stack
    for (int i = numStackArgs - 1; i >= 0; --i) {
      TackyVal arg = op.args().get(i + 6);
      Operand argOp = toOperand(arg);
      switch (argOp) {
        case RegisterOperand ro -> instructions.add(new Push(argOp));
        case Imm imm -> instructions.add(new Push(argOp));
        default -> {
          instructions.add(new Mov(argOp, RegisterOperand.RAX));
          instructions.add(new Push(RegisterOperand.RAX));
        }
      }
    }
    // Emit call instruction
    instructions.add(new Call(op.funName()));

    // Adjust stack pointer
    int bytesToRemove = 8 * numStackArgs + stackPadding;
    if (bytesToRemove > 0) {
      instructions.add(new DeallocateStack(bytesToRemove));
    }

    // retrieve return value
    Operand dest = toOperand(op.dst());
    instructions.add(new Mov(RegisterOperand.RAX, dest));

    return instructions;
  }

  @Override
  public List<Instruction> visit(TackySignExtend op) {
    throw new UnsupportedOperationException("Cannot geneate tackysignextend");
  }

  @Override
  public List<Instruction> visit(TackyTruncate op) {
    throw new UnsupportedOperationException("Cannot geneate tackytruncate");
  }
}