package com.plasstech.lang.c.typecheck;

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
}
