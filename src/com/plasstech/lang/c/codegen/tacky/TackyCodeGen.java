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
import com.plasstech.lang.c.parser.DoWhile;
import com.plasstech.lang.c.parser.Expression;
import com.plasstech.lang.c.parser.For;
import com.plasstech.lang.c.parser.FunDecl;
import com.plasstech.lang.c.parser.FunctionCall;
import com.plasstech.lang.c.parser.If;
import com.plasstech.lang.c.parser.InitDecl;
import com.plasstech.lang.c.parser.InitExp;
import com.plasstech.lang.c.parser.NullStatement;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;
import com.plasstech.lang.c.parser.VarDecl;
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
    List<TackyFunctionDef> functionDefs =
        program.funDecls().stream()
            // Only generate Tacky instructoins for functions with bodies. Page 182.
            .filter(fd -> fd.body().isPresent())
            .map(fd -> generate(fd)).toList();
    return new TackyProgram(functionDefs);
  }

  private void emit(TackyInstruction ti) {
    instructions.add(ti);
  }

  private TackyFunctionDef generate(FunDecl functionDef) {
    functionDef.body().get().accept(this);
    emit(new TackyReturn(ZERO));
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
    emit(new TackyCopy(result, dst));
    return dst;
  }

  @Override
  public TackyVal visit(VarDecl n) {
    TackyVar dst = new TackyVar(n.identifier());
    if (n.init().isPresent()) {
      TackyVal result = n.init().get().accept(this);
      emit(new TackyCopy(result, dst));
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
    emit(new TackyUnary(dst, n.operator(), src));
    return dst;
  }

  @Override
  public TackyVal visit(BinExp n) {
    TackyVal src1 = n.left().accept(this);
    TackyVar dst = newTemp("binexp_result");
    if (n.operator() == TokenType.DOUBLE_AMP) {
      // Short circuit
      String falseLabel = UniqueId.makeUnique("and_false");
      emit(new TackyJumpZero(src1, falseLabel));
      TackyVal src2 = n.right().accept(this);
      emit(new TackyJumpZero(src2, falseLabel));
      emit(new TackyCopy(ONE, dst));
      String endLabel = UniqueId.makeUnique("and_end");
      emit(new TackyJump(endLabel));
      emit(new TackyLabel(falseLabel));
      emit(new TackyCopy(ZERO, dst));
      emit(new TackyLabel(endLabel));
    } else if (n.operator() == TokenType.DOUBLE_BAR) {
      String trueLabel = UniqueId.makeUnique("or_true");
      emit(new TackyJumpNotZero(src1, trueLabel));
      TackyVal src2 = n.right().accept(this);
      emit(new TackyJumpNotZero(src2, trueLabel));
      emit(new TackyCopy(ZERO, dst));
      String endLabel = UniqueId.makeUnique("or_end");
      emit(new TackyJump(endLabel));
      emit(new TackyLabel(trueLabel));
      emit(new TackyCopy(ONE, dst));
      emit(new TackyLabel(endLabel));
    } else {
      TackyVal src2 = n.right().accept(this);
      emit(new TackyBinary(dst, src1, n.operator(), src2));
    }
    return dst;
  }

  @Override
  public TackyVal visit(Return n) {
    TackyVal dst = n.exp().accept(this);
    emit(new TackyReturn(dst));
    return dst;
  }

  @Override
  public TackyVal visit(Conditional n) {
    // evaluate conditional. if false, jump to "else".
    TackyVar result = newTemp("cond_result");
    String falseLabel = UniqueId.makeUnique("cond_false");
    String endLabel = UniqueId.makeUnique("cond_end");

    TackyVal condDest = n.condition().accept(this);
    emit(new TackyJumpZero(condDest, falseLabel));
    TackyVal leftVal = n.left().accept(this);
    emit(new TackyCopy(leftVal, result));
    emit(new TackyJump(endLabel));

    emit(new TackyLabel(falseLabel));
    TackyVal rightVal = n.right().accept(this);
    emit(new TackyCopy(rightVal, result));

    emit(new TackyLabel(endLabel));
    return result;
  }

  @Override
  public TackyVal visit(If n) {
    TackyVal condDest = n.condition().accept(this);
    String endLabel = UniqueId.makeUnique("if_end");
    String elseLabel = UniqueId.makeUnique("else");
    if (n.elseStmt().isPresent()) {
      emit(new TackyJumpZero(condDest, elseLabel));
    } else {
      emit(new TackyJumpZero(condDest, endLabel));
    }
    n.then().accept(this);
    if (n.elseStmt().isPresent()) {
      // Jump past the 'else"
      emit(new TackyJump(endLabel));
      emit(new TackyLabel(elseLabel));
      n.elseStmt().get().accept(this);
    }
    emit(new TackyLabel(endLabel));
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
  public TackyVal visit(FunDecl n) {
    assert (n.body().isEmpty());
    return null;
  }

  @Override
  public TackyVal visit(Program n) {
    throw new IllegalStateException("Should not codegen " + n.getClass().getCanonicalName());
  }

  private static String startLabel(Labelled node) {
    return "start_" + node.label();
  }

  private String breakLabel(Labelled n) {
    return "break_" + n.label();
  }

  private String continueLabel(Labelled n) {
    return "continue_" + n.label();
  }

  @Override
  public TackyVal visit(Break n) {
    emit(new TackyJump(breakLabel(n)));
    return null;
  }

  @Override
  public TackyVal visit(Continue n) {
    emit(new TackyJump(continueLabel(n)));
    return null;
  }

  @Override
  public TackyVal visit(DoWhile n) {
    String startLabel = startLabel(n);
    emit(new TackyLabel(startLabel));
    n.body().accept(this);
    emit(new TackyLabel(continueLabel(n)));
    TackyVal cond = n.condition().accept(this);
    emit(new TackyJumpNotZero(cond, startLabel));
    emit(new TackyLabel(breakLabel(n)));
    return null;
  }

  @Override
  public TackyVal visit(For n) {
    n.init().accept(this);
    String startLabel = startLabel(n);
    emit(new TackyLabel(startLabel));

    String breakLabel = breakLabel(n);
    if (n.condition().isPresent()) {
      TackyVal cond = n.condition().get().accept(this);
      emit(new TackyJumpZero(cond, breakLabel));
    }
    n.body().accept(this);
    emit(new TackyLabel(continueLabel(n)));
    if (n.post().isPresent()) {
      n.post().get().accept(this);
    }
    emit(new TackyJump(startLabel));

    emit(new TackyLabel(breakLabel));
    return null;
  }

  @Override
  public TackyVal visit(While n) {
    String continueLabel = continueLabel(n);
    emit(new TackyLabel(continueLabel));
    TackyVal cond = n.condition().accept(this);
    String breakLabel = breakLabel(n);
    emit(new TackyJumpZero(cond, breakLabel));
    n.body().accept(this);
    emit(new TackyJump(continueLabel));
    emit(new TackyLabel(breakLabel));
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

  @Override
  public TackyVal visit(FunctionCall n) {
    // Page 183, listing 9-24
    List<TackyVal> args = n.args().stream().map(arg -> arg.accept(this)).toList();
    // Unclear if this is right...
    TackyVar result = newTemp("fun_call_" + n.identifier());
    emit(new TackyFunCall(n.identifier(), args, result));
    return result;
  }
}
