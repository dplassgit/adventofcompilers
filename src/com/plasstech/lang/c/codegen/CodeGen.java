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
import com.plasstech.lang.c.parser.StorageClass;

/**
 * Input: Program (AST). Output: AsmProgramNode (ASM AST) Mostly obsolete, only used in chapter 1?
 */
public class CodeGen {
  public AsmProgram generate(Program program) {
    List<AsmTopLevel> fns = program.funDecls().stream().map(fn -> generate(fn)).toList();
    return new AsmProgram(fns);
  }

  private AsmTopLevel generate(FunDecl functionDef) {
    // This is totally bogus
    BlockItem body = functionDef.body().get().items().get(0);
    StatementVisitor sv = new StatementVisitor();
    List<Instruction> instructions = body.accept(sv);
    return new AsmFunction(functionDef.name(),
        !functionDef.hasStorageClass(StorageClass.STATIC), ImmutableList.copyOf(instructions));
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
    public <T extends Number> Operand visit(Constant<T> n) {
      return new Imm(n.value().toString());
    }
  }
}
