package com.plasstech.lang.c.codegen.tacky;

import java.util.List;

record TackyFunctionDef(String identifier, List<String> params, List<TackyInstruction> body) {
}
