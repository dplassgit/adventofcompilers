package com.plasstech.lang.c.codegen.tacky;

public class TackyVar extends TackyVal {
  private final String identifier;

  public TackyVar(String identifier) {
    this.identifier = identifier;
  }

  public String identifier() {
    return identifier;
  }

  @Override
  public void accept(TackyNodeVisitor visitor) {
    visitor.visit(this);
  }
}
