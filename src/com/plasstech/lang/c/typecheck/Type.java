package com.plasstech.lang.c.typecheck;

public interface Type {
  record IntType() implements Type {
  }

  public Type Int = new IntType();
}
