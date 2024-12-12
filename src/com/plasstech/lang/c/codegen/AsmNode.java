package com.plasstech.lang.c.codegen;

public interface AsmNode {
  <R> R accept(AsmNodeVisitor<R> visitor);
}
