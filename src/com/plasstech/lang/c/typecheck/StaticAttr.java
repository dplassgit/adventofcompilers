package com.plasstech.lang.c.typecheck;

public record StaticAttr(InitialValue init, boolean global) implements Attribute {
  @Override
  public boolean isGlobal() {
    return global();
  }

  @Override
  public boolean defined() {
    return true;
  }
}
