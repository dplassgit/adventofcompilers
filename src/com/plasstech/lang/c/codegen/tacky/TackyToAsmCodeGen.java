package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;
import com.plasstech.lang.c.codegen.Stack;

/**
 * This is the "code emission" step.
 * <p>
 * Input: TackyProgram (Tacky AST)
 * <p>
 * Output: AsmProgramNode (ASM AST)
 */
public class TackyToAsmCodeGen {
  public AsmProgramNode generate(TackyProgram program) {
    AsmFunctionNode functionNode = generate(program.functionDef());
    return new AsmProgramNode(functionNode);
  }

  private AsmFunctionNode generate(TackyFunctionDef functionDef) {
    TackyInstruction.Visitor<List<Instruction>> visitor =
        new TackyInstructionToInstructionsVisitor();
    PseudoToStackInstructionVisitor siv = new PseudoToStackInstructionVisitor();
    FixupVisitor mfv = new FixupVisitor();
    List<Instruction> instructions = functionDef.instructions().stream()
        .map(ti -> ti.accept(visitor)) // each tackyinstruction becomes a list of asmnodes
        .flatMap(List::stream)
        // remap from Pseudo -> Stack, and get the total # of bytes.
        .map(asmNode -> asmNode.accept(siv))
        // map mov stack1 stack2 -> mov stack1, r10; mov r10, stack2 etc
        .map(asmNode -> asmNode.accept(mfv)) // returns a list for each instruction
        .flatMap(List::stream)
        .toList();

    // Prepend an AllocateStack with the appropriate number of bytes (if it's > 0)
    List<Instruction> mutableInstructions = new ArrayList<>(instructions);
    mutableInstructions.add(0, new AllocateStack(totalOffset));

    return new AsmFunctionNode(functionDef.identifier(), mutableInstructions);
  }

  private int totalOffset = 0;
  // Maps from pseudo register name to offset
  private Map<String, Integer> pseudoMapping = new HashMap<>();

  private int getOffset(String name) {
    Integer offset = pseudoMapping.get(name);
    if (offset == null) {
      totalOffset += 4;
      offset = -totalOffset;
      pseudoMapping.put(name, offset);
    }
    return offset;
  }

  private class PseudoRegisterRemapper implements Operand.Visitor<Operand> {
    @Override
    public Operand visit(Imm imm) {
      return imm;
    }

    @Override
    public Operand visit(RegisterOperand ro) {
      return ro;
    }

    @Override
    public Operand visit(Pseudo p) {
      int offset = getOffset(p.identifier());
      return new Stack(offset);
    }

    @Override
    public Operand visit(Stack s) {
      return s;
    }
  }

  /** Replace pseudo operands to stack references. See page 42. */
  private class PseudoToStackInstructionVisitor implements AsmNode.Visitor<Instruction> {
    private final Operand.Visitor<Operand> operandRemapper = new PseudoRegisterRemapper();

    @Override
    public Instruction visit(Mov n) {
      Operand newSrc = n.src().accept(operandRemapper);
      Operand newDest = n.dest().accept(operandRemapper);
      return new Mov(newSrc, newDest);
    }

    @Override
    public Instruction visit(Ret n) {
      return n;
    }

    @Override
    public Instruction visit(AsmUnary u) {
      Operand newSrc = u.operand().accept(operandRemapper);
      return new AsmUnary(u.operator(), newSrc);
    }

    @Override
    public Instruction visit(AsmBinary n) {
      Operand newLeft = n.left().accept(operandRemapper);
      Operand newRight = n.right().accept(operandRemapper);
      return new AsmBinary(n.operator(), newLeft, newRight);
    }

    @Override
    public Instruction visit(AllocateStack a) {
      return a;
    }

    @Override
    public Instruction visit(Idiv n) {
      Operand newOperand = n.operand().accept(operandRemapper);
      return new Idiv(newOperand);
    }

    @Override
    public Instruction visit(Cdq n) {
      return n;
    }

    @Override
    public Instruction visit(Cmp n) {
      Operand newLeft = n.left().accept(operandRemapper);
      Operand newRight = n.right().accept(operandRemapper);
      return new Cmp(newLeft, newRight);
    }

    @Override
    public Instruction visit(Jmp n) {
      return n;
    }

    @Override
    public Instruction visit(JmpCC n) {
      return n;
    }

    @Override
    public Instruction visit(SetCC n) {
      Operand newOperand = n.dest().accept(operandRemapper);
      return new SetCC(n.cc(), newOperand);
    }

    @Override
    public Instruction visit(Label n) {
      return n;
    }

    @Override
    public Instruction visit(AsmProgramNode n) {
      return null;
    }

    @Override
    public Instruction visit(AsmFunctionNode n) {
      return null;
    }
  }
}
