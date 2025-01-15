package com.plasstech.lang.c.typecheck;

public record IntInit(int value) implements StaticInit {
  @Override
  public long valueAsLong() {
    return value;
  }
}
