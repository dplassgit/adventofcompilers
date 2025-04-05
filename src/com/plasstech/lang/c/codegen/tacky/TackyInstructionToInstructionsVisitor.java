package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmStaticConstant;
import com.plasstech.lang.c.codegen.AsmTopLevel;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.AssemblyType;
import com.plasstech.lang.c.codegen.Call;
import com.plasstech.lang.c.codegen.Cdq;
import com.plasstech.lang.c.codegen.Cmp;
import com.plasstech.lang.c.codegen.CondCode;
import com.plasstech.lang.c.codegen.Cvtsi2sd;
import com.plasstech.lang.c.codegen.Cvttsd2si;
import com.plasstech.lang.c.codegen.Data;
import com.plasstech.lang.c.codegen.Div;
import com.plasstech.lang.c.codegen.Idiv;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Jmp;
import com.plasstech.lang.c.codegen.JmpCC;
import com.plasstech.lang.c.codegen.Label;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.MovZeroExtend;
import com.plasstech.lang.c.codegen.Movsx;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.Push;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;
import com.plasstech.lang.c.common.UniqueId;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.typecheck.DoubleInit;
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
  private final Map<Double, AsmStaticConstant> doubleConstants = new HashMap<>();

  public TackyInstructionToInstructionsVisitor(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public ImmutableList<AsmTopLevel> doubleGlobals() {
    return ImmutableList.copyOf(doubleConstants.values());
  }

  private static final Imm ZERO = new Imm(0);

  private Operand toOperand(TackyVal val) {
    return switch (val) {
      case TackyVar v -> {
        yield new Pseudo(v.identifier(), v.type());
      }
      case TackyConstant<?> ic -> {
        if (ic.type().equals(Type.DOUBLE)) {
          double constantDouble = ic.val().doubleValue();
          // 1. add or get a new StaticConstant top-level object with the double's value
          AsmStaticConstant staticConstant = getOrMakeDoubleConstant(constantDouble, 8);
          // 2. make a Data object referring to the constant's label & return it
          yield new Data(staticConstant.name());
        }
        yield new Imm(ic.val().longValue());
      }
      default -> throw new IllegalArgumentException("Unexpected value: " + val);
    };
  }

  // Page 326
  private AsmStaticConstant getOrMakeDoubleConstant(double constantDouble, int alignment) {
    AsmStaticConstant staticConstant = doubleConstants.get(constantDouble);
    if (staticConstant == null) {
      // doesn't exist yet; make a new one.
      staticConstant = new AsmStaticConstant(newLabel(), alignment, new DoubleInit(constantDouble));
      doubleConstants.put(constantDouble, staticConstant);
    }
    return staticConstant;
  }

  private int id = 0;

  private String newLabel() {
    return String.format("DOUBLE_%d", id++);
  }

  private Type getType(TackyVal tv) {
    return switch (tv) {
      case TackyVar var -> {
        Symbol s = symbolTable.get(var.identifier());
        assert (s != null);
        yield s.type();
      }
      case TackyConstant<?> tc -> tc.type();
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
    switch (op.operator()) {
      case TokenType.BANG:
        if (dstType == AssemblyType.Double) {
          // page 328 (implied)
          return ImmutableList.of(
              new AsmBinary(TokenType.XOR, srcType, RegisterOperand.XMM0, RegisterOperand.XMM0),
              new Cmp(srcType, RegisterOperand.XMM0, src),
              new Mov(dstType, RegisterOperand.XMM0, dst),
              new SetCC(CondCode.E, dst));
        } else {
          // Page 86, 265
          instructions.add(new Cmp(srcType, ZERO, src));
          instructions.add(new Mov(dstType, ZERO, dst));
          instructions.add(new SetCC(CondCode.E, dst));
        }
        break;

      case TokenType.MINUS:
        if (dstType == AssemblyType.Double) {
          // page 327-328
          // add new constant
          AsmStaticConstant doubleConstant = getOrMakeDoubleConstant(-0.0, 16);
          instructions.add(new Mov(srcType, src, dst));
          instructions
              .add(new AsmBinary(TokenType.XOR, dstType, new Data(doubleConstant.name()), dst));
        } else {
          // negate
          instructions.add(new Mov(srcType, src, dst));
          instructions.add(new AsmUnary(op.operator(), srcType, dst));
        }
        break;

      case TokenType.TWIDDLE:
        // bit not
        instructions.add(new Mov(srcType, src, dst));
        instructions.add(new AsmUnary(op.operator(), srcType, dst));
        break;

      default:
        throw new IllegalStateException("Unknown unary operator " + op.operator());
    }
    return instructions;
  }

  @Override
  public List<Instruction> visit(TackyBinary op) {
    List<Instruction> instructions = new ArrayList<>();

    Operand left = toOperand(op.left());
    Operand right = toOperand(op.right());
    Operand dst = toOperand(op.dst());
    TokenType operator = op.operator();
    AssemblyType leftType = assemblyType(op.left());
    AssemblyType dstType = assemblyType(op.dst());
    switch (operator) {
      case DIVIDE:
        if (op.left().type().equals(Type.DOUBLE)) {
          // Page 327
          instructions.add(new Mov(leftType, left, dst));
          instructions.add(new AsmBinary(TokenType.DIVIDE, leftType, right, dst));
          return instructions;
        }
        // fall through:
      case PERCENT:
        // mov (left, register(ax))
        instructions.add(new Mov(leftType, left, RegisterOperand.RAX));
        if (op.left().type().signed()) {
          // cdq
          instructions.add(new Cdq(leftType));
          // idiv(right)
          instructions.add(new Idiv(leftType, right));
          if (operator == TokenType.DIVIDE) {
            // mov(reg(ax), dst)
            instructions.add(new Mov(leftType, RegisterOperand.RAX, dst));
          } else {
            // mov(reg(dx), dst)  for modulo
            instructions.add(new Mov(leftType, RegisterOperand.RDX, dst));
          }
        } else {
          // page 288
          // mov (left, register(ax)) (above)
          // mov 0, rdx
          instructions.add(new Mov(leftType, new Imm(0), RegisterOperand.RDX));
          // div(right)
          instructions.add(new Div(leftType, right));
          if (operator == TokenType.DIVIDE) {
            // mov(reg(ax), dst)
            instructions.add(new Mov(leftType, RegisterOperand.RAX, dst));
          } else {
            // mov(reg(dx), dst)  for modulo
            instructions.add(new Mov(leftType, RegisterOperand.RDX, dst));
          }
        }
        break;

      case EQEQ:
      case GT:
      case GEQ:
      case LT:
      case LEQ:
      case NEQ:
        // Page 86
        instructions.add(new Cmp(leftType, right, left));
        if (dstType == AssemblyType.Double) {
          // Page 328
          dstType = AssemblyType.Longword;
        }
        instructions.add(new Mov(dstType, ZERO, dst));
        boolean signed = op.left().type().signed();
        instructions.add(new SetCC(CondCode.from(operator, signed), dst));
        break;

      case PLUS:
      case MINUS:
      case MULTIPLY:
        // For +, -, *: 
        // First move left to dest
        instructions.add(new Mov(leftType, left, dst));
        // Then use right and dest with the operator
        instructions.add(new AsmBinary(op.operator(), leftType, right, dst));
        break;

      default:
        throw new IllegalStateException("Cannot generate code for " + operator.name());
    }
    return instructions;
  }

  private static RegisterOperand returnRegister(AssemblyType type) {
    if (type == AssemblyType.Double) {
      return RegisterOperand.XMM0;
    }
    return RegisterOperand.RAX;
  }

  @Override
  public List<Instruction> visit(TackyReturn op) {
    AssemblyType srcType = assemblyType(op.val());
    Operand operand = toOperand(op.val());
    return ImmutableList.of(
        new Mov(srcType, operand, returnRegister(srcType)),
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
    if (srcType == AssemblyType.Double) {
      // Page 328
      return ImmutableList.of(
          new AsmBinary(TokenType.XOR, srcType, RegisterOperand.XMM0, RegisterOperand.XMM0),
          new Cmp(srcType, operand, RegisterOperand.XMM0),
          new JmpCC(CondCode.E, op.target()));
    }
    return ImmutableList.of(
        new Cmp(srcType, ZERO, operand),
        new JmpCC(CondCode.E, op.target()));
  }

  @Override
  public List<Instruction> visit(TackyJumpNotZero op) {
    AssemblyType srcType = assemblyType(op.condition());
    // Page 86
    Operand operand = toOperand(op.condition());
    if (srcType == AssemblyType.Double) {
      // Page 328
      return ImmutableList.of(
          new AsmBinary(TokenType.XOR, srcType, RegisterOperand.XMM0, RegisterOperand.XMM0),
          new Cmp(srcType, operand, RegisterOperand.XMM0),
          new JmpCC(CondCode.NE, op.target()));
    }
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
      TackyVal arg = op.args().get(i);
      RegisterOperand register = RegisterOperand.argRegister(arg.type(), i);
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
      if (srcType == AssemblyType.Double) {
        // TODO: deal with doubles
      } else if (srcType == AssemblyType.Quadword) {
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
    instructions.add(new Mov(dstType, returnRegister(dstType), dest));

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

  @Override
  public List<Instruction> visit(TackyZeroExtend op) {
    // page 288ff
    return ImmutableList.of(new MovZeroExtend(toOperand(op.src()), toOperand(op.dst())));
  }

  @Override
  public List<Instruction> visit(TackyDoubleToInt op) {
    // Page 317, 334
    Operand src = toOperand(op.src());
    Operand dst = toOperand(op.dst());
    AssemblyType srcType = assemblyType(op.src());
    return ImmutableList.of(new Cvttsd2si(srcType, src, dst));
  }

  @Override
  public List<Instruction> visit(TackyDoubleToUInt op) {
    // Page 335
    Operand src = toOperand(op.src());
    Operand dst = toOperand(op.dst());
    Type srcType = op.src().type();
    if (srcType.equals(Type.INT)) {
      // to unsigned int.
      return ImmutableList.of(
          new Cvttsd2si(AssemblyType.Quadword, src, RegisterOperand.XMM1),
          new Mov(AssemblyType.Longword, RegisterOperand.XMM1, dst));
    }
    // To unsigned long
    AsmStaticConstant upperBound = getOrMakeDoubleConstant(9223372036854775808.0, 8);
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new Cmp(AssemblyType.Double, new Data(upperBound.name()), src));
    String outOfRangeLabel = UniqueId.makeUnique("double2uint.outofrange");
    String endLabel = UniqueId.makeUnique("double2uint.end");
    instructions.add(new JmpCC(CondCode.AE, outOfRangeLabel));
    instructions.add(new Cvttsd2si(AssemblyType.Quadword, src, dst));
    instructions.add(new Jmp(endLabel));
    instructions.add(new Label(outOfRangeLabel));
    instructions.add(new Mov(AssemblyType.Double, src, RegisterOperand.XMM1));
    instructions
        .add(new AsmBinary(TokenType.MINUS, AssemblyType.Double, new Data(upperBound.name()),
            RegisterOperand.XMM1));
    instructions.add(new Cvttsd2si(AssemblyType.Quadword, RegisterOperand.XMM1, dst));
    instructions
        // 9223372036854775808L is out of range for a 64-bit signed number. WTH!?
        // so I'm trying MIN_VALUE, which is 2^63 exactly
        .add(new Mov(AssemblyType.Quadword, new Imm(Long.MIN_VALUE), RegisterOperand.RDX));
    instructions
        .add(new AsmBinary(TokenType.PLUS, AssemblyType.Quadword, RegisterOperand.RDX, dst));
    instructions.add(new Label(endLabel));
    return instructions;
  }

  @Override
  public List<Instruction> visit(TackyIntToDouble op) {
    // Page 318, 334
    Operand src = toOperand(op.src());
    Operand dst = toOperand(op.dst());
    AssemblyType srcType = assemblyType(op.src());
    return ImmutableList.of(new Cvtsi2sd(srcType, src, dst));
  }

  @Override
  public List<Instruction> visit(TackyUIntToDouble op) {
    Operand src = toOperand(op.src());
    Operand dst = toOperand(op.dst());
    Type srcType = op.src().type();
    if (srcType.equals(Type.INT)) {
      // from unsigned int.
      return ImmutableList.of(
          new MovZeroExtend(src, RegisterOperand.RAX),
          new Cvtsi2sd(AssemblyType.Quadword, RegisterOperand.RAX, dst));
    }

    // from unsigned long
    List<Instruction> instructions = new ArrayList<>();
    String outOfRangelabel = UniqueId.makeUnique("uint2double.outofrange");
    String endLabel = UniqueId.makeUnique("uint2double.end");
    instructions.add(new Cmp(AssemblyType.Double, new Imm(0), src));
    instructions.add(new JmpCC(CondCode.L, outOfRangelabel));
    instructions.add(new Cvtsi2sd(AssemblyType.Quadword, src, dst));
    instructions.add(new Jmp(endLabel));
    instructions.add(new Label(outOfRangelabel));
    instructions.add(new Mov(AssemblyType.Quadword, src, RegisterOperand.RAX));
    instructions.add(new Mov(AssemblyType.Quadword, RegisterOperand.RAX, RegisterOperand.RDX));
    instructions
        .add(new AsmUnary(TokenType.SHIFT_RIGHT, AssemblyType.Quadword, RegisterOperand.RDX));
    instructions.add(
        new AsmBinary(TokenType.AMPERSAND, AssemblyType.Quadword, new Imm(1),
            RegisterOperand.RAX));
    instructions.add(new AsmBinary(TokenType.BAR, AssemblyType.Quadword, RegisterOperand.RAX,
        RegisterOperand.RDX));
    instructions.add(new Cvtsi2sd(AssemblyType.Quadword, RegisterOperand.RDX, dst));
    instructions.add(new AsmBinary(TokenType.PLUS, AssemblyType.Double, dst, dst));
    instructions.add(new Label(endLabel));
    return instructions;
  }
}