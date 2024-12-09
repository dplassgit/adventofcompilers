package com.plasstech.lang.c.codegen.tacky;

import java.util.List;

import com.plasstech.lang.c.parser.DefaultAstNodeVisitor;
import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.Statement;
import com.plasstech.lang.c.parser.UnaryExp;

public class TackyCodeGen {
  public TackyProgram generate(Program program) {
    return new TackyProgram(generate(program.functionDef()));
  }

  private TackyFunctionDef generate(FunctionDef functionDef) {
    return new TackyFunctionDef(functionDef.name(), generate(functionDef.body()));
  }

  private static class Foo extends DefaultAstNodeVisitor {
    TackyInstruction instruction;

    @Override
    public Void visit(UnaryExp n) {
      return null;
    }

    @Override
    public Void visit(Return n) {
      return null;
    }
  }

  private List<TackyInstruction> generate(Statement body) {
    return null;
  }
}
