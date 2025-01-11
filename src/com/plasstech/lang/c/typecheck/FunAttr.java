package com.plasstech.lang.c.typecheck;

public record FunAttr(boolean defined, boolean global) implements Attribute {
  @Override
  public boolean isGlobal() {
    return global();
  }
}
