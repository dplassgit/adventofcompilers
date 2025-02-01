package com.plasstech.lang.c.typecheck;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
  private final Map<String, Symbol> symbols = new HashMap<>();

  public Symbol get(String name) {
    return symbols.get(name);
  }

  public void put(String name, Symbol s) {
    symbols.put(name, s);
  }

  public Collection<Symbol> values() {
    return symbols.values();
  }

  @Override
  public String toString() {
    return "SymbolTable:" + symbols.toString();
  }
}
