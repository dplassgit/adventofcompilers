package com.plasstech.lang.c.codegen.tacky;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.codegen.AsmCodeGen;
import com.plasstech.lang.c.codegen.AsmProgramNode;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class TackyToAsmCodeGenTest {

  @Test
  public void generate() {
    String input = "int main(void) { return 1; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    TackyProgram tp = new TackyCodeGen().generate(prog);
    AsmProgramNode an = new TackyToAsmCodeGen().generate(tp);
    assertThat(an).isNotNull();
    System.out.println(an.toString());
    List<String> asm = new AsmCodeGen().generate(an);
    System.out.println(Joiner.on("\n").join(asm));
  }

  @Test
  public void generateMultiUnary() {
    String input = "int main(void) { return -(~(-1)); }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    TackyProgram tp = new TackyCodeGen().generate(prog);
    AsmProgramNode an = new TackyToAsmCodeGen().generate(tp);
    assertThat(an).isNotNull();
    System.out.println(an.toString());
  }

}
