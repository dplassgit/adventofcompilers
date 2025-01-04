package com.plasstech.lang.c.codegen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.FunDecl;
import com.plasstech.lang.c.parser.GenericNodeVisitor;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;

/**
 * Input: Program (AST). Output: AsmProgramNode (ASM AST)
 */
public class CodeGen {
  public AsmProgramNode generate(Program program) {
    List<AsmFunctionNode> fns = program.funDecls().stream().map(fn -> generate(fn)).toList();
    return new AsmProgramNode(fns);
  }

  private AsmFunctionNode generate(FunDecl functionDef) {
    BlockItem body = functionDef.body().get().items().get(0);
    StatementVisitor sv = new StatementVisitor();
    List<Instruction> instructions = body.accept(sv);
    return new AsmFunctionNode(functionDef.name(), ImmutableList.copyOf(instructions));
  }

  private static class StatementVisitor extends GenericNodeVisitor<List<Instruction>> {
    @Override
    public List<Instruction> visit(Return n) {
      List<Instruction> instructions = new ArrayList<>();
      // Generate the instructions for the expression
      OperandVisitor ov = new OperandVisitor();
      Operand op = n.exp().accept(ov);

      // Add a "mov" and a "ret"
      instructions.add(new Mov(op, RegisterOperand.RAX));
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
}
