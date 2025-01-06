package com.plasstech.lang.c.typecheck;

import com.plasstech.lang.c.parser.Program;

public class SemanticAnalyzer implements Validator {
  private final SymbolTable symbols = new SymbolTable();

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

  public SymbolTable symbolTable() {
    return symbols;
  }
}
