package com.plasstech.lang.c.codegen.tacky;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.AllocateStack;
import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmFunctionNode;
import com.plasstech.lang.c.codegen.AsmNode;
import com.plasstech.lang.c.codegen.AsmProgramNode;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.Cdq;
import com.plasstech.lang.c.codegen.Cmp;
import com.plasstech.lang.c.codegen.Idiv;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Jmp;
import com.plasstech.lang.c.codegen.JmpCC;
import com.plasstech.lang.c.codegen.Label;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.RegisterOperand.Register;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;
import com.plasstech.lang.c.codegen.Stack;

/** Fix up Mov, Binary, Idiv instructions that we've created naively. */
class FixupVisitor implements AsmNode.Visitor<List<Instruction>> {
  @Override
  public List<Instruction> visit(Mov n) {
    // Can't mov stack to stack: use r10 as an intermediary. See page 42.
    if (n.src() instanceof Stack && n.dest() instanceof Stack) {
      Operand r10 = new RegisterOperand(Register.R10D);
      return ImmutableList.of(
          new Mov(n.src(), r10),
          new Mov(r10, n.dest()));
    }
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(AsmBinary n) {
    switch (n.operator()) {
      case PLUS:
      case MINUS:
        // Can't add or subtract stack and stack; use r10. See page 64
        if (n.left() instanceof Stack && n.right() instanceof Stack) {
          Operand r10 = new RegisterOperand(Register.R10D);
          return ImmutableList.of(
              new Mov(n.left(), r10),
              new AsmBinary(n.operator(), r10, n.right()));
        }
        break;

      case STAR:
        // Can't mul into stack; use r11. See page 65
        if (n.right() instanceof Stack) {
          Operand r11 = new RegisterOperand(Register.R11D);
          return ImmutableList.of(
              new Mov(n.right(), r11), // NOTYPO
              new AsmBinary(n.operator(), n.left(), r11),
              new Mov(r11, n.right()));
        }
        break;

      default:
        throw new IllegalStateException("Cannot fix up " + n.operator().name());
    }
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Idiv n) {
    // Can't divide by a constant; use r10 as an intermediary. See page 64
    if (n.operand() instanceof Imm) {
      Operand r10 = new RegisterOperand(Register.R10D);
      return ImmutableList.of(
          new Mov(n.operand(), r10),
          new Idiv(r10));
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
  public List<Instruction> visit(AllocateStack n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(Cdq n) {
    return ImmutableList.of(n);
  }

  @Override
  public List<Instruction> visit(AsmProgramNode n) {
    return null;
  }

  @Override
  public List<Instruction> visit(AsmFunctionNode n) {
    return null;
  }

  @Override
  public List<Instruction> visit(Cmp n) {
    // Fix if both operands are in memory.
    // Fix if the second operand is a constant.
    return null;
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
}