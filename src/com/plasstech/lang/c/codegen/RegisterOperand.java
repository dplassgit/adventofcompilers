package com.plasstech.lang.c.codegen;

public record RegisterOperand(Register register) implements Operand {
  public enum Register {
    AX("al", "eax"),
    DX("dl", "edx"),
    R10("r10b", "r10d"),
    R11("r11b", "r11d");

    private final String string1;
    private final String string4;

    Register(String string1, String string4) {
      this.string1 = string1;
      this.string4 = string4;
    }

    String toString(int bytes) {
      if (bytes == 1) {
        return string1;
      }
      return string4;
    }
  }

  @Override
  public String toString() {
    return "%" + register.toString(4);
  }

  @Override
  public String toString(int bytes) {
    return register.toString(bytes);
  }

  @Override
  public <R> R accept(Visitor<R> visitor) {
    return visitor.visit(this);
  }
}
