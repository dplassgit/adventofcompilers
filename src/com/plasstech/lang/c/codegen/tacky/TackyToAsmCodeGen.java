package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.plasstech.lang.c.codegen.AllocateStack;
import com.plasstech.lang.c.codegen.AsmFunctionNode;
import com.plasstech.lang.c.codegen.AsmProgramNode;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.DefaultAsmNodeVisitor;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.RegisterOperand.Register;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.Stack;

/**
 * Input: TackyProgram (Tacky AST)
 * <p>
 * Output: AsmProgramNode (ASM AST)
 */
public class TackyToAsmCodeGen {
  public AsmProgramNode generate(TackyProgram program) {
    AsmFunctionNode functionNode = generate(program.functionDef());
    return new AsmProgramNode(functionNode);
  }

  private int totalOffset = 0;
  // Maps from pseudo register name to offset
  private Map<String, Integer> pseudoMapping = new HashMap<>();

  private int getOffset(String name) {
    Integer offset = pseudoMapping.get(name);
    if (offset == null) {
      totalOffset -= 4;
      offset = totalOffset;
      pseudoMapping.put(name, totalOffset);
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

  private class PseudoToStackInstructionVisitor extends DefaultAsmNodeVisitor<Instruction> {
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
    public Instruction visit(AllocateStack a) {
      return a;
    }
  }

  private class MovFixupVisitor extends DefaultAsmNodeVisitor<List<Instruction>> {
    @Override
    public List<Instruction> visit(Mov n) {
      if (n.src() instanceof Stack && n.dest() instanceof Stack) {
        Operand r10 = new RegisterOperand(Register.R10D);
        return ImmutableList.of(
            new Mov(n.src(), r10),
            new Mov(r10, n.dest()));
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
  }

  private AsmFunctionNode generate(TackyFunctionDef functionDef) {
    TackyInstruction.Visitor<List<Instruction>> visitor =
        new TackyInstructionToInstructionsVisitor();
    PseudoToStackInstructionVisitor siv = new PseudoToStackInstructionVisitor();
    MovFixupVisitor mfv = new MovFixupVisitor();
    List<Instruction> instructions = functionDef.instructions().stream()
        .map(ti -> ti.accept(visitor)) // each tackyinstruction becomes a list of asmnodes
        .flatMap(List::stream)
        // remap from Pseudo -> Stack, and get the total # of bytes.
        .map(asmNode -> asmNode.accept(siv))
        // map mov stack1 stack2 -> mov stack1, r10; mov r10, stack2
        .map(asmNode -> asmNode.accept(mfv)) // may result in multiple
        .flatMap(List::stream)
        .toList();

    // Prepend an AllocateStack with the appropriate number of bytes (if it's > 0)
    List<Instruction> mutableInstructions = new ArrayList<>(instructions);
    mutableInstructions.add(0, new AllocateStack(totalOffset));

    return new AsmFunctionNode(functionDef.identifier(), mutableInstructions);
  }
}
