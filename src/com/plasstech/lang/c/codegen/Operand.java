package com.plasstech.lang.c.codegen;

public interface Operand {
  interface Visitor<R> {
    R visit(Imm imm);

    R visit(RegisterOperand ro);

    R visit(Pseudo p);

    R visit(Stack s);
  }

  <R> R accept(Visitor<R> visitor);

  default String toString(int bytes) {
    return toString();
  }
}
