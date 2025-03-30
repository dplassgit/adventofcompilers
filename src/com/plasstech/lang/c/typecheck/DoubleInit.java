package com.plasstech.lang.c.typecheck;

public record DoubleInit(double value) implements StaticInit {

  @Override
  public int bytes() {
    return 0;
  }

  @Override
  public double valueAsDouble() {
    return value;
  }

  @Override
  public long valueAsLong() {
    return (long) value;
  }

  @Override
  public String name() {
    return "double"; // is this right?
  }
}
