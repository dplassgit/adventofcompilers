package com.plasstech.lang.c.typecheck;

import com.plasstech.lang.c.parser.Program;

public interface Validator {
  Program validate(Program program);

  default void error(String message, Object... params) {
    throw new SemanticAnalyzerException(String.format(message, params));
  }
}
