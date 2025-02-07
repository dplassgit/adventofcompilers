package com.plasstech.lang.c.typecheck;

/**
 * Represents the initialized value of a file-scope variable declaration.
 */
public record Initializer(StaticInit staticInit) implements InitialValue {
  public static Initializer of(long value, Type type) {
    if (type.equals(Type.INT)) {
      return new Initializer(new IntInit((int) value));
    }
    if (type.equals(Type.LONG)) {
      return new Initializer(new LongInit(value));
    }
    // Not sure if this is right. Page 280
    if (type.equals(Type.UNSIGNED_INT)) {
      return new Initializer(new UIntInit((int) value));
    }
    if (type.equals(Type.UNSIGNED_LONG)) {
      return new Initializer(new ULongInit(value));
    }
    throw new IllegalStateException("Cannot create of type " + type.toString());
  }

  public static InitialValue zeroOf(Type type) {
    return of(0L, type);
  }
}
