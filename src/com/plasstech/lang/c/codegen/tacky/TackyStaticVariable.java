package com.plasstech.lang.c.codegen.tacky;

import com.plasstech.lang.c.typecheck.StaticInit;
import com.plasstech.lang.c.typecheck.Type;

/** File-scoped variable. */
public record TackyStaticVariable(String identifier, boolean global, Type t, StaticInit init)
    implements TackyTopLevel {
}
