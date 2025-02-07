package com.plasstech.lang.c.codegen.tacky;

import static com.google.common.truth.Truth.assertThat;
import static com.plasstech.lang.c.codegen.RegisterOperand.R10;
import static com.plasstech.lang.c.codegen.RegisterOperand.R11;

import java.util.List;

import org.junit.Test;

import com.plasstech.lang.c.codegen.AsmBinary;
import com.plasstech.lang.c.codegen.AssemblyType;
import com.plasstech.lang.c.codegen.Data;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Movsx;
import com.plasstech.lang.c.codegen.Push;
import com.plasstech.lang.c.lex.TokenType;

public class FixupVisitorTest {
  private static final Data GLOBAL = new Data("global");
  // this is 3 when converted to a 32-bit int
  private static final Imm LONG_IMM_3 = new Imm(4294967299L);
  private FixupVisitor fv = new FixupVisitor();

  @Test
  public void movsxRegReg() {
    Movsx op = new Movsx(R10, R11);
    List<Instruction> instructions = fv.visit(op);
    assertThat(instructions).containsExactly(op);
  }

  @Test
  public void movsxImmReg() {
    Movsx op = new Movsx(new Imm(0), R11);
    List<Instruction> instructions = fv.visit(op);
    assertThat(instructions).hasSize(3);
  }

  @Test
  public void movsxRegMemory() {
    Movsx op = new Movsx(R11, GLOBAL);
    List<Instruction> instructions = fv.visit(op);
    assertThat(instructions).hasSize(3);
  }

  @Test
  public void movsxMemoryReg() {
    Movsx op = new Movsx(GLOBAL, R11);
    List<Instruction> instructions = fv.visit(op);
    assertThat(instructions).containsExactly(op);
  }

  @Test
  public void movLongTruncates() {
    Mov op = new Mov(AssemblyType.Longword, LONG_IMM_3, R10);
    List<Instruction> instructions = fv.visit(op);
    Mov expected = new Mov(AssemblyType.Longword, new Imm(3), R10);
    assertThat(instructions).containsExactly(expected);
  }

  @Test
  public void addLongToReg() {
    AsmBinary addBig = new AsmBinary(TokenType.PLUS, AssemblyType.Quadword, LONG_IMM_3, R10);
    List<Instruction> instructions = fv.visit(addBig);
    assertThat(instructions).containsExactly(addBig);
  }

  @Test
  public void addLongToMemory() {
    AsmBinary addBig = new AsmBinary(TokenType.PLUS, AssemblyType.Quadword, LONG_IMM_3, GLOBAL);
    List<Instruction> instructions = fv.visit(addBig);
    assertThat(instructions).containsExactly(
        new Mov(AssemblyType.Quadword, LONG_IMM_3, R10),
        new AsmBinary(TokenType.PLUS, AssemblyType.Quadword, R10, GLOBAL));
  }

  @Test
  public void pushLong() {
    Push push = new Push(new Imm(1));
    List<Instruction> instructions = fv.visit(push);
    assertThat(instructions).containsExactly(push);
  }

  @Test
  public void pushSmallQuadword() {
    Push push = new Push(new Imm(1));
    List<Instruction> instructions = fv.visit(push);
    assertThat(instructions).containsExactly(push);
  }

  @Test
  public void pushBigQuadword() {
    Push push = new Push(LONG_IMM_3);
    List<Instruction> instructions = fv.visit(push);
    assertThat(instructions).containsExactly(
        new Mov(AssemblyType.Quadword, LONG_IMM_3, R10),
        new Push(R10));
  }
}
