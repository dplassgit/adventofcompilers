package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.RegisterOperand.Register;
import com.plasstech.lang.c.codegen.Ret;

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
    return null;
  }

  @Override
  public List<Instruction> visit(TackyReturn tackyReturn) {
    List<Instruction> instructions = new ArrayList<>();
    Operand operand = tackyReturn.val().accept(valVisitor);
    instructions.add(new Mov(operand, new RegisterOperand(Register.EAX)));
    instructions.add(new Ret());
    return instructions;
  }
}