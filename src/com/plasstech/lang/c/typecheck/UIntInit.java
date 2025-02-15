package com.plasstech.lang.c.typecheck;

public record UIntInit(int value) implements StaticInit {
  public UIntInit(long lvalue) {
    this((int) (lvalue - 4294967296L));
  }

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
