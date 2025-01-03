package com.plasstech.lang.c.codegen.tacky;

import java.util.List;

public record TackyFunCall(String funName, List<TackyVal> args, TackyVar dst)
    implements TackyInstruction {
  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
