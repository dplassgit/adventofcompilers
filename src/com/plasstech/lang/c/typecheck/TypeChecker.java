package com.plasstech.lang.c.typecheck;

import java.util.List;
import java.util.Optional;

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
      // already defined somehow
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
    Optional<Integer> initialInt = getDeclInitialValue(decl);
    if (initialInt.isPresent()) {
      initialValue = new Initializer(initialInt.get());
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
      if (!oldDecl.type().equals(Type.INT)) {
        error("Function '%s' redeclared as variable", decl.name());
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
        new Symbol(decl.name(), Type.INT, new StaticAttr(initialValue, global)));
    // TODO: FIX ME
    return decl;
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

  private Optional<Integer> getDeclInitialValue(VarDecl decl) {
    if (decl.init().isEmpty()) {
      return Optional.empty();
    }
    Exp e = decl.init().get();
    return switch (e) {
      // TODO: This must change for longs
      case Constant<?> ci -> Optional.of(ci.asInt());
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

  private static Type getCommonType(Type t1, Type t2) {
    if (t1.equals(t2)) {
      return t1;
    }
    return Type.LONG;
  }

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
    typeCheckExp(w.condition());
    typeCheckStatement(w.body());
    // TODO: fixme
    return w;
  }

  private For typeCheckFor(For f) {
    typeCheckForInit(f.init());
    f.condition().ifPresent(c -> typeCheckExp(c));
    f.post().ifPresent(p -> typeCheckExp(p));
    typeCheckStatement(f.body());
    // TODO: fixme
    return f;
  }

  private ForInit typeCheckForInit(ForInit init) {
    switch (init) {
      case InitDecl id -> {
        typeCheckLocalVarDecl(id.decl());
        if (id.decl().storageClass().isPresent()) {
          error("Cannot include `extern` or `static` specifier in for loop header");
        }
      }
      case InitExp ie -> ie.exp().ifPresent(e -> typeCheckExp(e));
      default -> throw new IllegalArgumentException("Unexpected value: " + init);
    }
    // TODO: fixme
    return init;
  }

  private If typeCheckIf(If i) {
    typeCheckExp(i.condition());
    typeCheckStatement(i.then());
    i.elseStmt().ifPresent(stmt -> typeCheckStatement(stmt));
    // TODO: fixme
    return i;
  }

  private VarDecl typeCheckLocalVarDecl(VarDecl decl) {
    if (decl.hasStorageClass(StorageClass.EXTERN)) {
      if (decl.init().isPresent()) {
        error("Cannot initialize `extern` variable '%s'", decl.name());
        return null;
      }
      Symbol oldDecl = symbols.get(decl.name());
      if (oldDecl != null) {
        if (!oldDecl.type().equals(Type.INT)) {
          error("Function '%s' redeclared as variable", decl.name());
          return null;
        }
      } else {
        Attribute attrs = new StaticAttr(InitialValue.NO_INITIALIZER, true);
        Symbol symbol = new Symbol(decl.name(), Type.INT, attrs);
        symbols.put(decl.name(), symbol);
      }
    } else if (decl.hasStorageClass(StorageClass.STATIC)) {
      InitialValue initialValue = InitialValue.NO_INITIALIZER;
      Optional<Integer> maybeConst = getDeclInitialValue(decl);
      if (maybeConst.isPresent()) {
        initialValue = new Initializer(maybeConst.get());
      } else if (decl.init().isEmpty()) {
        initialValue = new Initializer(0); // ??!?
      } else {
        error("Non-constant iniitalizer on local static variable '%s'", decl.name());
        return null;
      }
      symbols.put(decl.name(),
          new Symbol(decl.name(), Type.INT, new StaticAttr(initialValue, false)));
    } else {
      symbols.put(decl.name(), new Symbol(decl.name(), Type.INT, Attribute.LOCAL_ATTR));
      decl.init().ifPresent(exp -> typeCheckExp(exp));
    }
    // TODO: FIX ME
    return decl;
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
    typeCheckExp(c.condition());
    typeCheckExp(c.left());
    typeCheckExp(c.right());
    // TODO: fixme
    return c;
  }

  // Page 255
  private Exp typeCheckBinExp(BinExp e) {
    typeCheckExp(e.left());
    typeCheckExp(e.right());
    // TODO: fixme
    return e;
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
    if (!(s.type() instanceof FunType)) {
      error("Variable '%s' called as function", fc.identifier());
      return null;
    }
    FunType ft = (FunType) s.type();
    if (ft.paramTypes().size() != fc.args().size()) {
      error("Function '%s' called with wrong number of params; saw %d, expected %d",
          fc.identifier(), fc.args().size(), ft.paramTypes().size());
      return null;
    }
    fc.args().stream().forEach(arg -> typeCheckExp(arg));
    // TODO: FIXME
    return fc;
  }

  // Page 256
  private Assignment typeCheckAssignment(Assignment a) {
    typeCheckExp(a.lvalue());
    typeCheckExp(a.rvalue());
    // TODO: FIXME
    return a;
  }
}
