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
    if (operator == TokenType.SLASH || operator == TokenType.PERCENT) {
      // mov (src1, registeroperand(ax))
      instructions.add(new Mov(src1, new RegisterOperand(Register.EAX)));
      // cdq
      instructions.add(new Cdq());
      // idiv(src2)
      instructions.add(new Idiv(src2));
      if (operator == TokenType.SLASH) {
        instructions.add(new Mov(new RegisterOperand(Register.EAX), dst));
        // mov(reg(ax), dst)
      } else {
        // mov(reg(dx), dst)  for percent
        instructions.add(new Mov(new RegisterOperand(Register.EDX), dst));
      }
      return instructions;
    }
    if (operator.isConditional) {
      // Do something different.
      instructions.add(new Cmp(src2, src1));
      instructions.add(new Mov(ZERO, dst));
      instructions.add(new SetCC(CondCode.from(operator), dst));
    }
    // For +, -, *: 
    // First move src1 to dest
    instructions.add(new Mov(src1, dst));
    // Then use dest and src2 with the operator
    instructions.add(new AsmBinary(op.operator(), src2, dst));
    return instructions;
  }

  @Override
  public List<Instruction> visit(TackyReturn tackyReturn) {
    List<Instruction> instructions = new ArrayList<>();
    Operand operand = tackyReturn.val().accept(valVisitor);
    instructions.add(new Mov(operand, new RegisterOperand(Register.EAX)));
    instructions.add(new Ret());
    return instructions;
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
    Operand operand = op.condition().accept(valVisitor);
    return ImmutableList.of(new Cmp(ZERO, operand),
        new JmpCC(CondCode.E, op.target()));
  }

  @Override
  public List<Instruction> visit(TackyJumpNotZero op) {
    Operand operand = op.condition().accept(valVisitor);
    return ImmutableList.of(new Cmp(ZERO, operand),
        new JmpCC(CondCode.NE, op.target()));
  }

  @Override
  public List<Instruction> visit(TackyLabel op) {
    return ImmutableList.of(new Label(op.target()));
  }
}