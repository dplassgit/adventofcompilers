package com.plasstech.lang.c.codegen;

import com.plasstech.lang.c.typecheck.Type;

/**
 * Page 261.
 */
public enum AssemblyType {
  Longword("l"),
  Quadword("q");

  private final String suffix;

  AssemblyType(String suffix) {
    this.suffix = suffix;
  }

  public static AssemblyType from(Type type) {
    if (type.equals(Type.LONG)) {
      return Quadword;
    } else if (type.equals(Type.INT)) {
      return Longword;
    }
    throw new IllegalStateException(
        "Unknown type " + type.toString() + " for conversion to assembly type");
  }

  public String suffix() {
    return suffix;
  }
}
