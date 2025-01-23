package com.plasstech.lang.c.codegen.tacky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.plasstech.lang.c.codegen.AllocateStack;
import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AsmFunction;
import com.plasstech.lang.c.codegen.AsmNode;
import com.plasstech.lang.c.codegen.AsmProgram;
import com.plasstech.lang.c.codegen.AsmStaticVariable;
import com.plasstech.lang.c.codegen.AsmTopLevel;
import com.plasstech.lang.c.codegen.AsmUnary;
import com.plasstech.lang.c.codegen.Call;
import com.plasstech.lang.c.codegen.Cdq;
import com.plasstech.lang.c.codegen.Cmp;
import com.plasstech.lang.c.codegen.Data;
import com.plasstech.lang.c.codegen.DeallocateStack;
import com.plasstech.lang.c.codegen.Idiv;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Jmp;
import com.plasstech.lang.c.codegen.JmpCC;
import com.plasstech.lang.c.codegen.Label;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Operand;
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.Push;
import com.plasstech.lang.c.codegen.RegisterOperand;
import com.plasstech.lang.c.codegen.Ret;
import com.plasstech.lang.c.codegen.SetCC;
import com.plasstech.lang.c.codegen.Stack;
import com.plasstech.lang.c.typecheck.Attribute;
import com.plasstech.lang.c.typecheck.StaticAttr;
import com.plasstech.lang.c.typecheck.Symbol;
import com.plasstech.lang.c.typecheck.SymbolTable;

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

  public AsmProgram generate(TackyProgram program) {
    // Generate multiple functiondefs. Page 194
    List<AsmTopLevel> fns = program.topLevelDefinitions().stream()
        .map(tln -> {
          return switch (tln) {
            case TackyFunction fn -> generateFn(fn);
            case TackyStaticVariable sv ->
              new AsmStaticVariable(sv.identifier(), sv.global(), sv.init().valueAsLong());
            default -> throw new IllegalArgumentException("Unexpected value: " + tln);
          };
        }).toList();
    return new AsmProgram(fns);
  }

  private int currentProcOffset = 0;

  private AsmTopLevel generateFn(TackyFunction function) {
    List<Instruction> instructions = new ArrayList<>();
    // Page 200, top
    // Copy input registers to param names.
    for (int i = 0; i < Math.min(6, function.params().size()); ++i) {
      instructions.add(new Mov(RegisterOperand.ARG_REGISTERS.get(i),
          new Pseudo(function.params().get(i))));
    }
    // Copy stack to param names.
    int offset = 16;
    for (int i = 6; i < function.params().size(); ++i) {
      // It *reads* from 16,24,32, etc.
      instructions.add(new Mov(new Stack(offset), new Pseudo(function.params().get(i))));
      offset += 8;
    }
    // 4 because they're ints now.
    currentProcOffset = 4 * function.params().size();

    TackyInstruction.Visitor<List<Instruction>> visitor =
        new TackyInstructionToInstructionsVisitor();
    PseudoToStackInstructionVisitor siv = new PseudoToStackInstructionVisitor();
    FixupVisitor mfv = new FixupVisitor();
    List<Instruction> opInstructions = function.body().stream()
        .map(ti -> ti.accept(visitor)) // each tackyinstruction becomes a list of asmnodes
        .flatMap(List::stream).toList();

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
      instructions.add(0, new AllocateStack(currentProcOffset));
    }

    return new AsmFunction(function.identifier(), function.global(), instructions);
  }

  // Maps from pseudo register name to offset
  private Map<String, Integer> pseudoMapping = new HashMap<>();

  private int getOffset(String name) {
    Integer offset = pseudoMapping.get(name);
    if (offset == null) {
      currentProcOffset += 4;
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
        Symbol s = symbolTable.get(p.identifier());
        if (s != null) {
          // It's OK if s is null; it means it's a temp
          Attribute attr = s.attribute();
          if (attr instanceof StaticAttr) {
            // Unclear if this is right. Page 237
            yield new Data(p.identifier());
          }
        }
        yield new Stack(getOffset(p.identifier()));
      }
      case Data d -> d;
      default -> throw new IllegalArgumentException("Unexpected value: " + input);
    };
  }

  /** Replace pseudo operands to stack references. See page 42. */
  private class PseudoToStackInstructionVisitor implements AsmNode.Visitor<Instruction> {
    @Override
    public Instruction visit(Mov n) {
      Operand newSrc = remap(n.src());
      Operand newDest = remap(n.dest());
      return new Mov(newSrc, newDest);
    }

    @Override
    public Instruction visit(Ret n) {
      return n;
    }

    @Override
    public Instruction visit(AsmUnary u) {
      Operand newSrc = remap(u.operand());
      return new AsmUnary(u.operator(), newSrc);
    }

    @Override
    public Instruction visit(AsmBinary n) {
      Operand newLeft = remap(n.left());
      Operand newRight = remap(n.right());
      return new AsmBinary(n.operator(), newLeft, newRight);
    }

    @Override
    public Instruction visit(AllocateStack a) {
      return a;
    }

    @Override
    public Instruction visit(Idiv n) {
      Operand newOperand = remap(n.operand());
      return new Idiv(newOperand);
    }

    @Override
    public Instruction visit(Cdq n) {
      return n;
    }

    @Override
    public Instruction visit(Cmp n) {
      Operand newLeft = remap(n.left());
      Operand newRight = remap(n.right());
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
      Operand newOperand = remap(n.dest());
      return new SetCC(n.cc(), newOperand);
    }

    @Override
    public Instruction visit(Label n) {
      return n;
    }

    @Override
    public Instruction visit(AsmProgram n) {
      return null;
    }

    @Override
    public Instruction visit(AsmFunction n) {
      return null;
    }

    @Override
    public Instruction visit(DeallocateStack n) {
      return n;
    }

    @Override
    public Instruction visit(Push n) {
      Operand newOperand = remap(n.operand());
      return new Push(newOperand);
    }

    @Override
    public Instruction visit(Call n) {
      return n;
    }

    @Override
    public Instruction visit(AsmStaticVariable n) {
      return null;
    }
  }
}
