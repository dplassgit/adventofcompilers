package com.plasstech.lang.c.typecheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.plasstech.lang.c.common.UniqueId;
import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.Block;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Break;
import com.plasstech.lang.c.parser.Compound;
import com.plasstech.lang.c.parser.Conditional;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.Continue;
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
import com.plasstech.lang.c.parser.NullStatement;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.Statement;
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;
import com.plasstech.lang.c.parser.VarDecl;
import com.plasstech.lang.c.parser.While;

/**
 * Resolver for renaming variables and making sure there aren't duplicated declarations, and that
 * all used variables are declared. Based on Chapter 5, page 103ff, Chapter 7, page 136ff
 */
class Resolver implements Validator {
  private record ScopedIdentifier(String variable, boolean sameScope, boolean hasLinkage) {
    public ScopedIdentifier(String variable, boolean sameScope) {
      // Local variables have no linkage
      this(variable, sameScope, false);
    }
  }

  @Override
  public Program validate(Program input) {
    Map<String, ScopedIdentifier> identifierMap = new HashMap<>();
    return new Program(input.declarations().stream()
        .map(fd -> resolveFunctionDeclaration(fd, identifierMap)).toList());
  }

  // Page 176
  private FunDecl resolveFunctionDeclaration(FunDecl decl,
      Map<String, ScopedIdentifier> identifierMap) {
    ScopedIdentifier scopedIdentifier = identifierMap.get(decl.name());
    if (scopedIdentifier != null) {
      if (scopedIdentifier.sameScope() && !scopedIdentifier.hasLinkage()) {
        error("Duplicate function declaration of " + decl.name());
      }
    }
    identifierMap.put(decl.name(), new ScopedIdentifier(decl.name(), true, true));
    Map<String, ScopedIdentifier> innerMap = copy(identifierMap);
    List<String> newParams =
        decl.params().stream().map(param -> resolveParam(param, innerMap)).toList();
    Optional<Block> newBlock = decl.body().map(oldBlock -> resolveBlock(oldBlock, innerMap));
    return new FunDecl(decl.name(), newParams, newBlock);
  }

  private String resolveParam(String name, Map<String, ScopedIdentifier> identifierMap) {
    ScopedIdentifier variable = identifierMap.get(name);
    if (variable != null && variable.sameScope()) {
      error("Duplicate parameter definition " + name);
    }
    String newName = UniqueId.makeUnique("resolved_param_" + name);
    identifierMap.put(name, new ScopedIdentifier(newName, true));
    return newName;
  }

  private Block resolveBlock(Block block, Map<String, ScopedIdentifier> identifierMap) {
    List<BlockItem> resolvedItems =
        block.items().stream().map(item -> resolveBlockItem(item, identifierMap)).toList();
    return new Block(resolvedItems);
  }

  private BlockItem resolveBlockItem(BlockItem item, Map<String, ScopedIdentifier> identifierMap) {
    return switch (item) {
      case VarDecl d -> resolveVarDecl(d, identifierMap);
      case FunDecl d -> {
        if (d.body().isPresent()) {
          error("Cannot define nested function " + d.name());
        }

        yield resolveFunctionDeclaration(d, identifierMap);
      }
      case Statement s -> resolveStatement(s, identifierMap);
      default -> throw new IllegalArgumentException("Unexpected block item: " + item);
    };
  }

  private Statement resolveStatement(Statement statement,
      Map<String, ScopedIdentifier> identifierMap) {
    return switch (statement) {
      case Expression e -> new Expression(resolveExp(e.exp(), identifierMap));
      case Return r -> new Return(resolveExp(r.exp(), identifierMap));
      case If i -> resolveIf(i, identifierMap);
      case NullStatement n -> n;
      case Compound c -> new Compound(resolveBlock(c.block(), copy(identifierMap)));
      case For f -> resolveFor(f, identifierMap);
      case While w -> resolveWhile(w, identifierMap);
      case DoWhile dw -> resolveDoWhile(dw, identifierMap);
      case Break b -> b;
      case Continue c -> c;
      default -> throw new IllegalArgumentException("Unexpected statement type: " + statement);
    };
  }

  private DoWhile resolveDoWhile(DoWhile dw, Map<String, ScopedIdentifier> identifierMap) {
    // Do we need to create a new scope?!
    return new DoWhile(resolveStatement(dw.body(), identifierMap),
        resolveExp(dw.condition(), identifierMap));
  }

  private While resolveWhile(While w, Map<String, ScopedIdentifier> identifierMap) {
    return new While(resolveExp(w.condition(), identifierMap),
        resolveStatement(w.body(), identifierMap));
  }

  private For resolveFor(For f, Map<String, ScopedIdentifier> identifierMap) {
    Map<String, ScopedIdentifier> newMap = copy(identifierMap);
    return new For(
        resolveForInit(f.init(), newMap),
        f.condition().map(c -> resolveExp(c, newMap)),
        f.post().map(p -> resolveExp(p, newMap)),
        resolveStatement(f.body(), newMap));
  }

  private ForInit resolveForInit(ForInit init, Map<String, ScopedIdentifier> newMap) {
    return switch (init) {
      case InitDecl id -> new InitDecl(resolveVarDecl(id.decl(), newMap));
      case InitExp ie -> new InitExp(ie.exp().map(e -> resolveExp(e, newMap)));
      default -> throw new IllegalArgumentException("Unexpected value: " + init);
    };
  }

  private static Map<String, ScopedIdentifier> copy(Map<String, ScopedIdentifier> identifierMap) {
    // Copy variable map with the from current block flag set to false. Page 139
    Map<String, ScopedIdentifier> copy = new HashMap<>();
    for (Map.Entry<String, ScopedIdentifier> entry : identifierMap.entrySet()) {
      copy.put(entry.getKey(), new ScopedIdentifier(entry.getValue().variable, false));
    }
    return copy;
  }

  private Statement resolveIf(If i, Map<String, ScopedIdentifier> identifierMap) {
    Exp cond = resolveExp(i.condition(), identifierMap);
    Statement then = resolveStatement(i.then(), identifierMap);
    Optional<Statement> elseStmt = i.elseStmt().map(stmt -> resolveStatement(stmt, identifierMap));
    return new If(cond, then, elseStmt);
  }

  private VarDecl resolveVarDecl(VarDecl vd, Map<String, ScopedIdentifier> identifierMap) {
    String name = vd.identifier();
    ScopedIdentifier variable = identifierMap.get(name);
    if (variable != null && variable.sameScope()) {
      error("Duplicate variable definition " + name);
    }
    String unique = UniqueId.makeUnique("resolved_var_" + name);
    identifierMap.put(name, new ScopedIdentifier(unique, true));
    Optional<Exp> init = vd.init().map(exp -> resolveExp(exp, identifierMap));
    return new VarDecl(unique, init);
  }

  private Exp resolveExp(Exp e, Map<String, ScopedIdentifier> identifierMap) {
    // Java 21 FTW
    return switch (e) {
      case Assignment a -> resolveAssignment(a, identifierMap);
      case BinExp b -> new BinExp(resolveExp(b.left(), identifierMap), b.operator(),
          resolveExp(b.right(), identifierMap));
      case Constant<?> c -> c;
      case UnaryExp u -> new UnaryExp(u.operator(), resolveExp(u.exp(), identifierMap));
      case Var v -> resolveVar(v, identifierMap);
      case Conditional c ->
        new Conditional(resolveExp(c.condition(), identifierMap),
            resolveExp(c.left(), identifierMap),
            resolveExp(c.right(), identifierMap));
      case FunctionCall fc -> resolveFunctionCall(fc, identifierMap);
      default -> throw new IllegalArgumentException("Unexpected value: " + e);
    };
  }

  // Page 175
  private Exp resolveFunctionCall(FunctionCall fc, Map<String, ScopedIdentifier> identifierMap) {
    ScopedIdentifier mapped = identifierMap.get(fc.identifier());
    if (mapped != null) {
      // It's in the map
      String newName = mapped.variable();
      List<Exp> newArgs =
          fc.args().stream().map(arg -> resolveExp(arg, identifierMap)).toList();
      return new FunctionCall(newName, newArgs);
    }
    // Not in the map. Never declared, or it's not a function.
    error("Undeclared function " + fc.identifier());
    return null;
  }

  private Exp resolveAssignment(Assignment a, Map<String, ScopedIdentifier> identifierMap) {
    if (!(a.lvalue() instanceof Var)) {
      error("lvalues can only be variables; saw: " + a.lvalue());
    }
    return new Assignment(resolveExp(a.lvalue(), identifierMap),
        resolveExp(a.rvalue(), identifierMap));
  }

  private Exp resolveVar(Var v, Map<String, ScopedIdentifier> identifierMap) {
    ScopedIdentifier mapped = identifierMap.get(v.identifier());
    if (mapped != null) {
      return new Var(mapped.variable());
    }
    error("Undeclared variable " + v.identifier());
    return null;
  }
}
