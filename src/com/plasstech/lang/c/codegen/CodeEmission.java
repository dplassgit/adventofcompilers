package com.plasstech.lang.c.codegen;

import java.util.ArrayList;
import java.util.List;

import com.plasstech.lang.c.typecheck.Symbol;
import com.plasstech.lang.c.typecheck.SymbolTable;

/**
 * This is the "code emission" step.
 * <p>
 * Input: AsmProgramNode (ASM AST)
 * <p>
 * Output: List<String> Assembly language text
 */
public class CodeEmission implements AsmNode.Visitor<Void> {
  private final SymbolTable symbolTable;
  private final List<String> emitted = new ArrayList<>();

  public CodeEmission(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public List<String> generate(AsmProgram program) {
    program.accept(this);
    return emitted;
  }

  private void emit(String pattern, Object... params) {
    emit0("  " + pattern, params);
  }

  private void emit0(String pattern, Object... params) {
    if (params != null && params.length > 0) {
      emitted.add(String.format(pattern, params));
    } else {
      emitted.add(pattern);
    }
  }

  // Maybe these methods should return List<String>?
  @Override
  public Void visit(AsmProgram n) {
    n.topLevelNodes().forEach(fn -> fn.accept(this));
    emit(".section .note.GNU-stack,\"\",@progbits");
    return null;
  }

  @Override
  public Void visit(AsmFunction n) {
    if (n.global()) {
      emit(".globl %s", n.name());
    }
    emit(".text");
    emit0("%s:", n.name());
    emit("pushq %rbp");
    emit("movq %rsp, %rbp");
    for (Instruction i : n.instructions()) {
      i.accept(this);
    }
    return null;
  }

  @Override
  public Void visit(AsmStaticVariable n) {
    if (n.global()) {
      emit(".globl %s", n.name());
    }
    if (n.init().valueAsLong() == 0) {
      emit(".bss");
    } else {
      emit(".data");
    }
    emit(".align %d", n.alignment());
    emit0("%s:", n.name());
    if (n.init().valueAsLong() == 0) {
      emit(".zero 4");
    } else {
      emit(".long %d", n.init().valueAsLong());
    }
    return null;
  }

  @Override
  public Void visit(Mov n) {
    // I'm sure I hate this.
    emit(n.toString());
    return null;
  }

  @Override
  public Void visit(Ret n) {
    emit("movq %rbp, %rsp");
    emit("popq %rbp");
    emit("ret");
    return null;
  }

  @Override
  public Void visit(AsmUnary n) {
    String instruction = switch (n.operator()) {
      case MINUS -> "negl";
      case TWIDDLE -> "notl";
      default -> throw new IllegalStateException("Bad unary operator " + n.operator());
    };
    emit("%s %s", instruction, n.operand().toString());
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
    emit("%s %s, %s", instruction, n.left(), n.right());
    return null;
  }

  @Override
  public Void visit(Idiv n) {
    emit("idivl %s", n.operand());
    return null;
  }

  @Override
  public Void visit(Cdq n) {
    emit("cdq");
    return null;
  }

  @Override
  public Void visit(Cmp n) {
    // Page 89
    emit("cmpl %s, %s", n.left(), n.right());
    return null;
  }

  @Override
  public Void visit(Jmp n) {
    // Page 89
    emit("jmp .L%s", n.label());
    return null;
  }

  @Override
  public Void visit(JmpCC n) {
    // Page 89
    emit("j%s .L%s", n.cc().name().toLowerCase(), n.label());
    return null;
  }

  @Override
  public Void visit(SetCC n) {
    // Page 89
    emit("set%s %s", n.cc().name().toLowerCase(), n.dest().toString(1));
    return null;
  }

  @Override
  public Void visit(Label n) {
    // Page 89
    emit0(".L%s:", n.label());
    return null;
  }

  @Override
  public Void visit(Push n) {
    switch (n.operand()) {
      case RegisterOperand ro -> emit("pushq %s", ro.toString(8));
      case Imm imm -> emit("pushq %s", imm.toString());
      default ->
        throw new IllegalArgumentException("Unexpected value: " + n.operand());
    }
    return null;
  }

  @Override
  public Void visit(Call n) {
    Symbol s = symbolTable.get(n.identifier());
    boolean external = false;
    if (s != null) {
      external = !s.attribute().defined();
    }
    emit("call %s%s", n.identifier(), external ? "@PLT" : "");
    return null;
  }

  @Override
  public Void visit(Movsx op) {
    throw new UnsupportedOperationException("movsx not implemented");
  }
}
