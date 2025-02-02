package com.plasstech.lang.c.typecheck;

public record IntInit(int value) implements StaticInit {
  @Override
  public long valueAsLong() {
    return value;
  }

  @Override
  public int bytes() {
    return 4;
  }

  @Override
  public String name() {
    return "long";
  }
}
