package com.plasstech.lang.c.codegen;

import java.util.ArrayList;
import java.util.List;

/**
 * Input: AsmProgramNode (ASM AST)
 * <p>
 * Output: List<String> Assembly language text
 */
public class AsmCodeGen implements AsmNode.Visitor<Void> {
  private final List<String> emitted = new ArrayList<>();

  public List<String> generate(AsmProgramNode program) {
    program.accept(this);
    return emitted;
  }

  // Maybe these methods should return List<String>?
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
  public Void visit(AsmUnary n) {
    String instruction = switch (n.operator()) {
      case MINUS -> "negl";
      case TWIDDLE -> "notl";
      default -> throw new IllegalStateException("Bad unary operator " + n.operator());
    };
    emitted.add(String.format("  %s %s", instruction, n.operand().toString()));
    return null;
  }

  @Override
  public Void visit(AllocateStack n) {
    if (n.bytes() > 0) {
      emitted.add(String.format("  subq $%d, %%rsp", n.bytes()));
    }
    return null;
  }

  @Override
  public Void visit(AsmBinary n) {
    String instruction = switch (n.operator()) {
      case MINUS -> "subl";
      case PLUS -> "addl";
      case STAR -> "imull";
      default -> throw new IllegalStateException("Bad binary operator " + n.operator().name());
    };
    emitted.add(String.format("  %s %s, %s", instruction, n.left(), n.right()));
    return null;
  }

  @Override
  public Void visit(Idiv n) {
    emitted.add(String.format("  idivl %s", n.operand()));
    return null;
  }

  @Override
  public Void visit(Cdq n) {
    emitted.add("  cdq");
    return null;
  }

  @Override
  public Void visit(Cmp n) {
    // Page 89
    emitted.add(String.format("  cmpl %s, %s", n.left(), n.right()));
    return null;
  }

  @Override
  public Void visit(Jmp n) {
    // Page 89
    emitted.add(String.format("  jmp .L%s", n.label()));
    return null;
  }

  @Override
  public Void visit(JmpCC n) {
    // Page 89
    emitted.add(String.format("  j%s .L%s", n.cc().name().toLowerCase(), n.label()));
    return null;
  }

  @Override
  public Void visit(SetCC n) {
    // Page 89
    emitted.add(String.format("  set%s %s", n.cc().name().toLowerCase(), n.dest().toString(1)));
    return null;
  }

  @Override
  public Void visit(Label n) {
    // Page 89
    emitted.add(String.format(".L%s:", n.label()));
    return null;
  }

  @Override
  public Void visit(DeallocateStack n) {
    return null;
  }

  @Override
  public Void visit(Push n) {
    return null;
  }

  @Override
  public Void visit(Call n) {
    return null;
  }
}
