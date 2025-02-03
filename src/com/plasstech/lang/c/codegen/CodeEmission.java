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

  private Void emit(Instruction i) {
    return emit(i.toString());
  }

  private Void emit(String pattern, Object... params) {
    emit0("  " + pattern, params);
    return null;
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
    return emit(".section .note.GNU-stack,\"\",@progbits");
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
      emit(".zero %d", n.init().bytes());
    } else {
      emit(".%s %d", n.init().name(), n.init().valueAsLong());
    }
    return null;
  }

  @Override
  public Void visit(Mov n) {
    return emit(n);
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
    return emit(n);
  }

  @Override
  public Void visit(AsmBinary n) {
    return emit(n);
  }

  @Override
  public Void visit(Idiv n) {
    return emit(n);
  }

  @Override
  public Void visit(Cdq n) {
    return emit(n);
  }

  @Override
  public Void visit(Cmp n) {
    return emit(n);
  }

  @Override
  public Void visit(Jmp n) {
    return emit(n);
  }

  @Override
  public Void visit(JmpCC n) {
    return emit(n);
  }

  @Override
  public Void visit(SetCC n) {
    return emit(n);
  }

  @Override
  public Void visit(Label n) {
    return emit(n);
  }

  @Override
  public Void visit(Push n) {
    return emit(n);
  }

  @Override
  public Void visit(Call n) {
    Symbol s = symbolTable.get(n.identifier());
    boolean external = s != null && !s.attribute().defined();
    emit("call %s%s", n.identifier(), external ? "@PLT" : "");
    return null;
  }

  @Override
  public Void visit(Movsx n) {
    return emit(n);
  }
}
