package com.plasstech.lang.c.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * Input: AsmProgramNode (ASM AST)
 * <p>
 * Output: List<String> Assembly language text
 */
public class AsmCodeGen implements AsmNodeVisitor<Void> {
  private final List<String> emitted = new ArrayList<>();

  public List<String> generate(AsmProgramNode program) {
    program.accept(this);
    return emitted;
  }

  @Override
  public Void visit(AsmProgramNode n) {
    n.function().accept(this);
    emitted.add("  .section .note.GNU-stack,\"\",@progbits");
    return null;
  }

  @Override
  public Void visit(AsmFunctionNode n) {
    emitted.add(String.format("  .globl %s", n.name()));
    emitted.add(String.format("%s:", n.name()));
    for (Instruction i : n.instructions()) {
      i.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(Mov n) {
    // I'm not sure I like this... /shrug.
    emitted.add("  " + n.toString());
    return null;
  }

  @Override
  public Void visit(Ret n) {
    // I'm not sure I like this... /shrug.
    emitted.add("  " + n.toString());
    return null;
  }

  @Override
  public Void visit(AsmUnary asmUnary) {
    // TODO
    emitted.add("  ; AsmUnary");
    return null;
  }

  @Override
  public Void visit(AllocateStack allocateStack) {
    // TODO
    emitted.add("  ; allocate stack");
    return null;
  }
}
