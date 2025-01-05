package com.plasstech.lang.c.typecheck;

import java.util.HashMap;
import java.util.Map;

import com.plasstech.lang.c.parser.Program;

public class SemanticAnalyzer implements Validator {
  private final Map<String, Symbol> symbols = new HashMap<>();

  @Override
  public Program validate(Program program) {
    Validator resolver = new Resolver();
    program = resolver.validate(program);
    Validator loopLabeler = new LoopLabeler();
    program = loopLabeler.validate(program);
    Validator typeChecker = new TypeChecker(symbols);
    program = typeChecker.validate(program);
    return program;
  }

  public Map<String, Symbol> symbolTable() {
    return symbols;
  }
}
