package com.plasstech.lang.c.parser;

public class FunctionDef extends AstNode {
  private final String name;
  private final Statement body;

  public FunctionDef(String name, Statement body) {
    this.name = name;
    this.body = body;
  }

  public String name() {
    return name;
  }

  public Statement body() {
    return body;
  }

  @Override
  public void accept(AstNodeVisitor visitor) {
    visitor.visit(this);
  }
}
