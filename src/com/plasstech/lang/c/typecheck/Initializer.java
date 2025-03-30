package com.plasstech.lang.c.typecheck;

/**
 * Represents the initialized value of a file-scope variable declaration.
 */
public record Initializer(StaticInit staticInit) implements InitialValue {
  public static Initializer of(Number value, Type type) {
    if (type.equals(Type.INT)) {
      return new Initializer(new IntInit(value.longValue()));
    }
    if (type.equals(Type.LONG)) {
      return new Initializer(new LongInit(value.longValue()));
    }
    // Not sure if this is right. Page 280
    if (type.equals(Type.UNSIGNED_INT)) {
      return new Initializer(new UIntInit(value.longValue()));
    }
    if (type.equals(Type.UNSIGNED_LONG)) {
      return new Initializer(new ULongInit(value.longValue()));
    }
    if (type.equals(Type.DOUBLE)) {
      return new Initializer(new DoubleInit(value.doubleValue()));
    }
    throw new IllegalStateException("Cannot create Initializer of type " + type.toString());
  }

  public static InitialValue zeroOf(Type type) {
    if (type.equals(Type.DOUBLE)) {
      return of(0.0, type);
    }
    return of(0L, type);
  }
}
