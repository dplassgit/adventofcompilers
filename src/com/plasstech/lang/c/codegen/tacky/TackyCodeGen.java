package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.common.Labelled;
import com.plasstech.lang.c.common.UniqueId;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.AstNode;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.Block;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Break;
import com.plasstech.lang.c.parser.Cast;
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
import com.plasstech.lang.c.parser.StorageClass;
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;
import com.plasstech.lang.c.parser.VarDecl;
import com.plasstech.lang.c.parser.While;
import com.plasstech.lang.c.typecheck.Attribute;
import com.plasstech.lang.c.typecheck.FunType;
import com.plasstech.lang.c.typecheck.InitialValue;
import com.plasstech.lang.c.typecheck.Initializer;
import com.plasstech.lang.c.typecheck.StaticAttr;
import com.plasstech.lang.c.typecheck.Symbol;
import com.plasstech.lang.c.typecheck.SymbolTable;
import com.plasstech.lang.c.typecheck.Type;

/**
 * Input: Program (Parse AST)
 * <p>
 * Output: TackyProgram (Tacky AST)
 */
public class TackyCodeGen implements AstNode.Visitor<TackyVal> {
  private static final TackyVal ONE = new TackyConstant(Type.INT, 1);
  private static final TackyVal ZERO = new TackyConstant(Type.INT, 0);

  private final SymbolTable symbolTable;
  private final List<TackyInstruction> instructions = new ArrayList<>();

  public TackyCodeGen(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public TackyProgram generate(Program program) {
    List<TackyTopLevel> functionDefs =
        program.funDecls().stream()
            // Only generate Tacky instructions for functions with bodies. Page 182.
            .filter(fd -> fd.body().isPresent())
            .map(fd -> (TackyTopLevel) generate(fd)).collect(Collectors.toList());

    List<TackyTopLevel> allTopLevel = new ArrayList<>(functionDefs);
    allTopLevel.addAll(convertSymbolsToTacky());
    return new TackyProgram(allTopLevel);
  }

  // Page 235
  private List<TackyTopLevel> convertSymbolsToTacky() {
    List<TackyTopLevel> defs = new ArrayList<>();
    for (Symbol s : symbolTable.values()) {
      Attribute attr = s.attribute();
      if (attr instanceof StaticAttr sa) {
        InitialValue iv = sa.init();
        // Middle of 259
        if (iv instanceof Initializer i) {
          defs.add(
              new TackyStaticVariable(s.name(), sa.isGlobal(), s.type(), i.staticInit()));
        } else if (iv.equals(InitialValue.TENTATIVE)) {
          defs.add(new TackyStaticVariable(s.name(), sa.isGlobal(), s.type(),
              Initializer.of(0, s.type()).staticInit()));
        }
      }
    }
    return defs;
  }

  // Page 261
  private TackyVar makeTackyVariable(String name, Type type) {
    TackyVar dst = new TackyVar(UniqueId.makeUnique(name), type);

    symbolTable.put(dst.identifier(), new Symbol(dst.identifier(), type, Attribute.LOCAL_ATTR));
    return dst;
  }

  private void emit(TackyInstruction ti) {
    instructions.add(ti);
  }

  private TackyFunction generate(FunDecl functionDef) {
    instructions.clear(); // otherwise things will grow out of control

    functionDef.body().get().accept(this);
    emit(new TackyReturn(ZERO));
    // Must make a copy, otherwise we will have our successors list of instructions too.

    // HOW to know if this should be global? "from the symbol table"
    Symbol s = symbolTable.get(functionDef.name());
    assert (s != null);
    assert (s.type() instanceof FunType);
    return new TackyFunction(functionDef.name(),
        s.attribute().isGlobal(),
        ImmutableList.copyOf(functionDef.paramNames()),
        ImmutableList.copyOf(instructions));
  }

  @Override
  public TackyVal visit(Block n) {
    for (BlockItem item : n.items()) {
      item.accept(this);
    }
    return null;
  }

  @Override
  public TackyVal visit(Compound n) {
    return n.block().accept(this);
  }

  @Override
  public <T extends Number> TackyVal visit(Constant<T> n) {
    return new TackyConstant(n.type(), n.asLong());
  }

  @Override
  public TackyVal visit(Var n) {
    return new TackyVar(n.identifier(), n.type());
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
    TackyVar dst = new TackyVar(n.name(), n.type());
    if (!(n.hasStorageClass(StorageClass.STATIC)) || n.hasStorageClass(StorageClass.EXTERN)) {
      // Do not generate code for var decls with storage class static or extern (page 234)
      if (n.init().isPresent()) {
        TackyVal result = n.init().get().accept(this);
        emit(new TackyCopy(result, dst));
      }
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
    TackyVar dst = makeTackyVariable("unaryexp_result", n.type());
    emit(new TackyUnary(dst, n.operator(), src));
    return dst;
  }

  @Override
  public TackyVal visit(BinExp n) {
    TackyVal src1 = n.left().accept(this);
    TackyVar dst = makeTackyVariable("binexp_result", n.type());
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
    // Page 127
    // evaluate conditional. if false, jump to "else".
    TackyVar result = makeTackyVariable("cond_result", n.type()); // WAS: int
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
    TackyVar result = makeTackyVariable("fun_call_" + n.identifier(), n.returnType());
    emit(new TackyFunCall(n.identifier(), args, result));
    return result;
  }

  @Override
  public TackyVal visit(Cast n) {
    // p 260
    TackyVal result = n.exp().accept(this);
    Type targetType = n.targetType();
    if (targetType.equals(n.exp().type())) {
      return result;
    }
    TackyVar dst = makeTackyVariable("cast_to_" + n.targetType().name(), n.type());
    if (targetType.equals(Type.LONG)) {
      emit(new TackySignExtend(result, dst));
    } else if (targetType.equals(Type.INT)) {
      emit(new TackyTruncate(result, dst));
    } else {
      throw new UnsupportedOperationException("Cannot generate cast");
    }
    return dst;
  }
}
