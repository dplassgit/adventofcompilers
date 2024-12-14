package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.Cdq;
import com.plasstech.lang.c.codegen.Idiv;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.RegisterOperand.Register;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.lex.TokenType;

/**
 * Input: TackyInstruction.
 * <p>
 * Output: List<Instruction>
 */
final class TackyInstructionToInstructionsVisitor
    implements TackyInstruction.Visitor<List<Instruction>> {

  private final TackyValToOperandVisitor valVisitor = new TackyValToOperandVisitor();

  @Override
  public List<Instruction> visit(TackyUnary op) {
    List<Instruction> instructions = new ArrayList<>();
    Operand src = op.src().accept(valVisitor);
    Operand dest = op.dest().accept(valVisitor);
    instructions.add(new Mov(src, dest));
    instructions.add(new AsmUnary(op.operator(), dest));
    return instructions;
  }

  @Override
  public List<Instruction> visit(TackyBinary op) {
    List<Instruction> instructions = new ArrayList<>();
    Operand src1 = op.src1().accept(valVisitor);
    Operand src2 = op.src2().accept(valVisitor);
    Operand dest = op.dest().accept(valVisitor);
    if (op.operator() == TokenType.SLASH || op.operator() == TokenType.PERCENT) {
      // mov (src1, registeroperand(ax))
      instructions.add(new Mov(src1, new RegisterOperand(Register.EAX)));
      // cdq
      instructions.add(new Cdq());
      // idiv(src2)
      instructions.add(new Idiv(src2));
      if (op.operator() == TokenType.SLASH) {
        instructions.add(new Mov(new RegisterOperand(Register.EAX), dest));
        // mov(reg(ax), dst)
      } else {
        // mov(reg(dx), dst)  for percent
        instructions.add(new Mov(new RegisterOperand(Register.EDX), dest));
      }
      return instructions;
    }
    // For +, -, *: 
    // First move src1 to dest
    instructions.add(new Mov(src1, dest));
    // Then use dest and src2 with the operator
    instructions.add(new AsmBinary(op.operator(), src2, dest));
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Instruction> visit(TackyJump op) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Instruction> visit(TackyJumpZero op) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Instruction> visit(TackyJumpNotZero op) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Instruction> visit(TackyLabel op) {
    // TODO Auto-generated method stub
    return null;
  }
}