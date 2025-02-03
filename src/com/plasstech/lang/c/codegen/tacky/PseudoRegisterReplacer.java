package com.plasstech.lang.c.codegen.tacky;

import java.util.HashMap;
import java.util.Map;

import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmFunction;
import com.plasstech.lang.c.codegen.AsmNode;
import com.plasstech.lang.c.codegen.AsmProgram;
import com.plasstech.lang.c.codegen.AsmStaticVariable;
import com.plasstech.lang.c.codegen.AsmSymtabEntry;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.AssemblyType;
import com.plasstech.lang.c.codegen.BackendSymbolTable;
import com.plasstech.lang.c.codegen.Call;
import com.plasstech.lang.c.codegen.Cdq;
import com.plasstech.lang.c.codegen.Cmp;
import com.plasstech.lang.c.codegen.Data;
import com.plasstech.lang.c.codegen.Idiv;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Jmp;
import com.plasstech.lang.c.codegen.JmpCC;
import com.plasstech.lang.c.codegen.Label;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Movsx;
import com.plasstech.lang.c.codegen.ObjEntry;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.Push;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;
import com.plasstech.lang.c.codegen.Stack;

class PseudoRegisterReplacer implements AsmNode.Visitor<Instruction> {
  /** Replace pseudo operands to stack references. See page 42. */
  private final BackendSymbolTable symbolTable;
  private int currentProcOffset;

  PseudoRegisterReplacer(BackendSymbolTable bst, int currentProcOffset) {
    this.symbolTable = bst;
    this.currentProcOffset = currentProcOffset;
  }

  int currentProcOffset() {
    return currentProcOffset;
  }

  // Maps from pseudo register name to offset
  private Map<String, Integer> pseudoMapping = new HashMap<>();

  private int getOffset(String name) {
    Integer offset = pseudoMapping.get(name);
    if (offset == null) {
      AsmSymtabEntry entry = symbolTable.get(name);
      if (entry instanceof ObjEntry oe) {
        if (oe.type() == AssemblyType.Longword) {
          currentProcOffset += 4;
        } else if (oe.type() == AssemblyType.Quadword) {
          if ((currentProcOffset % 8) == 4) {
            // Round up. page 267
            currentProcOffset += 4;
          }
          currentProcOffset += 8;
        }
      } else {
        throw new IllegalStateException(
            "Unknown backend symbol type " + entry + " for pseudo name " + name + " symbol table "
                + symbolTable);
      }
      offset = -currentProcOffset;
      pseudoMapping.put(name, offset);
    }
    return offset;
  }

  private Operand remap(Operand input) {
    return switch (input) {
      case Imm imm -> imm;
      case RegisterOperand ro -> ro;
      case Stack s -> s;
      case Pseudo p -> {
        AsmSymtabEntry s = symbolTable.get(p.identifier());
        // unclear if this is right p. 237
        if (s instanceof ObjEntry oe) {
          if (oe.isStatic()) {
            yield new Data(oe.name());
          }
        }
        yield new Stack(getOffset(p.identifier()));
      }
      case Data d -> d;
      default -> throw new IllegalArgumentException("Unexpected value: " + input);
    };
  }

  @Override
  public Instruction visit(Mov op) {
    Operand newSrc = remap(op.src());
    Operand newDest = remap(op.dst());
    return new Mov(op.type(), newSrc, newDest);
  }

  @Override
  public Instruction visit(Ret op) {
    return op;
  }

  @Override
  public Instruction visit(AsmUnary op) {
    Operand newSrc = remap(op.operand());
    return new AsmUnary(op.operator(), op.type(), newSrc);
  }

  @Override
  public Instruction visit(AsmBinary op) {
    Operand newLeft = remap(op.src());
    Operand newRight = remap(op.dst());
    return new AsmBinary(op.operator(), op.type(), newLeft, newRight);
  }

  @Override
  public Instruction visit(Idiv op) {
    Operand newOperand = remap(op.operand());
    return new Idiv(op.type(), newOperand);
  }

  @Override
  public Instruction visit(Cdq op) {
    return op;
  }

  @Override
  public Instruction visit(Cmp op) {
    Operand newLeft = remap(op.left());
    Operand newRight = remap(op.right());
    return new Cmp(op.type(), newLeft, newRight);
  }

  @Override
  public Instruction visit(Jmp op) {
    return op;
  }

  @Override
  public Instruction visit(JmpCC op) {
    return op;
  }

  @Override
  public Instruction visit(SetCC op) {
    Operand newOperand = remap(op.dest());
    return new SetCC(op.cc(), newOperand);
  }

  @Override
  public Instruction visit(Label op) {
    return op;
  }

  @Override
  public Instruction visit(AsmProgram op) {
    return null;
  }

  @Override
  public Instruction visit(AsmFunction op) {
    return null;
  }

  @Override
  public Instruction visit(Push op) {
    Operand newOperand = remap(op.operand());
    return new Push(newOperand);
  }

  @Override
  public Instruction visit(Call op) {
    return op;
  }

  @Override
  public Instruction visit(AsmStaticVariable op) {
    return null;
  }

  @Override
  public Instruction visit(Movsx op) {
    Operand newSrc = remap(op.src());
    Operand newDest = remap(op.dst());
    return new Movsx(newSrc, newDest);
  }
}