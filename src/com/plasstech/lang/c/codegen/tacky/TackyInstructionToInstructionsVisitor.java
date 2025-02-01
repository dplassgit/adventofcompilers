package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.AssemblyType;
import com.plasstech.lang.c.codegen.Call;
import com.plasstech.lang.c.codegen.Cdq;
import com.plasstech.lang.c.codegen.Cmp;
import com.plasstech.lang.c.codegen.CondCode;
import com.plasstech.lang.c.codegen.Idiv;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Jmp;
import com.plasstech.lang.c.codegen.JmpCC;
import com.plasstech.lang.c.codegen.Label;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Movsx;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.Push;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.typecheck.Symbol;
import com.plasstech.lang.c.typecheck.SymbolTable;
import com.plasstech.lang.c.typecheck.Type;

/**
 * This is part of the "assembly generation" step, used by the top-level TackyToAsmCodeGen.
 * <p>
 * Input: TackyInstruction
 * <p>
 * Output: List<Instruction>
 */
class TackyInstructionToInstructionsVisitor implements TackyInstruction.Visitor<List<Instruction>> {
  private final SymbolTable symbolTable;

  public TackyInstructionToInstructionsVisitor(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  private static final Imm ZERO = new Imm(0);

  private Operand toOperand(TackyVal val) {
    return switch (val) {
      case TackyVar v -> {
        yield new Pseudo(v.identifier(), v.type());
      }
      case TackyConstant ic -> new Imm(ic.val());
      default -> throw new IllegalArgumentException("Unexpected value: " + val);
    };
  }

  private Type getType(TackyVal tv) {
    return switch (tv) {
      case TackyVar var -> {
        Symbol s = symbolTable.get(var.identifier());
        assert (s != null);
        yield s.type();
      }
      case TackyConstant tc -> tc.type();
      default -> throw new IllegalArgumentException("Unexpected value: " + tv);
    };
  }

  private AssemblyType assemblyType(TackyVal tv) {
    return AssemblyType.from(getType(tv));
  }

  @Override
  public List<Instruction> visit(TackyUnary op) {
    List<Instruction> instructions = new ArrayList<>();
    Operand src = toOperand(op.src());
    Operand dst = toOperand(op.dst());
    AssemblyType dstType = assemblyType(op.dst());
    AssemblyType srcType = assemblyType(op.src());
    if (op.operator() == TokenType.BANG) {
      // Page 86, 265
      instructions.add(new Cmp(srcType, ZERO, src));
      instructions.add(new Mov(dstType, ZERO, dst));
      instructions.add(new SetCC(CondCode.E, dst));
    } else {
      instructions.add(new Mov(srcType, src, dst));
      instructions.add(new AsmUnary(op.operator(), srcType, dst));
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
    AssemblyType src1Type = assemblyType(op.src1());
    AssemblyType dstType = assemblyType(op.dst());
    switch (operator) {
      case SLASH:
      case PERCENT:
        // mov (src1, register(ax))
        instructions.add(new Mov(src1Type, src1, RegisterOperand.RAX));
        // cdq
        instructions.add(new Cdq(src1Type));
        // idiv(src2)
        instructions.add(new Idiv(src1Type, src2));
        if (operator == TokenType.SLASH) {
          // mov(reg(ax), dst)
          instructions.add(new Mov(src1Type, RegisterOperand.RAX, dst));
        } else {
          // mov(reg(dx), dst)  for modulo
          instructions.add(new Mov(src1Type, RegisterOperand.RDX, dst));
        }
        break;

      case EQEQ:
      case GT:
      case GEQ:
      case LT:
      case LEQ:
      case NEQ:
        // Page 86
        instructions.add(new Cmp(src1Type, src2, src1));
        instructions.add(new Mov(dstType, ZERO, dst));
        instructions.add(new SetCC(CondCode.from(operator), dst));
        break;

      case PLUS:
      case MINUS:
      case STAR:
        // For +, -, *: 
        // First move src1 to dest
        instructions.add(new Mov(src1Type, src1, dst));
        // Then use dest and src2 with the operator
        // Are these types right?!
        instructions.add(new AsmBinary(op.operator(), src1Type, src2, dst));
        break;

      default:
        throw new IllegalStateException("Cannot generate code for " + operator.name());
    }
    return instructions;
  }

  @Override
  public List<Instruction> visit(TackyReturn op) {
    AssemblyType srcType = assemblyType(op.val());
    Operand operand = toOperand(op.val());
    return ImmutableList.of(
        new Mov(srcType, operand, RegisterOperand.RAX),
        new Ret());
  }

  @Override
  public List<Instruction> visit(TackyCopy op) {
    AssemblyType dstType = assemblyType(op.dst());
    Operand src = toOperand(op.src());
    Operand dst = toOperand(op.dst());
    return ImmutableList.of(new Mov(dstType, src, dst));
  }

  @Override
  public List<Instruction> visit(TackyJump op) {
    return ImmutableList.of(new Jmp(op.target()));
  }

  @Override
  public List<Instruction> visit(TackyJumpZero op) {
    AssemblyType srcType = assemblyType(op.condition());
    // Page 86
    Operand operand = toOperand(op.condition());
    return ImmutableList.of(
        new Cmp(srcType, ZERO, operand),
        new JmpCC(CondCode.E, op.target()));
  }

  @Override
  public List<Instruction> visit(TackyJumpNotZero op) {
    AssemblyType srcType = assemblyType(op.condition());
    // Page 86
    Operand operand = toOperand(op.condition());
    return ImmutableList.of(
        new Cmp(srcType, ZERO, operand),
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
      Instruction allocateStack =
          new AsmBinary(TokenType.MINUS, AssemblyType.Quadword, new Imm(stackPadding),
              RegisterOperand.RSP);
      instructions.add(allocateStack);
    }

    // Pass args in registers
    for (int i = 0; i < numRegArgs; ++i) {
      RegisterOperand register = RegisterOperand.ARG_REGISTERS.get(i);
      TackyVal arg = op.args().get(i);
      AssemblyType srcType = assemblyType(arg);
      Operand argOp = toOperand(arg);
      instructions.add(new Mov(srcType, argOp, register));
    }

    // Pass args on stack
    // Updated with AssemblyType p 263
    for (int i = numStackArgs - 1; i >= 0; --i) {
      TackyVal arg = op.args().get(i + 6);
      AssemblyType srcType = assemblyType(arg);
      Operand argOp = toOperand(arg);
      if (srcType == AssemblyType.Quadword) {
        instructions.add(new Push(argOp));
      } else {
        switch (argOp) {
          case RegisterOperand ro -> instructions.add(new Push(argOp));
          case Imm imm -> instructions.add(new Push(argOp));
          default -> {
            instructions.add(new Mov(AssemblyType.Longword, argOp, RegisterOperand.RAX));
            instructions.add(new Push(RegisterOperand.RAX));
          }
        }
      }
    }
    // Emit call instruction
    instructions.add(new Call(op.funName()));

    // Adjust stack pointer
    int bytesToRemove = 8 * numStackArgs + stackPadding;
    if (bytesToRemove > 0) {
      Instruction deallocateStack =
          new AsmBinary(TokenType.PLUS, AssemblyType.Quadword, new Imm(bytesToRemove),
              RegisterOperand.RSP);
      instructions.add(deallocateStack);
    }

    // retrieve return value
    Operand dest = toOperand(op.dst());
    AssemblyType dstType = assemblyType(op.dst());
    instructions.add(new Mov(dstType, RegisterOperand.RAX, dest));

    return instructions;
  }

  @Override
  public List<Instruction> visit(TackySignExtend op) {
    // page 263
    return ImmutableList.of(new Movsx(toOperand(op.src()), toOperand(op.dst())));
  }

  @Override
  public List<Instruction> visit(TackyTruncate op) {
    // page 263
    return ImmutableList
        .of(new Mov(AssemblyType.Longword, toOperand(op.src()), toOperand(op.dst())));
  }
}