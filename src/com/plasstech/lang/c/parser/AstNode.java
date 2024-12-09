package com.plasstech.lang.c.parser;

public interface AstNode {

  /** Visitor pattern. */
  <T> T accept(AstNodeVisitor<T> visitor);
}
