package com.plasstech.lang.c.codegen;

public class Mov extends Instruction {
  private final Operand src;
  private final Operand dest;

  public Mov(Operand src, Operand dest) {
    this.src = src;
    this.dest = dest;
  }

  public Operand src() {
    return src;
  }

  public Operand dest() {
    return dest;
  }

  @Override
  public String toString() {
    return String.format("movl %s, %s", src, dest);
  }
}
