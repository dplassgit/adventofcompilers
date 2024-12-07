package com.plasstech.lang.c.codegen;

import java.util.List;

public class AsmFunctionNode extends AsmNode {
  private final String name;
  private final List<Instruction> instructions;

  public AsmFunctionNode(String name, List<Instruction> instructions) {
    this.name = name;
    this.instructions = instructions;
  }

  public String name() {
    return name;
  }

  public List<Instruction> instructions() {
    return instructions;
  }

  @Override
  public void accept(AsmNodeVisitor visitor) {
    visitor.visit(this);
  }
}
