package com.plasstech.lang.c.codegen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.RegisterOperand.Register;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.DefaultAstNodeVisitor;
import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.Statement;

public class CodeGen {
  public AsmProgramNode generate(Program program) {
    AsmFunctionNode function = generate(program.functionDef());
    return new AsmProgramNode(function);
  }

  private static class StatementVisitor extends DefaultAstNodeVisitor {
    List<Instruction> instructions = new ArrayList<>();

    @Override
    public void visit(Return n) {
      // Generate the instructions for the expression
      OperandVisitor ov = new OperandVisitor();
      n.expr().accept(ov);

      // Add a "mov" and a "ret"
      instructions.add(new Mov(ov.operand, new RegisterOperand(Register.EAX)));
      instructions.add(new Ret());
    }
  }

  private static class OperandVisitor extends DefaultAstNodeVisitor {
    Operand operand;

    @Override
    public <T> void visit(Constant<T> n) {
      operand = new Imm(n.value().toString());
    }
  }

  private AsmFunctionNode generate(FunctionDef functionDef) {
    Statement body = functionDef.body();
    StatementVisitor sv = new StatementVisitor();
    body.accept(sv);
    return new AsmFunctionNode(functionDef.name(), ImmutableList.copyOf(sv.instructions));
  }
}
