package com.plasstech.lang.c.typecheck;

import java.util.Map;

import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.Block;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Compound;
import com.plasstech.lang.c.parser.Conditional;
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
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;
import com.plasstech.lang.c.parser.VarDecl;
import com.plasstech.lang.c.parser.While;

/** Finally, a type checker. Starts on page 178. */
public class TypeChecker implements Validator {

  private final Map<String, Symbol> symbols;

  public TypeChecker(Map<String, Symbol> symbols) {
    this.symbols = symbols;
  }

  @Override
  public Program validate(Program program) {
    program.funDecls().stream().forEach(this::typeCheckFunctionDeclaration);
    return program;
  }

  // Page 180, listing 9-21.
  private void typeCheckFunctionDeclaration(FunDecl decl) {
    Type funType = new FunType(decl.params().size());
    boolean hasBody = decl.body().isPresent();
    boolean alreadyDefined = false;
    Symbol oldDecl = symbols.get(decl.name());
    if (oldDecl != null) {
      // already defined somehow
      if (!oldDecl.type().equals(funType)) {
        error("Incompatible function declarations; " + decl.name() + " already defined as "
            + oldDecl.toString());
        return;
      }
      alreadyDefined = oldDecl.defined();
      if (alreadyDefined && hasBody) {
        error("Function " + decl.name() + " defined more than once");
        return;
      }
    }
    symbols.put(decl.name(), new Symbol(decl.name(), funType, alreadyDefined || hasBody));
    if (hasBody) {
      decl.params().forEach(paramName -> symbols.put(paramName, new Symbol(paramName, Type.Int)));
      typeCheckBlock(decl.body().get());
    }
  }

  private void typeCheckBlock(Block block) {
    block.items().forEach(this::typeCheckBlockItem);
  }

  private void typeCheckBlockItem(BlockItem item) {
    switch (item) {
      case VarDecl d -> typeCheckVarDecl(d);
      case FunDecl d -> {
        typeCheckFunctionDeclaration(d);
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
      case InitDecl id -> typeCheckVarDecl(id.decl());
      case InitExp ie -> ie.exp().ifPresent(e -> typeCheckExp(e));
      default -> throw new IllegalArgumentException("Unexpected value: " + init);
    }
  }

  private void typeCheckIf(If i) {
    typeCheckExp(i.condition());
    typeCheckStatement(i.then());
    i.elseStmt().ifPresent(stmt -> typeCheckStatement(stmt));
  }

  private void typeCheckVarDecl(VarDecl decl) {
    symbols.put(decl.identifier(), new Symbol(decl.identifier(), Type.Int));
    decl.init().ifPresent(exp -> typeCheckExp(exp));
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
    if (!s.type().equals(Type.Int)) {
      error("Function name " + v.identifier() + " used as variable");
    }
  }

  // page 181 (listing 9-22)
  private void typeCheckFunctionCall(FunctionCall fc) {
    Symbol s = symbols.get(fc.identifier());
    if (s == null) {
      error("Undeclared function " + fc.identifier());
      return;
    }
    if (!(s.type() instanceof FunType)) {
      error("Variable " + fc.identifier() + " called as function");
      return;
    }
    FunType ft = (FunType) s.type();
    if (ft.paramCount() != fc.args().size()) {
      error(String.format("Function %s called with wrong number of params; saw %d, expected %d",
          fc.identifier(), fc.args().size(), ft.paramCount()));
      return;
    }
    fc.args().stream().forEach(arg -> typeCheckExp(arg));
  }

  private void typeCheckAssignment(Assignment a) {
    typeCheckExp(a.lvalue());
    typeCheckExp(a.rvalue());
  }
}
