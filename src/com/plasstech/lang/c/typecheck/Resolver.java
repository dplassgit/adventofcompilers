package com.plasstech.lang.c.typecheck;

import com.plasstech.lang.c.parser.FunctionDef;
import com.plasstech.lang.c.parser.Program;

public class Resolver {

  public Program validate(Program input) {
    FunctionDef functionDef = input.functionDef();
    return new Program(validate(functionDef));
  }

  private FunctionDef validate(FunctionDef functionDef) {
    return functionDef;
  }
}
