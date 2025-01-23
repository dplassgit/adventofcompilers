package com.plasstech.lang.c.typecheck;

import com.plasstech.lang.c.parser.Program;

public class SemanticAnalyzer implements Validator {
  private final SymbolTable symbols;

  public SemanticAnalyzer() {
    this(new SymbolTable());
  }

  public SemanticAnalyzer(SymbolTable symbols) {
    this.symbols = symbols;
  }

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
