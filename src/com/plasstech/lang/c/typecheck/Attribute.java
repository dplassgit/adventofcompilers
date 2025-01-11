package com.plasstech.lang.c.typecheck;

public interface Attribute {

  /** Whether the symbol this attribute is on has been defined or just declared. */
  boolean defined();

  default boolean isGlobal() {
    return false;
  }

  /**
   * A local attribute, for function parameters and variables with automatic storage duration. Page
   * 230
   */
  Attribute LOCAL_ATTR = new Attribute() {
    @Override
    public boolean defined() {
      return true;
    }
  };
}
