package com.plasstech.lang.c.codegen.tacky;

public interface TackyNodeVisitor {
  void visit(TackyProgram tackyProgram);

  void visit(TackyFunctionDef tackyFunctionNode);

  void visit(TackyUnary tackyUnaryOp);

  void visit(TackyReturn tackyReturn);

  void visit(TackyConstant tackyConstant);

  void visit(TackyVar tackyVar);
}
