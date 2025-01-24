package com.plasstech.lang.c.codegen;

import java.util.HashMap;
import java.util.Map;

import com.plasstech.lang.c.typecheck.FunType;
import com.plasstech.lang.c.typecheck.Symbol;
import com.plasstech.lang.c.typecheck.SymbolTable;
import com.plasstech.lang.c.typecheck.Type.SimpleType;

/** Page 266 */
public class BackendSymbolTable {
  private Map<String, AsmSymtabEntry> map = new HashMap<>();

  public void put(String name, AsmSymtabEntry entry) {
    map.put(name, entry);
  }

  public AsmSymtabEntry get(String name) {
    return map.get(name);
  }

  // Page 266 "This process is simple enough taht I won't provide the pseudocode for it". FML
  public void populate(SymbolTable input) {
    for (Symbol s : input.values()) {
      switch (s.type()) {
        case FunType ft -> {
          put(ft.name(), new FunEntry(ft.name(), s.attribute().defined()));
        }
        case SimpleType st -> {
          AssemblyType assemblyType = AssemblyType.from(s.type());
          put(st.name(), new ObjEntry(st.name(), assemblyType, s.attribute().isGlobal()));
        }
        default -> throw new IllegalArgumentException("Unexpected value: " + s.type());
      }
    }
  }
}
