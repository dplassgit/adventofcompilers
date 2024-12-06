package com.plasstech.lang.c.parser;

public abstract class AstNode {

  /** Visitor pattern. */
  public abstract void accept(AstNodeVisitor visitor);
}
