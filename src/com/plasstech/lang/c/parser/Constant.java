package com.plasstech.lang.c.parser;

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.plasstech.lang.c.typecheck.Type;

public record Constant<T extends Number>(T value, Type type) implements Exp {
  public static Constant<Integer> of(int value) {
    return new Constant<Integer>(value, Type.INT);
  }

  public static Constant<Long> of(long value) {
    return new Constant<Long>(value, Type.LONG);
  }

  public static Constant<UnsignedInteger> ofUnsignedInt(String value) {
    return new Constant<UnsignedInteger>(UnsignedInteger.valueOf(value), Type.UNSIGNED_INT);
  }

  public static Constant<UnsignedLong> ofUnsignedLong(String value) {
    return new Constant<UnsignedLong>(UnsignedLong.valueOf(value), Type.UNSIGNED_LONG);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return value.toString();
  }

  public int asInt() {
    Preconditions.checkState(type.equals(Type.INT));
    return value.intValue();
  }

  public long asLong() {
    return value.longValue();
  }
}
