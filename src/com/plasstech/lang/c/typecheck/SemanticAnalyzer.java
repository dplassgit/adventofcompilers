package com.plasstech.lang.c.typecheck;

import com.plasstech.lang.c.parser.Program;

public class SemanticAnalyzer implements Validator {
  @Override
  public Program validate(Program program) {
    Validator resolver = new Resolver();
    program = resolver.validate(program);
    Validator loopLabeler = new LoopLabeler();
    program = loopLabeler.validate(program);
    return program;
  }
}
