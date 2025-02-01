package com.plasstech.lang.c.codegen.tacky;

import static com.plasstech.lang.c.codegen.RegisterOperand.R10;
import static com.plasstech.lang.c.codegen.RegisterOperand.R11;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmFunction;
import com.plasstech.lang.c.codegen.AsmNode;
import com.plasstech.lang.c.codegen.AsmProgram;
import com.plasstech.lang.c.codegen.AsmStaticVariable;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.AssemblyType;
import com.plasstech.lang.c.codegen.Call;
import com.plasstech.lang.c.codegen.Cdq;
import com.plasstech.lang.c.codegen.Cmp;
import com.plasstech.lang.c.codegen.Idiv;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Jmp;
import com.plasstech.lang.c.codegen.JmpCC;
import com.plasstech.lang.c.codegen.Label;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Movsx;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.Push;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;

/** Fix up AsmNode instructions that we've created naively. */
class FixupVisitor implements AsmNode.Visitor<List<Instruction>> {
  @Override
  public List<Instruction> visit(Mov n) {
    // Can't mov stack to stack: use r10 as an intermediary. See page 42.
    if (n.src().inMemory() && n.dest().inMemory()) {
      return ImmutableList.of(
          new Mov(n.type(), n.src(), R10),
          new Mov(n.type(), R10, n.dest()));
    }
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(AsmBinary n) {
    switch (n.operator()) {
      case PLUS:
      case MINUS:
        // Can't add or subtract stack and stack; use r10. See page 64
        if (n.left().inMemory() && n.right().inMemory()) {
          return ImmutableList.of(
              new Mov(n.type(), n.left(), R10),
              new AsmBinary(n.operator(), n.type(), R10, n.right()));
        }
        break;

      case STAR:
        // Can't mul into stack; use r11. See page 65
        if (n.right().inMemory()) {
          return ImmutableList.of(
              new Mov(n.type(), n.right(), R11), // NOTYPO
              new AsmBinary(n.operator(), n.type(), n.left(), R11),
              new Mov(n.type(), R11, n.right()));
        }
        break;

      default:
        break;
    }
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Idiv n) {
    // Can't divide by a constant; use r10 as an intermediary. See page 64
    if (n.operand() instanceof Imm) {
      return ImmutableList.of(
          new Mov(n.type(), n.operand(), R10),
          new Idiv(n.type(), R10));
    }
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(AsmUnary n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Ret n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Cdq n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(AsmProgram n) {
    return null;
  }

  @Override
  public List<Instruction> visit(AsmFunction n) {
    return null;
  }

  @Override
  public List<Instruction> visit(AsmStaticVariable n) {
    return null;
  }

  @Override
  public List<Instruction> visit(Cmp n) {
    // Fix if both operands are in memory; use r10. See page 88
    if (n.left().inMemory() && n.right().inMemory()) {
      return ImmutableList.of(
          new Mov(n.type(), n.left(), R10),
          new Cmp(n.type(), R10, n.right()));
    }
    // Fix if the second operand is a constant. See page 88
    if (n.right() instanceof Imm) {
      return ImmutableList.of(
          new Mov(n.type(), n.right(), R11),
          new Cmp(n.type(), n.left(), R11));
    }
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Jmp n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(JmpCC n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(SetCC n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Label n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Push n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Call n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Movsx op) {
    // Can't use memory as dest or immediate as source.
    // Page 267
    Operand dst = op.dst();
    Operand src = op.src();
    if (dst.inMemory() || src instanceof Imm) {
      // mov src to reg 10
      // movsx reg 10, reg 11
      // mov r11 to dest
      return ImmutableList.of(
          new Mov(AssemblyType.Longword, src, R10),
          new Movsx(R10, R11),
          new Mov(AssemblyType.Quadword, R11, dst));
    }
    return ImmutableList.of(op);
  }
}
