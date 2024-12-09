package com.plasstech.lang.c.codegen.tacky;

public interface TackyNode {
  void accept(TackyNodeVisitor visitor);
}
