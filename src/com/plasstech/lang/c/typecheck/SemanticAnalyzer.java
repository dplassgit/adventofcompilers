package com.plasstech.lang.c.typecheck;

import com.plasstech.lang.c.parser.Program;

public class SemanticAnalyzer implements Validator {
  @Override
  public Program validate(Program program) {
    return new Resolver().validate(program);
  }
}
