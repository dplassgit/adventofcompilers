package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.GenericNodeVisitor;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.Statement;
import com.plasstech.lang.c.parser.UnaryExp;

/**
 * Input: Program (Parse AST)
 * <p>
 * Output: TackyProgram (Tacky AST)
 */
public class TackyCodeGen extends GenericNodeVisitor<TackyVal> {
  private List<TackyInstruction> instructions = new ArrayList<>();

  private static int id = 0;

  private static String makeTemp(String prefix) {
    return String.format("%s.%d", prefix, id++);
  }

  public TackyProgram generate(Program program) {
    return new TackyProgram(generate(program.functionDef()));
  }

  private TackyFunctionDef generate(FunctionDef functionDef) {
    return new TackyFunctionDef(functionDef.name(), generate(functionDef.body()));
  }

  private List<TackyInstruction> generate(Statement body) {
    body.accept(this);
    return instructions;
  }

  @Override
  public <T> TackyVal visit(Constant<T> n) {
    return new TackyIntConstant(n.asInt());
  }

  @Override
  public TackyVal visit(UnaryExp n) {
    TackyVal src = n.exp().accept(this);
    String destName = makeTemp("unaryexp");
    TackyVar dst = new TackyVar(destName);
    instructions.add(new TackyUnary(dst, n.operator(), src));
    return dst;
  }

  @Override
  public TackyVal visit(BinExp n) {
    TackyVal src1 = n.left().accept(this);
    TackyVal src2 = n.right().accept(this);
    String destName = makeTemp("binexp");
    TackyVar dst = new TackyVar(destName);
    instructions.add(new TackyBinary(dst, src1, n.operator(), src2));
    return dst;
  }

  @Override
  public TackyVal visit(Return n) {
    TackyVal dst = n.exp().accept(this);
    instructions.add(new TackyReturn(dst));
    return dst;
  }
}
