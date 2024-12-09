package com.plasstech.lang.c.parser;

public abstract class AstNode {

  /** Visitor pattern. */
  public abstract <T> T accept(AstNodeVisitor<T> visitor);
}
