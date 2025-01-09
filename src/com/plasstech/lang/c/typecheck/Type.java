package com.plasstech.lang.c.typecheck;

public interface Type {
  String name();

  record IntType() implements Type {
    @Override
    public String name() {
      return "int";
    }
  }

  public Type Int = new IntType();
}
