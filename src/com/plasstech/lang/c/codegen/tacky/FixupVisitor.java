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
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;

/** Fix up AsmNode instructions that we've created naively. */
class FixupVisitor implements AsmNode.Visitor<List<Instruction>> {
  @Override
  public List<Instruction> visit(Mov n) {
    boolean needsIntermediary =
        n.dst().inMemory() &&
            (n.src().inMemory() || (n.type() == AssemblyType.Quadword && immOutOfRange(n.src())));
    if (needsIntermediary) {
      // Can't mov stack to stack: use r10 as an intermediary. See page 42.
      // Can't movq from immediate that is bigger than 32 bits. Page 268
      return ImmutableList.of(
          new Mov(n.type(), n.src(), R10),
          new Mov(n.type(), R10, n.dst()));
    }
    if (n.type() == AssemblyType.Longword && immOutOfRange(n.src())) {
      // placate the assembler. P 268
      long value = ((Imm) n.src()).value();
      // I don't know if this will work for negative numbers. Shrug.
      int intVal = (int) value;
      return ImmutableList.of(
          new Mov(n.type(), new Imm(intVal), n.dst()));
    }
    return ImmutableList.of(n);
  }

  private static boolean immOutOfRange(Operand operand) {
    if (operand instanceof Imm source) {
      long val = source.value();
      return val > Integer.MAX_VALUE || val < Integer.MIN_VALUE;
    }
    return false;
  }

  @Override
  public List<Instruction> visit(AsmBinary n) {
    switch (n.operator()) {
      case PLUS:
      case MINUS: {
        boolean needsIntermediary =
            n.dst().inMemory() &&
                (n.src().inMemory()
                    || (n.type() == AssemblyType.Quadword && immOutOfRange(n.src())));
        if (needsIntermediary) {
          // Can't add or subtract stack and stack; use r10. See page 64
          // Or, if left or right is an immediate that is bigger than 32 bits, need to fixup. Page 268
          return ImmutableList.of(
              new Mov(n.type(), n.src(), R10),
              new AsmBinary(n.operator(), n.type(), R10, n.dst()));
        }
      }
        break;

      case STAR: {
        // Can't mul into stack; use r11. See page 65
        // Also can't mul with a 64-bit immediate. Page 268. 
        boolean needsIntermediary =
            n.dst().inMemory() || (n.type() == AssemblyType.Quadword && immOutOfRange(n.src()));
        if (needsIntermediary) {
          return ImmutableList.of(
              new Mov(n.type(), n.src(), RegisterOperand.R11), // NOTYPO
              new AsmBinary(n.operator(), n.type(), n.dst(), RegisterOperand.R11),
              new Mov(n.type(), RegisterOperand.R11, n.dst()));
        }
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
    // Fix if the second operand is a constant. See page 88, 268
    if (n.right() instanceof Imm) {
      if (n.type() == AssemblyType.Quadword && immOutOfRange(n.left())) {
        return ImmutableList.of(
            new Mov(n.type(), n.left(), R10),
            new Mov(n.type(), n.right(), R11),
            new Cmp(n.type(), R10, R11));
      }

      // convert cmp foo, 10234567
      // to:
      // mov 1023454326, r11
      // cmp foo, r11
      return ImmutableList.of(
          new Mov(n.type(), n.right(), R11),
          new Cmp(n.type(), n.left(), R11));
    }
    // Fix if both operands are in memory; use r10. See page 88
    boolean needsIntermediary = (n.left().inMemory() && n.right().inMemory() ||
        (n.type() == AssemblyType.Quadword && immOutOfRange(n.left())));
    if (needsIntermediary) {
      // convert cmp foo, bar to:
      // mov foo, r10
      // cmp r10, bar
      // and also

      // cmp 1234, bar to
      // mov 123, r10
      // cmp r10, bar
      return ImmutableList.of(
          new Mov(n.type(), n.left(), R10),
          new Cmp(n.type(), R10, n.right()));
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
    if (immOutOfRange(n.operand())) {
      return ImmutableList.of(
          new Mov(AssemblyType.Quadword, n.operand(), R10),
          new Push(R10));
    }
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
