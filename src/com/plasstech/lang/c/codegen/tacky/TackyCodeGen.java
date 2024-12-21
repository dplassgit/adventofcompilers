package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.AstNode;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Conditional;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.Declaration;
import com.plasstech.lang.c.parser.Expression;
import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.If;
import com.plasstech.lang.c.parser.NullStatement;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;

/**
 * Input: Program (Parse AST)
 * <p>
 * Output: TackyProgram (Tacky AST)
 */
public class TackyCodeGen implements AstNode.Visitor<TackyVal> {
  private static final TackyVal ONE = new TackyIntConstant(1);
  private static final TackyVal ZERO = new TackyIntConstant(0);

  private List<TackyInstruction> instructions = new ArrayList<>();

  public TackyProgram generate(Program program) {
    return new TackyProgram(generate(program.functionDef()));
  }

  private TackyFunctionDef generate(FunctionDef functionDef) {
    return new TackyFunctionDef(functionDef.name(), generate(functionDef.body()));
  }

  private List<TackyInstruction> generate(List<BlockItem> body) {
    for (BlockItem item : body) {
      item.accept(this);
    }
    instructions.add(new TackyReturn(ZERO));
    return instructions;
  }

  @Override
  public <T> TackyVal visit(Constant<T> n) {
    return new TackyIntConstant(n.asInt());
  }

  @Override
  public TackyVal visit(Var n) {
    return new TackyVar(n.identifier());
  }

  @Override
  public TackyVal visit(Assignment n) {
    TackyVal result = n.rvalue().accept(this);
    TackyVar dst = (TackyVar) n.lvalue().accept(this);
    instructions.add(new TackyCopy(result, dst));
    return dst;
  }

  @Override
  public TackyVal visit(Declaration n) {
    TackyVar dst = new TackyVar(n.identifier());
    if (n.init().isPresent()) {
      TackyVal result = n.init().get().accept(this);
      instructions.add(new TackyCopy(result, dst));
    }
    return dst;
  }

  @Override
  public TackyVal visit(Expression e) {
    return e.exp().accept(this);
  }

  @Override
  public TackyVal visit(UnaryExp n) {
    TackyVal src = n.exp().accept(this);
    TackyVar dst = newTemp("unaryexp_result");
    instructions.add(new TackyUnary(dst, n.operator(), src));
    return dst;
  }

  @Override
  public TackyVal visit(BinExp n) {
    TackyVal src1 = n.left().accept(this);
    TackyVar dst = newTemp("binexp_result");
    if (n.operator() == TokenType.DOUBLE_AMP) {
      // Short circuit
      String falseLabel = makeUnique("and_false");
      instructions.add(new TackyJumpZero(src1, falseLabel));
      TackyVal src2 = n.right().accept(this);
      instructions.add(new TackyJumpZero(src2, falseLabel));
      instructions.add(new TackyCopy(ONE, dst));
      String endLabel = makeUnique("and_end");
      instructions.add(new TackyJump(endLabel));
      instructions.add(new TackyLabel(falseLabel));
      instructions.add(new TackyCopy(ZERO, dst));
      instructions.add(new TackyLabel(endLabel));
    } else if (n.operator() == TokenType.DOUBLE_BAR) {
      String trueLabel = makeUnique("or_true");
      instructions.add(new TackyJumpNotZero(src1, trueLabel));
      TackyVal src2 = n.right().accept(this);
      instructions.add(new TackyJumpNotZero(src2, trueLabel));
      instructions.add(new TackyCopy(ZERO, dst));
      String endLabel = makeUnique("or_end");
      instructions.add(new TackyJump(endLabel));
      instructions.add(new TackyLabel(trueLabel));
      instructions.add(new TackyCopy(ONE, dst));
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

  @Override
  public TackyVal visit(Conditional n) {
    // evaluate conditional. if false, jump to "else".
    TackyVar result = newTemp("cond_result");
    String falseLabel = makeUnique("cond_false");
    String endLabel = makeUnique("cond_end");

    TackyVal condDest = n.condition().accept(this);
    instructions.add(new TackyJumpZero(condDest, falseLabel));
    TackyVal leftVal = n.left().accept(this);
    instructions.add(new TackyCopy(leftVal, result));
    instructions.add(new TackyJump(endLabel));

    instructions.add(new TackyLabel(falseLabel));
    TackyVal rightVal = n.right().accept(this);
    instructions.add(new TackyCopy(rightVal, result));

    instructions.add(new TackyLabel(endLabel));
    return result;
  }

  @Override
  public TackyVal visit(If n) {
    TackyVal condDest = n.condition().accept(this);
    String endLabel = makeUnique("if_end");
    String elseLabel = makeUnique("else");
    if (n.elseStmt().isPresent()) {
      instructions.add(new TackyJumpZero(condDest, elseLabel));
    } else {
      instructions.add(new TackyJumpZero(condDest, endLabel));
    }
    n.then().accept(this);
    if (n.elseStmt().isPresent()) {
      // Jump past the 'else"
      instructions.add(new TackyJump(endLabel));
      instructions.add(new TackyLabel(elseLabel));
      n.elseStmt().get().accept(this);
    }
    instructions.add(new TackyLabel(endLabel));
    return condDest;
  }

  @Override
  public TackyVal visit(NullStatement n) {
    return null;
  }

  private static int id = 0;

  private static String makeUnique(String prefix) {
    return String.format("%s.%d", prefix, id++);
  }

  private static TackyVar newTemp(String prefix) {
    return new TackyVar(makeUnique(prefix));
  }

  @Override
  public TackyVal visit(FunctionDef n) {
    throw new IllegalStateException("Should not codegen " + n.getClass().getCanonicalName());
  }

  @Override
  public TackyVal visit(Program n) {
    throw new IllegalStateException("Should not codegen " + n.getClass().getCanonicalName());
  }
}
