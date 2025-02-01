package com.plasstech.lang.c.typecheck;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.plasstech.lang.c.codegen.Pseudo;

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

  public void addPseudo(Pseudo pseudo) {
    put(pseudo.identifier(), new Symbol(pseudo.identifier(), pseudo.type(), Attribute.LOCAL_ATTR));
  }

  @Override
  public String toString() {
    return symbols.toString();
  }
}
