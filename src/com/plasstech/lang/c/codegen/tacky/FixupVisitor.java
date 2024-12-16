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

/** Fix up AsmNode instructions that we've created naively. */
class FixupVisitor implements AsmNode.Visitor<List<Instruction>> {
  private static final Operand R10_OPERAND = new RegisterOperand(Register.R10);
  private static final Operand R11_OPERAND = new RegisterOperand(Register.R11);

  @Override
  public List<Instruction> visit(Mov n) {
    // Can't mov stack to stack: use r10 as an intermediary. See page 42.
    if (n.src() instanceof Stack && n.dest() instanceof Stack) {
      return ImmutableList.of(
          new Mov(n.src(), R10_OPERAND),
          new Mov(R10_OPERAND, n.dest()));
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
          return ImmutableList.of(
              new Mov(n.left(), R10_OPERAND),
              new AsmBinary(n.operator(), R10_OPERAND, n.right()));
        }
        break;

      case STAR:
        // Can't mul into stack; use r11. See page 65
        if (n.right() instanceof Stack) {
          return ImmutableList.of(
              new Mov(n.right(), R11_OPERAND), // NOTYPO
              new AsmBinary(n.operator(), n.left(), R11_OPERAND),
              new Mov(R11_OPERAND, n.right()));
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
          new Mov(n.operand(), R10_OPERAND),
          new Idiv(R10_OPERAND));
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
    // Fix if both operands are in memory; use r10. See page 88
    if (n.left() instanceof Stack && n.right() instanceof Stack) {
      return ImmutableList.of(
          new Mov(n.left(), R10_OPERAND),
          new Cmp(R10_OPERAND, n.right()));
    }
    // Fix if the second operand is a constant. See page 88
    if (n.right() instanceof Imm) {
      return ImmutableList.of(
          new Mov(n.right(), R11_OPERAND),
          new Cmp(n.left(), R11_OPERAND));
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
}
