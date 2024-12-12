package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.Pseudo;

/**
 * Input: TackyVal
 * <p>
 * Output: Operand
 */
class TackyValToOperandVisitor implements TackyVal.Visitor<Operand> {
  @Override
  public Operand visit(TackyVar tackyVar) {
    return new Pseudo(tackyVar.identifier());
  }

  @Override
  public Operand visit(TackyIntConstant tackyInt) {
    return new Imm(Integer.toString(tackyInt.val()));
  }
}