package com.plasstech.lang.c.codegen;

public interface AsmNode {
  void accept(AsmNodeVisitor visitor);
}
