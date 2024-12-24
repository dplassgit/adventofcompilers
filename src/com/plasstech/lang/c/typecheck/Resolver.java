package com.plasstech.lang.c.typecheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.plasstech.lang.c.parser.Assignment;
import com.plasstech.lang.c.parser.BinExp;
import com.plasstech.lang.c.parser.Block;
import com.plasstech.lang.c.parser.BlockItem;
import com.plasstech.lang.c.parser.Compound;
import com.plasstech.lang.c.parser.Conditional;
import com.plasstech.lang.c.parser.Constant;
import com.plasstech.lang.c.parser.Declaration;
import com.plasstech.lang.c.parser.Exp;
import com.plasstech.lang.c.parser.Expression;
import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.If;
import com.plasstech.lang.c.parser.NullStatement;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;
import com.plasstech.lang.c.parser.Statement;
import com.plasstech.lang.c.parser.UnaryExp;
import com.plasstech.lang.c.parser.Var;

/**
 * Resolver for renaming variables and making sure there aren't duplicated declarations, and that
 * all used variables are declared. Based on Chapter 5, page 103ff, Chapter 7, page 136ff
 */
public class Resolver {
  private record ScopedVariable(String variable, boolean sameScope) {
  }

  public Program validate(Program input) {
    FunctionDef functionDef = input.functionDef();
    return new Program(validate(functionDef));
  }

  private FunctionDef validate(FunctionDef functionDef) {
    // Maps from old to new name
    Map<String, ScopedVariable> variableMap = new HashMap<>();
    Block resolvedBlock = resolveBlock(functionDef.body(), variableMap);
    return new FunctionDef(functionDef.name(), resolvedBlock);
  }

  private Block resolveBlock(Block block, Map<String, ScopedVariable> variableMap) {
    List<BlockItem> resolvedItems =
        block.items().stream().map(item -> resolveBlockItem(item, variableMap)).toList();
    return new Block(resolvedItems);
  }

  private BlockItem resolveBlockItem(BlockItem item, Map<String, ScopedVariable> variableMap) {
    return switch (item) {
      case Declaration d -> resolveDeclaration(d, variableMap);
      case Statement s -> resolveStatement(s, variableMap);
      default -> throw new IllegalArgumentException("Unexpected block item: " + item);
    };
  }

  private Statement resolveStatement(Statement statement, Map<String, ScopedVariable> variableMap) {
    return switch (statement) {
      case Expression e -> new Expression(resolveExp(e.exp(), variableMap));
      case Return r -> new Return(resolveExp(r.exp(), variableMap));
      case If i -> resolveIf(i, variableMap);
      case NullStatement n -> n;
      case Compound c -> new Compound(resolveBlock(c.block(), copy(variableMap)));
      default -> throw new IllegalArgumentException("Unexpected statement type: " + statement);
    };
  }

  private static Map<String, ScopedVariable> copy(Map<String, ScopedVariable> variableMap) {
    // Copy variable map with the from curent block flag set to false. Page 139
    Map<String, ScopedVariable> copy = new HashMap<>();
    for (Map.Entry<String, ScopedVariable> entry : variableMap.entrySet()) {
      copy.put(entry.getKey(), new ScopedVariable(entry.getValue().variable, false));
    }
    return copy;
  }

  private Statement resolveIf(If i, Map<String, ScopedVariable> variableMap) {
    Exp cond = resolveExp(i.condition(), variableMap);
    Statement then = resolveStatement(i.then(), variableMap);
    Optional<Statement> elseStmt = i.elseStmt().map(stmt -> resolveStatement(stmt, variableMap));
    return new If(cond, then, elseStmt);
  }

  private Declaration resolveDeclaration(Declaration d, Map<String, ScopedVariable> variableMap) {
    String name = d.identifier();
    ScopedVariable variable = variableMap.get(name);
    if (variable != null && variable.sameScope()) {
      error("Duplicate variable definition " + name);
    }
    String unique = makeTemp(name);
    variableMap.put(name, new ScopedVariable(unique, true));
    Optional<Exp> init = d.init().map(exp -> resolveExp(exp, variableMap));
    return new Declaration(unique, init);
  }

  private Exp resolveExp(Exp e, Map<String, ScopedVariable> variableMap) {
    // Java 21 FTW
    return switch (e) {
      case Assignment a -> resolveAssignment(a, variableMap);
      case BinExp b -> new BinExp(resolveExp(b.left(), variableMap), b.operator(),
          resolveExp(b.right(), variableMap));
      case Constant<?> c -> c;
      case UnaryExp u -> new UnaryExp(u.operator(), resolveExp(u.exp(), variableMap));
      case Var v -> resolveVar(v, variableMap);
      case Conditional c ->
        new Conditional(resolveExp(c.condition(), variableMap), resolveExp(c.left(), variableMap),
            resolveExp(c.right(), variableMap));
      default -> throw new IllegalArgumentException("Unexpected value: " + e);
    };
  }

  private Exp resolveAssignment(Assignment a, Map<String, ScopedVariable> variableMap) {
    if (!(a.lvalue() instanceof Var)) {
      error("lvalues can only be variables; saw: " + a.lvalue());
    }
    return new Assignment(resolveExp(a.lvalue(), variableMap), resolveExp(a.rvalue(), variableMap));
  }

  private Exp resolveVar(Var v, Map<String, ScopedVariable> variableMap) {
    ScopedVariable mapped = variableMap.get(v.identifier());
    if (mapped != null) {
      return new Var(mapped.variable());
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
