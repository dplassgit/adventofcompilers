package com.plasstech.lang.c.codegen.tacky;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class TackyCodeGenTest {

  @Test
  public void generate() {
    String input = "int main(void) { return 1; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    System.out.println(tp.toString());
  }

  @Test
  public void generateUnary() {
    String input = "int main(void) { return -1; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    System.out.println(tp.toString());
  }

  @Test
  public void generateMultipleUnary() {
    String input = "int main(void) { return -(~(-1)); }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    System.out.println(tp.toString());
  }

  @Test
  public void generateBinary() {
    String input = "int main(void) { return 1+2*3; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    System.out.println(tp.toString());
  }
}
