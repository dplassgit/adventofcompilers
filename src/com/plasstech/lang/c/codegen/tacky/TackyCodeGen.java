package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.plasstech.lang.c.lex.TokenType;
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
  private static final TackyVal TRUE = new TackyIntConstant(1);
  private static final TackyVal FALSE = new TackyIntConstant(0);

  private List<TackyInstruction> instructions = new ArrayList<>();

  private static int id = 0;

  private static String makeUnique(String prefix) {
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
    String destName = makeUnique("unaryexp");
    TackyVar dst = new TackyVar(destName);
    instructions.add(new TackyUnary(dst, n.operator(), src));
    return dst;
  }

  @Override
  public TackyVal visit(BinExp n) {
    TackyVal src1 = n.left().accept(this);
    TackyVar dst = new TackyVar(makeUnique("binexp_result"));
    if (n.operator() == TokenType.DOUBLE_AMP) {
      // Short circuit
      String falseLabel = makeUnique("and_false");
      instructions.add(new TackyJumpZero(src1, falseLabel));
      TackyVal src2 = n.right().accept(this);
      instructions.add(new TackyJumpZero(src2, falseLabel));
      instructions.add(new TackyCopy(TRUE, dst));
      String endLabel = makeUnique("and_end");
      instructions.add(new TackyJump(endLabel));
      instructions.add(new TackyLabel(falseLabel));
      instructions.add(new TackyCopy(FALSE, dst));
      instructions.add(new TackyLabel(endLabel));
    } else if (n.operator() == TokenType.DOUBLE_BAR) {
      String trueLabel = makeUnique("or_true");
      instructions.add(new TackyJumpNotZero(src1, trueLabel));
      TackyVal src2 = n.right().accept(this);
      instructions.add(new TackyJumpNotZero(src2, trueLabel));
      instructions.add(new TackyCopy(FALSE, dst));
      String endLabel = makeUnique("or_end");
      instructions.add(new TackyJump(endLabel));
      instructions.add(new TackyLabel(trueLabel));
      instructions.add(new TackyCopy(TRUE, dst));
      instructions.add(new TackyLabel(endLabel));
    } else {
      TackyVal src2 = n.right().accept(this);
      instructions.add(new TackyBinary(dst, src1, n.operator(), src2));
    }
    return dst;
  }

  @Override
  public TackyVal visit(Return n) {
    TackyVal dst = n.exp().accept(this);
    instructions.add(new TackyReturn(dst));
    return dst;
  }
}
