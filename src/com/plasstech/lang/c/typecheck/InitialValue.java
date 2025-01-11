package com.plasstech.lang.c.typecheck;

public interface InitialValue {
  InitialValue TENTATIVE = new InitialValue() {};
  InitialValue NO_INITIALIZER = new InitialValue() {};
}
