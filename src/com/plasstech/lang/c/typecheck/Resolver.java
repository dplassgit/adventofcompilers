package com.plasstech.lang.c.typecheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.Declaration;
import com.plasstech.lang.c.parser.Exp;
import com.plasstech.lang.c.parser.Expression;
import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.NullStatement;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;

/**
 * Resolver for renaming variables and making sure there aren't duplicated declarations, and that
 * all used variables are declared. Based on Chapter 5, page 103ff.
 */

public class Resolver {
  // Maps from old to new name
  private final Map<String, String> variableMap = new HashMap<>();

  public Program validate(Program input) {
    FunctionDef functionDef = input.functionDef();
    return new Program(validate(functionDef));
  }

  private FunctionDef validate(FunctionDef functionDef) {
    List<BlockItem> resolvedBody = functionDef.body().stream().map(this::resolve).toList();
    return new FunctionDef(functionDef.name(), resolvedBody);
  }

  private BlockItem resolve(BlockItem item) {
    return switch (item) {
      case Declaration d -> resolveDeclaration(d);
      case Expression e -> resolveExpression(e);
      case Return r -> resolveReturn(r);
      case NullStatement n -> n;
      default -> throw new IllegalArgumentException("Unexpected value: " + item);
    };
  }

  private BlockItem resolveExpression(Expression e) {
    return new Expression(resolveExp(e.exp()));
  }

  private BlockItem resolveReturn(Return r) {
    return new Return(resolveExp(r.exp()));
  }

  private Declaration resolveDeclaration(Declaration d) {
    String name = d.identifier();
    if (variableMap.containsKey(name)) {
      error("Duplicate variable definition " + name);
    }
    String unique = makeTemp(name);
    variableMap.put(name, unique);
    Optional<Exp> init = d.init().map(this::resolveExp);
    return new Declaration(unique, init);
  }

  private Exp resolveExp(Exp e) {
    // Java 21 FTW
    return switch (e) {
      case Assignment a -> resolveAssignment(a);
      case BinExp b -> new BinExp(resolveExp(b.left()), b.operator(), resolveExp(b.right()));
      case Constant<?> c -> c;
      case UnaryExp u -> new UnaryExp(u.operator(), resolveExp(u.exp()));
      case Var v -> resolveVar(v);
      default -> throw new IllegalArgumentException("Unexpected value: " + e);
    };
  }

  private Exp resolveAssignment(Assignment a) {
    if (!(a.lvalue() instanceof Var)) {
      error("Invalid lvalue: " + a.lvalue());
    }
    return new Assignment(resolveExp(a.lvalue()), resolveExp(a.rvalue()));
  }

  private Exp resolveVar(Var v) {
    String mapped = variableMap.get(v.identifier());
    if (mapped != null) {
      return new Var(mapped);
    }
    error("Undeclared variable " + v.identifier());
    return null;
  }

  private static void error(String message) {
    throw new ResolverException(message);
  }

  private static int id;

  private static String makeTemp(String in) {
    return String.format("resolver.%s.%d", in, id++);
  }
}
