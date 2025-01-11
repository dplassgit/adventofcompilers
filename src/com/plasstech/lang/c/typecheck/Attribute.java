package com.plasstech.lang.c.typecheck;

public interface Attribute {

  boolean defined();

  default boolean isGlobal() {
    return false;
  }

  Attribute LOCAL_ATTR = new Attribute() {
    @Override
    public boolean defined() {
      return true;
    }
  };
}
