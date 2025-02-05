package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.typecheck.Type;

/**
 * Page 261.
 */
public enum AssemblyType {
  Longword("l"), // 4 bytes
  Quadword("q"); // 8 bytes

  private final String suffix;

  AssemblyType(String suffix) {
    this.suffix = suffix;
  }

  public static AssemblyType from(Type type) {
    if (type.equals(Type.LONG) || type.equals(Type.UNSIGNED_LONG)) {
      return Quadword;
    } else if (type.equals(Type.INT) || type.equals(Type.UNSIGNED_INT)) {
      return Longword;
    }
    throw new IllegalStateException(
        "Unknown type " + type.toString() + " for conversion to assembly type");
  }

  public String suffix() {
    return suffix;
  }
}
