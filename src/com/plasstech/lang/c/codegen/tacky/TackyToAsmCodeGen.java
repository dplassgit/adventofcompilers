package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmFunction;
import com.plasstech.lang.c.codegen.AsmProgram;
import com.plasstech.lang.c.codegen.AsmStaticVariable;
import com.plasstech.lang.c.codegen.AsmTopLevel;
import com.plasstech.lang.c.codegen.AssemblyType;
import com.plasstech.lang.c.codegen.BackendSymbolTable;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.Stack;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.typecheck.Symbol;
import com.plasstech.lang.c.typecheck.SymbolTable;
import com.plasstech.lang.c.typecheck.Type;

/**
 * This is part of the "assembly generation" step, including "replacing pseudoregisters" and "fixing
 * up instructions".
 * <p>
 * Input: TackyProgram (Tacky AST)
 * <p>
 * Output: AsmProgramNode (ASM AST)
 */
public class TackyToAsmCodeGen {
  private final SymbolTable symbolTable;

  public TackyToAsmCodeGen(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public AsmState generate(TackyProgram program) {
    // Generate multiple functiondefs. Page 194
    List<AsmTopLevel> fns = program.topLevelDefinitions().stream()
        .map(tln -> {
          return switch (tln) {
            case TackyFunction fn -> generateFn(fn);
            case TackyStaticVariable sv -> {
              int alignment;
              if (sv.type().equals(Type.LONG)) {
                alignment = 8;
              } else if (sv.type().equals(Type.INT)) {
                alignment = 4;
              } else {
                throw new IllegalStateException("Unknown static type " + sv.type());
              }
              yield new AsmStaticVariable(sv.identifier(), sv.global(), alignment, sv.init());
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + tln);
          };
        }).toList();
    BackendSymbolTable bst = new BackendSymbolTable(symbolTable);
    return new AsmState(new AsmProgram(fns), bst);
  }

  private AsmTopLevel generateFn(TackyFunction function) {
    List<Instruction> instructions = new ArrayList<>();
    // Page 200, 264 (bottom)
    // Copy input registers to param names.
    for (int i = 0; i < Math.min(6, function.params().size()); ++i) {
      String paramName = function.params().get(i);
      Symbol symbol = symbolTable.get(paramName);
      Type type = symbol.type();
      instructions.add(new Mov(AssemblyType.from(type), RegisterOperand.ARG_REGISTERS.get(i),
          new Pseudo(paramName)));
    }
    // Copy stack to param names.
    int offset = 16;
    for (int i = 6; i < function.params().size(); ++i) {
      // It *reads* from 16,24,32, etc.
      String paramName = function.params().get(i);
      Symbol symbol = symbolTable.get(paramName);
      Type type = symbol.type();
      instructions.add(
          new Mov(AssemblyType.from(type), new Stack(offset), new Pseudo(paramName)));
      offset += 8;
    }
    // 4 because they're ints now??? that's not right
    int currentProcOffset = 4 * function.params().size();

    TackyInstruction.Visitor<List<Instruction>> visitor =
        new TackyInstructionToInstructionsVisitor(symbolTable);
    List<Instruction> opInstructions = function.body().stream()
        .map(ti -> ti.accept(visitor)) // each tackyinstruction becomes a list of asmnodes
        .flatMap(List::stream).toList();

    PseudoRegisterReplacer siv =
        new PseudoRegisterReplacer(new BackendSymbolTable(symbolTable), currentProcOffset);
    FixupVisitor mfv = new FixupVisitor();
    instructions.addAll(opInstructions);
    instructions = instructions.stream()
        // remap from Pseudo -> Stack, and get the total # of bytes.
        .map(asmNode -> asmNode.accept(siv))
        // map mov stack1, stack2 -> mov stack1, r10; mov r10, stack2 etc
        .map(asmNode -> asmNode.accept(mfv)) // returns a list for each instruction
        .flatMap(List::stream)
        .collect(Collectors.toList());

    // Prepend an AllocateStack with the appropriate number of bytes (if it's > 0)
    if (currentProcOffset != 0) {
      // Round this up to the nearest 16.
      currentProcOffset = (int) (Math.ceil(currentProcOffset / 16.0) * 16);
      Instruction allocateStack =
          new AsmBinary(TokenType.MINUS, AssemblyType.Quadword, new Imm(currentProcOffset),
              RegisterOperand.RSP);
      instructions.add(0, allocateStack);
    }

    return new AsmFunction(function.identifier(), function.global(), instructions);
  }

}
