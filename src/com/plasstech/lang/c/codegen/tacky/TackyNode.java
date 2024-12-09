package com.plasstech.lang.c.codegen.tacky;

public abstract class TackyNode {
  public abstract void accept(TackyNodeVisitor visitor);
}
