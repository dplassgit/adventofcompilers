package com.plasstech.lang.c.codegen.tacky;

import java.util.List;

record TackyFunctionDef(String identifier, boolean global, List<String> params,
    List<TackyInstruction> body)
    implements TackyTopLevel {
}
