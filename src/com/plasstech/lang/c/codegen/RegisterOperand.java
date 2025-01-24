package com.plasstech.lang.c.codegen;

import java.util.List;

import com.google.common.collect.ImmutableList;

public record RegisterOperand(Register register) implements Operand {
  private enum Register {
    RSP("rsp", "rsp"),
    RAX("al", "eax"),
    RCX("cl", "ecx"),
    RDX("dl", "edx"),
    RDI("dil", "edi"),
    RSI("sil", "esi"),
    R8("r8b", "r8d"),
    R9("r9b", "r9d"),
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
      if (bytes == 4) {
        return string4;
      }
      // 8 bytes
      return name();
    }
  }

  public static RegisterOperand RAX = new RegisterOperand(Register.RAX);
  public static RegisterOperand RCX = new RegisterOperand(Register.RCX);
  public static RegisterOperand RDX = new RegisterOperand(Register.RDX);
  public static RegisterOperand RDI = new RegisterOperand(Register.RDI);
  public static RegisterOperand RSI = new RegisterOperand(Register.RSI);
  public static RegisterOperand R8 = new RegisterOperand(Register.R8);
  public static RegisterOperand R9 = new RegisterOperand(Register.R9);
  public static RegisterOperand R10 = new RegisterOperand(Register.R10);
  public static RegisterOperand R11 = new RegisterOperand(Register.R11);
  public static RegisterOperand RSP = new RegisterOperand(Register.RSP);

  public static final List<RegisterOperand> ARG_REGISTERS = ImmutableList.of(
      RDI, RSI, RDX, RCX, R8, R9);

  @Override
  public String toString() {
    return "%" + register.toString(4);
  }

  @Override
  public String toString(int bytes) {
    return "%" + register.toString(bytes);
  }

  @Override
  public boolean inMemory() {
    return false;
  }
}
