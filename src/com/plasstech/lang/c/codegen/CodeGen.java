package com.plasstech.lang.c.codegen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.RegisterOperand.Register;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.GenericNodeVisitor;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.Statement;

public class CodeGen {
  public AsmProgramNode generate(Program program) {
    AsmFunctionNode function = generate(program.functionDef());
    return new AsmProgramNode(function);
  }

  private static class StatementVisitor extends GenericNodeVisitor<List<Instruction>> {
    @Override
    public List<Instruction> visit(Return n) {
      List<Instruction> instructions = new ArrayList<>();
      // Generate the instructions for the expression
      OperandVisitor ov = new OperandVisitor();
      Operand op = n.exp().accept(ov);

      // Add a "mov" and a "ret"
      instructions.add(new Mov(op, new RegisterOperand(Register.EAX)));
      instructions.add(new Ret());
      return instructions;
    }
  }

  private static class OperandVisitor extends GenericNodeVisitor<Operand> {
    @Override
    public <T> Operand visit(Constant<T> n) {
      return new Imm(n.value().toString());
    }
  }

  private AsmFunctionNode generate(FunctionDef functionDef) {
    Statement body = functionDef.body();
    StatementVisitor sv = new StatementVisitor();
    List<Instruction> instructions = body.accept(sv);
    return new AsmFunctionNode(functionDef.name(), ImmutableList.copyOf(instructions));
  }
}
