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
      emit(".zero %d", n.init().bytes());
    } else {
      emit(".%s %d", n.init().name(), n.init().valueAsLong());
    }
    return null;
  }

  @Override
  public Void visit(Mov n) {
    // Suffix added page 270
    emit("mov%s %s, %s", n.type().suffix(), n.src().toString(n.type()), n.dst().toString(n.type()));
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
      case MINUS -> "neg";
      case TWIDDLE -> "not";
      default -> throw new IllegalStateException("Bad unary operator " + n.operator());
    };
    // Suffix added page 270
    emit("%s%s %s", instruction, n.type().suffix(), n.operand().toString(n.type()));
    return null;
  }

  @Override
  public Void visit(AsmBinary n) {
    String instruction = switch (n.operator()) {
      case MINUS -> "sub";
      case PLUS -> "add";
      case STAR -> "imul";
      default -> throw new IllegalStateException("Bad binary operator " + n.operator().name());
    };
    // Suffix added page 270
    emit("%s%s %s, %s", instruction, n.type().suffix(), n.src().toString(n.type()),
        n.dst().toString(n.type()));
    return null;
  }

  @Override
  public Void visit(Idiv n) {
    emit("idiv%s %s", n.type().suffix(), n.operand().toString(n.type()));
    return null;
  }

  @Override
  public Void visit(Cdq n) {
    switch (n.type()) {
      case Longword:
        emit("cdq");
        break;

      case Quadword:
        emit("cqo");
        break;
    }
    return null;
  }

  @Override
  public Void visit(Cmp n) {
    // Page 89
    emit("cmp%s %s, %s", n.type().suffix(), n.left().toString(n.type()),
        n.right().toString(n.type()));
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
    // Suffixes added page 270
    switch (n.operand()) {
      case RegisterOperand ro -> emit("push%s %s", n.type().suffix(), ro.toString(n.type()));
      case Imm imm -> emit("push%s %s", n.type().suffix(), imm.toString(n.type()));
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
    // Page 270
    emit("movslq %s, %s", op.src().toString(AssemblyType.Longword),
        op.dst().toString(AssemblyType.Quadword));
    return null;
  }
}
