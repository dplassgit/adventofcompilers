package com.plasstech.lang.c.codegen;

public abstract class AsmNode {
  public abstract void accept(AsmNodeVisitor visitor);
}
