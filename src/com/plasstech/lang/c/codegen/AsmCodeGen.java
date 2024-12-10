package com.plasstech.lang.c.codegen;

import java.util.ArrayList;
import java.util.List;

public class AsmCodeGen implements AsmNodeVisitor {
  private final List<String> emitted = new ArrayList<>();

  public List<String> generate(AsmProgramNode program) {
    program.accept(this);
    return emitted;
  }

  @Override
  public void visit(AsmProgramNode n) {
    emitted.add("  .section .note.GNU-stack,\"\",@progbits");
    emitted.add("  .section .text");
    n.function().accept(this);
  }

  @Override
  public void visit(AsmFunctionNode n) {
    emitted.add(String.format("  .globl %s", n.name()));
    emitted.add(String.format("%s:", n.name()));
    for (Instruction i : n.instructions()) {
      i.accept(this);
    }
  }

  @Override
  public void visit(Mov n) {
    // I'm not sure I like this... /shrug.
    emitted.add("  " + n.toString());
  }

  @Override
  public void visit(Ret n) {
    // I'm not sure I like this... /shrug.
    emitted.add("  " + n.toString());
  }

  @Override
  public void visit(AsmUnary asmUnary) {}

  @Override
  public void visit(AllocateStack allocateStack) {}
}
