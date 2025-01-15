package com.plasstech.lang.c.typecheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.Block;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Cast;
import com.plasstech.lang.c.parser.Compound;
import com.plasstech.lang.c.parser.Conditional;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.Declaration;
import com.plasstech.lang.c.parser.DoWhile;
import com.plasstech.lang.c.parser.Exp;
import com.plasstech.lang.c.parser.Expression;
import com.plasstech.lang.c.parser.For;
import com.plasstech.lang.c.parser.ForInit;
import com.plasstech.lang.c.parser.FunDecl;
import com.plasstech.lang.c.parser.FunctionCall;
import com.plasstech.lang.c.parser.If;
import com.plasstech.lang.c.parser.InitDecl;
import com.plasstech.lang.c.parser.InitExp;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.Statement;
import com.plasstech.lang.c.parser.StorageClass;
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;
import com.plasstech.lang.c.parser.VarDecl;
import com.plasstech.lang.c.parser.While;

/** Finally, a type checker. Page 178, 253-258. */
public class TypeChecker implements Validator {

  private final SymbolTable symbols;
  private Type currentRetType;

  private static final Set<TokenType> ARITHMETIC_OPS =
      ImmutableSet.of(TokenType.PLUS, TokenType.MINUS,
          TokenType.STAR, TokenType.SLASH, TokenType.PERCENT);

  public TypeChecker(SymbolTable symbols) {
    this.symbols = symbols;
  }

  @Override
  public Program validate(Program program) {
    List<Declaration> declarations = program.declarations().stream().map(d -> switch (d) {
      case FunDecl fd -> typeCheckFunDecl(fd);
      case VarDecl vd -> typeCheckFileScopeVarDecl(vd);
      default -> throw new IllegalArgumentException("Unexpected value: " + d);
    }).toList();
    return new Program(declarations);
  }

  // Page 180, 230
  private Declaration typeCheckFunDecl(FunDecl decl) {
    boolean hasBody = decl.body().isPresent();
    if (decl.hasStorageClass(StorageClass.EXTERN) && hasBody) {
      error("Cannot define `extern` function '%s'", decl.name());
      return null;
    }
    boolean global = !decl.hasStorageClass(StorageClass.STATIC);

    FunType funType = decl.funType();
    boolean alreadyDefined = false;
    Symbol oldDecl = symbols.get(decl.name());
    if (oldDecl != null) {
      // already defined 
      if (!oldDecl.type().equals(funType)) {
        error("Incompatible function declarations; '%s' already defined as '%s'",
            decl.name(), oldDecl.toString());
        return null;
      }
      alreadyDefined = oldDecl.attribute().defined();
      if (alreadyDefined && hasBody) {
        error("Function '%s' defined more than once", decl.name());
        return null;
      }
      global = oldDecl.attribute().isGlobal();
      if (oldDecl.attribute().isGlobal() && decl.hasStorageClass(StorageClass.STATIC)) {
        error("Static function declaration folllows non-static");
        return null;
      }
    }
    Attribute attr = new FunAttr(alreadyDefined || hasBody, global);
    symbols.put(decl.name(), new Symbol(decl.name(), funType, attr));

    Optional<Block> checkedBlock = Optional.empty();
    if (hasBody) {
      for (int i = 0; i < decl.paramNames().size(); ++i) {
        String paramName = decl.paramNames().get(i);
        Type paramType = decl.funType().paramTypes().get(i);
        symbols.put(paramName, new Symbol(paramName, paramType,
            Attribute.LOCAL_ATTR));
      }
      currentRetType = funType.returnType();
      checkedBlock = Optional.of(typeCheckBlock(decl.body().get()));
      currentRetType = null;
    }
    return new FunDecl(decl.name(), funType, decl.paramNames(), checkedBlock, decl.storageClass());
  }

  // Page 231
  private Declaration typeCheckFileScopeVarDecl(VarDecl decl) {
    InitialValue initialValue = InitialValue.NO_INITIALIZER;
    // Figure out initial IV
    Optional<Long> initialLong = getDeclInitialValue(decl);
    Optional<Exp> newInit = decl.init();

    // Something about subtracting 2^32 if it's an int but won't fit in an int?
    if (initialLong.isPresent()) {
      initialValue = Initializer.of(initialLong.get(), decl.type());
    } else if (decl.init().isEmpty()) {
      if (decl.hasStorageClass(StorageClass.EXTERN)) {
        initialValue = InitialValue.NO_INITIALIZER;
      } else {
        initialValue = InitialValue.TENTATIVE;
      }
    } else {
      error("Non-constant initializer for file scope variable '%s'", decl.name());
      return null;
    }

    boolean global = !decl.hasStorageClass(StorageClass.STATIC);

    Symbol oldDecl = symbols.get(decl.name());
    if (oldDecl != null) {
      // Page 257 it says something vague about making sure the old
      // and new decl are the same but unclear if there is something else we have to do,
      // or do it elsewhere too
      if (!oldDecl.type().equals(decl.type())) {
        error("Conflicting types for '%s': originally declared as %s, then as %s",
            decl.name(), oldDecl.type(), decl.type());
        return null;
      }

      if (decl.hasStorageClass(StorageClass.EXTERN)) {
        global = oldDecl.attribute().isGlobal();
      } else if (oldDecl.attribute().isGlobal() != global) {
        error("Conflicting variable linkage for '%s'", decl.name());
        return null;
      }

      // Page 231. The book is messed up. It says:
      // if oldDecl.attrs.init "is a constant" {
      //    if initialValue "is a constant" {
      //       error("Conflicting file scope variable definitions");
      //    } else {
      //      initialValue = oldDecl.attributes().init()
      //    }
      // } else if initialvalue "is not a constant" && oldDecl.attrs.init == Tentative {
      //    initialValue = Tentative
      // }

      // BUT what does "is a constant" mean"? attrs.init only exists if attrs is a static attr
      // and init can only be a constant if it's an Initializer
      if (isConstantInit(oldDecl.attribute())) {
        if (initialValue instanceof Initializer) {
          error("Conflicting file scope variable definitions");
        }
        if (oldDecl.attribute() instanceof StaticAttr sa) {
          initialValue = sa.init();
        } else {
          error("Should never get here.");
        }
      } else if (!isConstant(initialValue) && isTentative(oldDecl.attribute())) {
        initialValue = InitialValue.TENTATIVE;
      }
    }
    symbols.put(decl.name(),
        new Symbol(decl.name(), decl.type(), new StaticAttr(initialValue, global)));
    return new VarDecl(decl.name(), decl.type(), newInit, decl.storageClass());
  }

  private static boolean isTentative(Attribute attribute) {
    if (attribute instanceof StaticAttr sa) {
      return sa.init().equals(InitialValue.TENTATIVE);
    }
    return false;
  }

  private static boolean isConstant(InitialValue initialValue) {
    return initialValue instanceof Initializer;
  }

  private static boolean isConstantInit(Attribute attribute) {
    if (attribute instanceof StaticAttr sa) {
      return isConstant(sa.init());
    }
    return false;
  }

  private Optional<Long> getDeclInitialValue(VarDecl decl) {
    if (decl.init().isEmpty()) {
      return Optional.empty();
    }
    Exp e = decl.init().get();
    return switch (e) {
      case Constant<?> ci -> {
        if (ci.type().equals(Type.INT)) {
          yield Optional.of((long) ci.asInt());
        }
        if (ci.type().equals(Type.LONG)) {
          yield Optional.of(ci.asLong());
        }
        throw new IllegalArgumentException("Unexpected value: " + ci.type());
      }
      default -> Optional.empty();
    };
  }

  private Block typeCheckBlock(Block block) {
    return new Block(block.items().stream().map(this::typeCheckBlockItem).toList());
  }

  private BlockItem typeCheckBlockItem(BlockItem item) {
    return switch (item) {
      case VarDecl d -> typeCheckLocalVarDecl(d);
      case FunDecl d -> {
        if (d.hasStorageClass(StorageClass.STATIC)) {
          error("Can't have static storage class on block-scope function declaration of '%s'",
              d.name());
          yield null;
        }
        yield typeCheckFunDecl(d);
      }
      case Statement s -> typeCheckStatement(s);
      default -> throw new IllegalArgumentException("Unexpected value: " + item);
    };
  }

  private Statement typeCheckStatement(Statement s) {
    return switch (s) {
      case Expression e -> new Expression(typeCheckExp(e.exp()));
      case Return r -> typeCheckReturn(r);
      case If i -> typeCheckIf(i);
      case Compound c -> new Compound(typeCheckBlock(c.block()));
      case For f -> typeCheckFor(f);
      case While w -> typeCheckWhile(w);
      case DoWhile dw -> typeCheckDoWhile(dw);
      default -> s;
    };
  }

  private Return typeCheckReturn(Return r) {
    assert currentRetType != null;
    return new Return(convertTo(typeCheckExp(r.exp()), currentRetType));
  }

  // Page 254
  private static Type getCommonType(Type t1, Type t2) {
    if (t1.equals(t2)) {
      return t1;
    }
    return Type.LONG;
  }

  // Page 255
  private static Exp convertTo(Exp exp, Type desiredType) {
    if (exp.type().equals(desiredType)) {
      return exp;
    }
    return new Cast(desiredType, exp);
  }

  private DoWhile typeCheckDoWhile(DoWhile dw) {
    Statement checkedBody = typeCheckStatement(dw.body());
    Exp checkedExp = typeCheckExp(dw.condition());
    return new DoWhile(dw.label(), checkedBody, checkedExp);
  }

  private While typeCheckWhile(While w) {
    Exp newCond = typeCheckExp(w.condition());
    Statement newStatement = typeCheckStatement(w.body());
    return new While(w.label(), newCond, newStatement);
  }

  private For typeCheckFor(For f) {
    ForInit newForInit = typeCheckForInit(f.init());
    Optional<Exp> newCondition = f.condition().map(c -> typeCheckExp(c));
    Optional<Exp> newPost = f.post().map(p -> typeCheckExp(p));
    Statement newStatement = typeCheckStatement(f.body());
    return new For(f.label(), newForInit, newCondition, newPost, newStatement);
  }

  private ForInit typeCheckForInit(ForInit init) {
    switch (init) {
      case InitDecl id -> {
        VarDecl varDecl = typeCheckLocalVarDecl(id.decl());
        if (id.decl().storageClass().isPresent()) {
          error("Cannot include `extern` or `static` specifier in `for` loop header");
        }
        return new InitDecl(varDecl);
      }
      case InitExp ie -> {
        Optional<Exp> newExp = ie.exp().map(e -> typeCheckExp(e));
        return new InitExp(newExp);
      }
      default -> throw new IllegalArgumentException("Unexpected value: " + init);
    }
  }

  private If typeCheckIf(If i) {
    Exp newCondition = typeCheckExp(i.condition());
    Statement newThen = typeCheckStatement(i.then());
    Optional<Statement> newElse = i.elseStmt().map(stmt -> typeCheckStatement(stmt));
    return new If(newCondition, newThen, newElse);
  }

  private VarDecl typeCheckLocalVarDecl(VarDecl decl) {
    Optional<Exp> newInit = decl.init();
    if (decl.hasStorageClass(StorageClass.EXTERN)) {
      if (decl.init().isPresent()) {
        error("Cannot initialize `extern` variable '%s'", decl.name());
        return null;
      }
      Symbol oldDecl = symbols.get(decl.name());
      if (oldDecl != null) {
        if (!oldDecl.type().equals(decl.type())) {
          error("Conflicting types for '%s': originally declared as %s, then as %s",
              decl.name(), oldDecl.type(), decl.type());
          return null;
        }
      } else {
        Attribute attrs = new StaticAttr(InitialValue.NO_INITIALIZER, true);
        Symbol symbol = new Symbol(decl.name(), decl.type(), attrs);
        symbols.put(decl.name(), symbol);
      }
    } else if (decl.hasStorageClass(StorageClass.STATIC)) {
      InitialValue initialValue = InitialValue.NO_INITIALIZER;
      Optional<Long> maybeConst = getDeclInitialValue(decl);
      if (maybeConst.isPresent()) {
        initialValue = Initializer.of(maybeConst.get(), decl.type());
      } else if (decl.init().isEmpty()) {
        initialValue = Initializer.zeroOf(decl.type());
      } else {
        error("Non-constant iniitalizer on local static variable '%s'", decl.name());
        return null;
      }
      symbols.put(decl.name(),
          new Symbol(decl.name(), decl.type(), new StaticAttr(initialValue, false)));
    } else {
      symbols.put(decl.name(), new Symbol(decl.name(), decl.type(), Attribute.LOCAL_ATTR));
      newInit = decl.init().map(exp -> convertTo(typeCheckExp(exp), decl.type()));
    }
    return new VarDecl(decl.name(), decl.type(), newInit, decl.storageClass());
  }

  private Exp typeCheckExp(Exp e) {
    return switch (e) {
      case Assignment a -> typeCheckAssignment(a);
      case BinExp b -> typeCheckBinExp(b);
      case Conditional c -> typeCheckConditional(c);
      case FunctionCall fc -> typeCheckFunctionCall(fc);
      case UnaryExp u -> typeCheckUnaryExp(u);
      case Var v -> typeCheckVar(v);
      case Cast c -> new Cast(c.targetType(), typeCheckExp(c.exp()));
      default -> e;
    };
  }

  private Exp typeCheckUnaryExp(UnaryExp u) {
    Exp typedInner = typeCheckExp(u.exp());
    return switch (u.operator()) {
      case BANG -> new UnaryExp(u.operator(), typedInner, Type.INT);
      default -> new UnaryExp(u.operator(), typedInner, typedInner.type());
    };
  }

  private Exp typeCheckConditional(Conditional c) {
    Exp newCondition = typeCheckExp(c.condition());
    Exp newLeft = typeCheckExp(c.left());
    Exp newRight = typeCheckExp(c.right());
    Type commonType = getCommonType(newLeft.type(), newRight.type());

    return new Conditional(newCondition, convertTo(newLeft, commonType),
        convertTo(newRight, commonType), commonType);
  }

  // Page 255
  private Exp typeCheckBinExp(BinExp e) {
    Exp typedE1 = typeCheckExp(e.left());
    Exp typedE2 = typeCheckExp(e.right());
    if (e.operator() == TokenType.DOUBLE_AMP || e.operator() == TokenType.DOUBLE_BAR) {
      // 'and' and 'or' always become "int"
      return new BinExp(typedE1, e.operator(), typedE2, Type.INT);
    }
    Type commonType = getCommonType(typedE1.type(), typedE2.type());
    Exp convertedE1 = convertTo(typedE1, commonType);
    Exp convertedE2 = convertTo(typedE2, commonType);
    if (!ARITHMETIC_OPS.contains(e.operator())) {
      // Comparisons become INT; arithmetic (+ - / * %) retain the common type. 
      commonType = Type.INT;
    }
    return new BinExp(convertedE1, e.operator(), convertedE2, commonType);
  }

  // page 181, 253
  private Var typeCheckVar(Var v) {
    Symbol s = symbols.get(v.identifier());
    if (s == null) {
      error("Unknown symbol '%s'", v.identifier());
    }
    Type type = s.type();
    if (type instanceof FunType) {
      error("Function name %s used as variable", v.identifier());
      return null;
    }
    return new Var(v.identifier(), type);
  }

  // page 181, 256
  private FunctionCall typeCheckFunctionCall(FunctionCall fc) {
    Symbol s = symbols.get(fc.identifier());
    if (s == null) {
      error("Undeclared function '%s'", fc.identifier());
      return null;
    }
    Type sType = s.type();
    if (sType instanceof FunType ft) {
      if (ft.paramTypes().size() != fc.args().size()) {
        error("Function '%s' called with wrong number of params; saw %d, expected %d",
            fc.identifier(), fc.args().size(), ft.paramTypes().size());
        return null;
      }
      List<Exp> newArgs = new ArrayList<>();
      // For each arg, convert to param type
      for (int i = 0; i < fc.args().size(); ++i) {
        Exp newArg = typeCheckExp(fc.args().get(i));
        Exp convertedArg = convertTo(newArg, ft.paramTypes().get(i));
        newArgs.add(convertedArg);
      }
      return new FunctionCall(fc.identifier(), newArgs, ft.returnType());
    }
    error("Variable '%s' called as function", fc.identifier());
    return null;
  }

  // Page 256
  private Assignment typeCheckAssignment(Assignment a) {
    Exp newLvalue = typeCheckExp(a.lvalue());
    if (!(newLvalue instanceof Var)) {
      error("lvalues can only be variables; saw: %s", newLvalue);
    }
    Exp convertedRValue = convertTo(typeCheckExp(a.rvalue()), newLvalue.type());
    return new Assignment(newLvalue, convertedRValue, newLvalue.type());
  }
}
