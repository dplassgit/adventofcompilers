package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmUnary;
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
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.RegisterOperand.Register;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;
import com.plasstech.lang.c.lex.TokenType;

/**
 * This is the "assembly generation" step.
 * <p>
 * Input: TackyInstruction
 * <p>
 * Output: List<Instruction>
 */
class TackyInstructionToInstructionsVisitor implements TackyInstruction.Visitor<List<Instruction>> {

  private static final Imm ZERO = new Imm("0");
  private final TackyValToOperandVisitor valVisitor = new TackyValToOperandVisitor();

  @Override
  public List<Instruction> visit(TackyUnary op) {
    List<Instruction> instructions = new ArrayList<>();
    Operand src = op.src().accept(valVisitor);
    Operand dst = op.dst().accept(valVisitor);
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
    Operand src1 = op.src1().accept(valVisitor);
    Operand src2 = op.src2().accept(valVisitor);
    Operand dst = op.dst().accept(valVisitor);
    TokenType operator = op.operator();
    switch (operator) {
      case SLASH:
      case PERCENT:
        // mov (src1, register(ax))
        instructions.add(new Mov(src1, new RegisterOperand(Register.AX)));
        // cdq
        instructions.add(new Cdq());
        // idiv(src2)
        instructions.add(new Idiv(src2));
        if (operator == TokenType.SLASH) {
          // mov(reg(ax), dst)
          instructions.add(new Mov(new RegisterOperand(Register.AX), dst));
        } else {
          // mov(reg(dx), dst)  for modulo
          instructions.add(new Mov(new RegisterOperand(Register.DX), dst));
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
    Operand operand = tackyReturn.val().accept(valVisitor);
    return ImmutableList.of(
        new Mov(operand, new RegisterOperand(Register.AX)),
        new Ret());
  }

  @Override
  public List<Instruction> visit(TackyCopy op) {
    Operand src = op.src().accept(valVisitor);
    Operand dst = op.dst().accept(valVisitor);
    return ImmutableList.of(new Mov(src, dst));
  }

  @Override
  public List<Instruction> visit(TackyJump op) {
    return ImmutableList.of(new Jmp(op.target()));
  }

  @Override
  public List<Instruction> visit(TackyJumpZero op) {
    // Page 86
    Operand operand = op.condition().accept(valVisitor);
    return ImmutableList.of(
        new Cmp(ZERO, operand),
        new JmpCC(CondCode.E, op.target()));
  }

  @Override
  public List<Instruction> visit(TackyJumpNotZero op) {
    // Page 86
    Operand operand = op.condition().accept(valVisitor);
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
    return null;
  }
}