package com.plasstech.lang.c.codegen;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.typecheck.Type;

public record RegisterOperand(String name, String name1, String name4) implements Operand {
  public RegisterOperand(String name) {
    this(name, name, name);
  }

  public static RegisterOperand RAX = new RegisterOperand("RAX", "AL", "EAX");
  public static RegisterOperand RCX = new RegisterOperand("RCX", "CL", "ECX");
  public static RegisterOperand RDX = new RegisterOperand("RDX", "DL", "EDX");
  public static RegisterOperand RDI = new RegisterOperand("RDI", "DIL", "EDI");
  public static RegisterOperand RSI = new RegisterOperand("RSI", "SIL", "ESI");
  public static RegisterOperand R8 = new RegisterOperand("R8", "R8b", "R8d");
  public static RegisterOperand R9 = new RegisterOperand("R9", "R9b", "R9d");
  public static RegisterOperand R10 = new RegisterOperand("R10", "R10b", "R10d");
  public static RegisterOperand R11 = new RegisterOperand("R11", "R11b", "R11d");
  public static RegisterOperand RSP = new RegisterOperand("RSP");
  public static RegisterOperand XMM0 = new RegisterOperand("XMM0");
  public static RegisterOperand XMM1 = new RegisterOperand("XMM1");
  public static RegisterOperand XMM2 = new RegisterOperand("XMM2");
  public static RegisterOperand XMM3 = new RegisterOperand("XMM3");
  public static RegisterOperand XMM4 = new RegisterOperand("XMM4");
  public static RegisterOperand XMM5 = new RegisterOperand("XMM5");
  public static RegisterOperand XMM6 = new RegisterOperand("XMM6");
  public static RegisterOperand XMM7 = new RegisterOperand("XMM7");
  public static RegisterOperand XMM14 = new RegisterOperand("XMM14");
  public static RegisterOperand XMM15 = new RegisterOperand("XMM15");

  public static final List<RegisterOperand> ARG_REGISTERS = ImmutableList.of(
      RDI, RSI, RDX, RCX, R8, R9);
  public static final List<RegisterOperand> DOUBLE_ARG_REGISTERS = ImmutableList.of(
      XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6);

  public static final List<RegisterOperand> argRegisters(Type type) {
    if (type.equals(Type.DOUBLE)) {
      return DOUBLE_ARG_REGISTERS;
    }
    return ARG_REGISTERS;
  }

  private String nameToString(int bytes) {
    if (bytes == 1) {
      return name1;
    }
    if (bytes == 4) {
      return name4;
    }
    // 8 bytes
    return name;
  }

  @Override
  public String toString() {
    return toString(4);
  }

  @Override
  public String toString(int bytes) {
    return "%" + nameToString(bytes);
  }

  @Override
  public boolean inMemory() {
    return false;
  }
}
