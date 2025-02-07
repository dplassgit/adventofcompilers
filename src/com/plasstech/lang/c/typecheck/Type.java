package com.plasstech.lang.c.typecheck;

public interface Type {
  String name();

  default int size() {
    return 0;
  }

  default boolean signed() {
    return false;
  }

  record SimpleType(String name, int size, boolean signed) implements Type {
    @Override
    public final String toString() {
      return name;
    }
  }

  Type INT = new SimpleType("int", 32, true);
  Type LONG = new SimpleType("long", 64, true);
  Type UNSIGNED_INT = new SimpleType("unsigned int", 32, false);
  Type UNSIGNED_LONG = new SimpleType("unsigned long", 64, false);
  Type NO_TYPE = new SimpleType("no type", 0, false);
}
