package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;

import com.plasstech.lang.c.common.Labelled;
import com.plasstech.lang.c.common.UniqueId;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.AstNode;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.Block;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Break;
import com.plasstech.lang.c.parser.Compound;
import com.plasstech.lang.c.parser.Conditional;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.Continue;
import com.plasstech.lang.c.parser.Declaration;
import com.plasstech.lang.c.parser.DoWhile;
import com.plasstech.lang.c.parser.Expression;
import com.plasstech.lang.c.parser.For;
import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.If;
import com.plasstech.lang.c.parser.InitDecl;
import com.plasstech.lang.c.parser.InitExp;
import com.plasstech.lang.c.parser.NullStatement;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;
import com.plasstech.lang.c.parser.While;

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
    functionDef.body().accept(this);
    instructions.add(new TackyReturn(ZERO));
    return new TackyFunctionDef(functionDef.name(), instructions);
  }

  @Override
  public TackyVal visit(Block n) {
    generate(n.items());
    return null;
  }

  @Override
  public TackyVal visit(Compound n) {
    return n.block().accept(this);
  }

  private List<TackyInstruction> generate(List<BlockItem> body) {
    for (BlockItem item : body) {
      item.accept(this);
    }
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
      String falseLabel = UniqueId.makeUnique("and_false");
      instructions.add(new TackyJumpZero(src1, falseLabel));
      TackyVal src2 = n.right().accept(this);
      instructions.add(new TackyJumpZero(src2, falseLabel));
      instructions.add(new TackyCopy(ONE, dst));
      String endLabel = UniqueId.makeUnique("and_end");
      instructions.add(new TackyJump(endLabel));
      instructions.add(new TackyLabel(falseLabel));
      instructions.add(new TackyCopy(ZERO, dst));
      instructions.add(new TackyLabel(endLabel));
    } else if (n.operator() == TokenType.DOUBLE_BAR) {
      String trueLabel = UniqueId.makeUnique("or_true");
      instructions.add(new TackyJumpNotZero(src1, trueLabel));
      TackyVal src2 = n.right().accept(this);
      instructions.add(new TackyJumpNotZero(src2, trueLabel));
      instructions.add(new TackyCopy(ZERO, dst));
      String endLabel = UniqueId.makeUnique("or_end");
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
    String falseLabel = UniqueId.makeUnique("cond_false");
    String endLabel = UniqueId.makeUnique("cond_end");

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
    String endLabel = UniqueId.makeUnique("if_end");
    String elseLabel = UniqueId.makeUnique("else");
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

  private static TackyVar newTemp(String prefix) {
    return new TackyVar(UniqueId.makeUnique(prefix));
  }

  @Override
  public TackyVal visit(FunctionDef n) {
    throw new IllegalStateException("Should not codegen " + n.getClass().getCanonicalName());
  }

  @Override
  public TackyVal visit(Program n) {
    throw new IllegalStateException("Should not codegen " + n.getClass().getCanonicalName());
  }

  private static String beforeLabel(Labelled node) {
    return "before_" + node.label();
  }

  private static String afterLabel(Labelled node) {
    return "after_" + node.label();
  }

  @Override
  public TackyVal visit(Break n) {
    instructions.add(new TackyJump(afterLabel(n)));
    return null;
  }

  @Override
  public TackyVal visit(Continue n) {
    instructions.add(new TackyJump(beforeLabel(n)));
    return null;
  }

  @Override
  public TackyVal visit(DoWhile n) {
    String beforeLabel = beforeLabel(n);
    instructions.add(new TackyLabel(beforeLabel));
    n.body().accept(this);
    TackyVal cond = n.condition().accept(this);
    instructions.add(new TackyJumpNotZero(cond, beforeLabel));
    instructions.add(new TackyLabel(afterLabel(n)));
    return null;
  }

  @Override
  public TackyVal visit(For n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TackyVal visit(While n) {
    String beforeLabel = beforeLabel(n);
    String afterLabel = afterLabel(n);
    instructions.add(new TackyLabel(beforeLabel));
    TackyVal cond = n.condition().accept(this);
    instructions.add(new TackyJumpZero(cond, afterLabel));
    n.body().accept(this);
    instructions.add(new TackyJump(beforeLabel));
    instructions.add(new TackyLabel(afterLabel));
    return null;
  }

  @Override
  public TackyVal visit(InitDecl n) {
    return n.decl().accept(this);
  }

  @Override
  public TackyVal visit(InitExp n) {
    if (n.exp().isPresent()) {
      return n.exp().get().accept(this);
    }
    return null;
  }
}
