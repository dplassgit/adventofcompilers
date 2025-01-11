package com.plasstech.lang.c.codegen.tacky;

/** File-scoped variable. */
public record TackyStaticVariable(String identifier, boolean global, int initialValue)
    implements TackyTopLevel {
}
