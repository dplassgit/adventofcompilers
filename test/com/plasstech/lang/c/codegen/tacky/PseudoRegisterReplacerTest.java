package com.plasstech.lang.c.codegen.tacky;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import com.plasstech.lang.c.codegen.AssemblyType;
import com.plasstech.lang.c.codegen.BackendSymbolTable;
import com.plasstech.lang.c.codegen.Imm;
import com.plasstech.lang.c.codegen.Mov;
import com.plasstech.lang.c.codegen.Pseudo;
import com.plasstech.lang.c.codegen.Stack;
import com.plasstech.lang.c.typecheck.Attribute;
import com.plasstech.lang.c.typecheck.Symbol;
import com.plasstech.lang.c.typecheck.SymbolTable;
import com.plasstech.lang.c.typecheck.Type;

public class PseudoRegisterReplacerTest {
  @Test
  public void longwordThenQuadword() {
    SymbolTable st = new SymbolTable();
    st.put("foo", new Symbol("foo", Type.INT, Attribute.LOCAL_ATTR));
    st.put("bar", new Symbol("bar", Type.LONG, Attribute.LOCAL_ATTR));
    BackendSymbolTable bst = new BackendSymbolTable(st);
    PseudoRegisterReplacer prp = new PseudoRegisterReplacer(bst, 0);
    Mov newMov1 =
        (Mov) prp.visit(new Mov(AssemblyType.Longword, new Imm(0), new Pseudo("foo", Type.INT)));
    assertThat(newMov1.dst()).isEqualTo(new Stack(-4));
    Mov newMov2 =
        (Mov) prp.visit(new Mov(AssemblyType.Quadword, new Imm(1), new Pseudo("bar", Type.LONG)));
    assertThat(newMov2.dst()).isEqualTo(new Stack(-16));
    assertThat(prp.currentProcOffset()).isEqualTo(16);
  }

  @Test
  public void quadwordThenLongword() {
    SymbolTable st = new SymbolTable();
    st.put("foo", new Symbol("foo", Type.INT, Attribute.LOCAL_ATTR));
    st.put("bar", new Symbol("bar", Type.LONG, Attribute.LOCAL_ATTR));
    BackendSymbolTable bst = new BackendSymbolTable(st);
    PseudoRegisterReplacer prp = new PseudoRegisterReplacer(bst, 0);
    Mov newMov1 =
        (Mov) prp.visit(new Mov(AssemblyType.Quadword, new Imm(1), new Pseudo("bar", Type.LONG)));
    assertThat(newMov1.dst()).isEqualTo(new Stack(-8));
    Mov newMov2 =
        (Mov) prp.visit(new Mov(AssemblyType.Longword, new Imm(0), new Pseudo("foo", Type.INT)));
    assertThat(newMov2.dst()).isEqualTo(new Stack(-12));
    assertThat(prp.currentProcOffset()).isEqualTo(12);
  }
}
