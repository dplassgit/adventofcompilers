package com.plasstech.lang.c.codegen.tacky;

import static com.google.common.truth.Truth.assertThat;
import static com.plasstech.lang.c.codegen.RegisterOperand.R10;
import static com.plasstech.lang.c.codegen.RegisterOperand.R11;

import java.util.List;

import org.junit.Test;

import com.plasstech.lang.c.codegen.Data;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Instruction;
import com.plasstech.lang.c.codegen.Movsx;

public class FixupVisitorTest {
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
    Movsx op = new Movsx(R11, new Data("global"));
    List<Instruction> instructions = fv.visit(op);
    assertThat(instructions).hasSize(3);
  }

  @Test
  public void movsxMemoryReg() {
    Movsx op = new Movsx(new Data("global"), R11);
    List<Instruction> instructions = fv.visit(op);
    assertThat(instructions).containsExactly(op);
  }
}
