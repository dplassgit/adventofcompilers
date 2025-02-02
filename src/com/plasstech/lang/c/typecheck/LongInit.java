package com.plasstech.lang.c.typecheck;

public record LongInit(long value) implements StaticInit {
  @Override
  public long valueAsLong() {
    return value;
  }

  @Override
  public int bytes() {
    return 8;
  }

  @Override
  public String name() {
    return "quad";
  }
}
