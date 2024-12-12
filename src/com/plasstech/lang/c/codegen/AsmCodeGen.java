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
    emitted.add("  pushq %rbp");
    emitted.add("  movq %rsp, %rbp");
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
    emitted.add("  movq %rbp, %rsp");
    emitted.add("  popq %rbp");
    emitted.add("  ret");
    return null;
  }

  @Override
  public Void visit(AsmUnary asmUnary) {
    String instruction = switch (asmUnary.operator()) {
      case MINUS -> "negl";
      case TWIDDLE -> "notl";
      default -> throw new IllegalStateException("Bad");
    };
    emitted.add(String.format("  %s %s", instruction, asmUnary.operand().toString()));
    return null;
  }

  @Override
  public Void visit(AllocateStack allocateStack) {
    if (allocateStack.bytes() > 0) {
      emitted.add(String.format("  subq $%d, %%rsp", allocateStack.bytes()));
    }
    return null;
  }
}
