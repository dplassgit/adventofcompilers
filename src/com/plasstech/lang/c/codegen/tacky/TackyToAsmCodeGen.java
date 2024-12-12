package com.plasstech.lang.c.codegen.tacky;

import java.util.List;

import com.plasstech.lang.c.codegen.AsmFunctionNode;
import com.plasstech.lang.c.codegen.AsmProgramNode;
import com.plasstech.lang.c.codegen.Instruction;

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

  private AsmFunctionNode generate(TackyFunctionDef functionDef) {
    TackyInstruction.Visitor<List<Instruction>> visitor =
        new TackyInstructionToInstructionsVisitor();
    List<Instruction> instructions = functionDef.instructions().stream()
        .map(ti -> ti.accept(visitor)).flatMap(List::stream)
        .toList();
    // TODO: remap from Pseudo -> Stack, and get the total # of bytes.

    // TODO: prepend an AllocateStack with the appropriate number of bytes (if it's > 0)
    // TODO: do the second remapping (i.e., mov stack1 stack2 -> mov stack1, r10; mov r10, stack2

    return new AsmFunctionNode(functionDef.identifier(), instructions);
  }
}
