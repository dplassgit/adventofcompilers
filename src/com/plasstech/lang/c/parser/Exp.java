package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.typecheck.Type;

public interface Exp extends AstNode {
  Type type();
}
