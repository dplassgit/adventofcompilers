package com.plasstech.lang.c.typecheck;

public record LongInit(long value) implements StaticInit {
  @Override
  public long valueAsLong() {
    return value;
  }
}
