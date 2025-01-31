package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.codegen.AsmProgram;
import com.plasstech.lang.c.codegen.BackendSymbolTable;

public record AsmState(AsmProgram program, BackendSymbolTable backendSymbolTable) {
}
