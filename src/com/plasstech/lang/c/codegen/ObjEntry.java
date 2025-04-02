package com.plasstech.lang.c.codegen;

/** Page 266, 327 */
public record ObjEntry(String name, AssemblyType type, boolean isStatic, boolean isConstant)
    implements AsmSymtabEntry {

}
