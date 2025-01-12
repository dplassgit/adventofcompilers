package com.plasstech.lang.c.typecheck;

import java.util.Optional;

import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.Block;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Compound;
import com.plasstech.lang.c.parser.Conditional;
import com.plasstech.lang.c.parser.Constant;
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

/** Finally, a type checker. Starts on page 178. */
public class TypeChecker implements Validator {

  private final SymbolTable symbols;

  public TypeChecker(SymbolTable symbols) {
    this.symbols = symbols;
  }

  @Override
  public Program validate(Program program) {
    program.declarations().stream().forEach(d -> {
      switch (d) {
        case FunDecl fd -> typeCheckFunDecl(fd);
        case VarDecl vd -> typeCheckFileScopeVarDecl(vd);
        default -> throw new IllegalArgumentException("Unexpected value: " + d);
      }
    });
    return program;
  }

  // Page 180, 230
  private void typeCheckFunDecl(FunDecl decl) {
    boolean hasBody = decl.body().isPresent();
    if (decl.hasStorageClass(StorageClass.EXTERN) && hasBody) {
      error("Cannot define `extern` function '%s'", decl.name());
    }
    boolean global = !decl.hasStorageClass(StorageClass.STATIC);

    // ??? is this right?
    Type funType = decl.funType();// new FunType(decl.params().size());
    boolean alreadyDefined = false;
    Symbol oldDecl = symbols.get(decl.name());
    if (oldDecl != null) {
      // already defined somehow
      if (!oldDecl.type().equals(funType)) {
        error("Incompatible function declarations; '%s' already defined as '%s'",
            decl.name(), oldDecl.toString());
        return;
      }
      alreadyDefined = oldDecl.attribute().defined();
      if (alreadyDefined && hasBody) {
        error("Function '%s' defined more than once", decl.name());
        return;
      }
      global = oldDecl.attribute().isGlobal();
      if (oldDecl.attribute().isGlobal() && decl.hasStorageClass(StorageClass.STATIC)) {
        error("Static function declaration folllows non-static");
      }
    }
    Attribute attr = new FunAttr(alreadyDefined || hasBody, global);
    symbols.put(decl.name(), new Symbol(decl.name(), funType, attr));
    if (hasBody) {
      // Defaults to ints as parameter types
      decl.params().forEach(
          paramName -> symbols.put(paramName, new Symbol(paramName, Type.INT,
              Attribute.LOCAL_ATTR)));
      typeCheckBlock(decl.body().get());
    }
  }

  // Page 231
  private void typeCheckFileScopeVarDecl(VarDecl decl) {
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
    }

    boolean global = !decl.hasStorageClass(StorageClass.STATIC);

    Symbol oldDecl = symbols.get(decl.name());
    if (oldDecl != null) {
      if (!oldDecl.type().equals(Type.INT)) {
        error("Function '%s' redeclared as variable", decl.name());
      }
      if (decl.hasStorageClass(StorageClass.EXTERN)) {
        global = oldDecl.attribute().isGlobal();
      } else if (oldDecl.attribute().isGlobal() != global) {
        error("Conflicting variable linkage for '%s'", decl.name());
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
      case Constant<?> ci -> Optional.of(ci.asInt());
      default -> Optional.empty();
    };
  }

  private void typeCheckBlock(Block block) {
    block.items().forEach(this::typeCheckBlockItem);
  }

  private void typeCheckBlockItem(BlockItem item) {
    switch (item) {
      case VarDecl d -> typeCheckLocalVarDecl(d);
      case FunDecl d -> {
        typeCheckFunDecl(d);
        if (d.hasStorageClass(StorageClass.STATIC)) {
          error("Can't have static storage class on block-scope function declaration of '%s'",
              d.name());
        }
      }
      case Statement s -> typeCheckStatement(s);
      default -> {
      }
    }
  }

  private void typeCheckStatement(Statement statement) {
    switch (statement) {
      case Expression e -> typeCheckExp(e.exp());
      case Return r -> typeCheckExp(r.exp());
      case If i -> typeCheckIf(i);
      case Compound c -> typeCheckBlock(c.block());
      case For f -> typeCheckFor(f);
      case While w -> typeCheckWhile(w);
      case DoWhile dw -> typeCheckDoWhile(dw);
      default -> {
      }
    }
  }

  private void typeCheckDoWhile(DoWhile dw) {
    typeCheckExp(dw.condition());
  }

  private void typeCheckWhile(While w) {
    typeCheckExp(w.condition());
    typeCheckStatement(w.body());
  }

  private void typeCheckFor(For f) {
    typeCheckForInit(f.init());
    f.condition().ifPresent(c -> typeCheckExp(c));
    f.post().ifPresent(p -> typeCheckExp(p));
    typeCheckStatement(f.body());
  }

  private void typeCheckForInit(ForInit init) {
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
  }

  private void typeCheckIf(If i) {
    typeCheckExp(i.condition());
    typeCheckStatement(i.then());
    i.elseStmt().ifPresent(stmt -> typeCheckStatement(stmt));
  }

  private void typeCheckLocalVarDecl(VarDecl decl) {
    if (decl.hasStorageClass(StorageClass.EXTERN)) {
      if (decl.init().isPresent()) {
        error("Cannot initialize `extern` variable '%s'", decl.name());
      }
      Symbol oldDecl = symbols.get(decl.name());
      if (oldDecl != null) {
        if (!oldDecl.type().equals(Type.INT)) {
          error("Function '%s' redeclared as variable", decl.name());
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
      }
      symbols.put(decl.name(),
          new Symbol(decl.name(), Type.INT, new StaticAttr(initialValue, false)));
    } else {
      symbols.put(decl.name(), new Symbol(decl.name(), Type.INT, Attribute.LOCAL_ATTR));
      decl.init().ifPresent(exp -> typeCheckExp(exp));
    }
  }

  private void typeCheckExp(Exp e) {
    switch (e) {
      case Assignment a -> typeCheckAssignment(a);
      case BinExp b -> {
        typeCheckExp(b.left());
        typeCheckExp(b.right());
      }
      case Conditional c -> {
        typeCheckExp(c.condition());
        typeCheckExp(c.left());
        typeCheckExp(c.right());
      }
      case FunctionCall fc -> typeCheckFunctionCall(fc);
      case UnaryExp u -> typeCheckExp(u.exp());
      case Var v -> typeCheckVar(v);
      default -> {
      }
    }
  }

  // page 181 (listing 9-22)
  private void typeCheckVar(Var v) {
    Symbol s = symbols.get(v.identifier());
    if (s == null) {
      return;
    }
    if (!s.type().equals(Type.INT)) {
      error("Function name " + v.identifier() + " used as variable");
    }
  }

  // page 181 (listing 9-22)
  private void typeCheckFunctionCall(FunctionCall fc) {
    Symbol s = symbols.get(fc.identifier());
    if (s == null) {
      error("Undeclared function '%s'", fc.identifier());
      return;
    }
    if (!(s.type() instanceof FunType)) {
      error("Variable '%s' called as function", fc.identifier());
      return;
    }
    FunType ft = (FunType) s.type();
    if (ft.params().size() != fc.args().size()) {
      error("Function '%s' called with wrong number of params; saw %d, expected %d",
          fc.identifier(), fc.args().size(), ft.params().size());
      return;
    }
    fc.args().stream().forEach(arg -> typeCheckExp(arg));
  }

  private void typeCheckAssignment(Assignment a) {
    typeCheckExp(a.lvalue());
    typeCheckExp(a.rvalue());
  }
}
