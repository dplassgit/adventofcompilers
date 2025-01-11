package com.plasstech.lang.c.codegen;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class CodeGenTest {

  @Test
  public void generate() {
    String input = "int main(void) { return 1; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    CodeGen cg = new CodeGen();
    AsmProgram node = cg.generate(prog);
    assertThat(node).isNotNull();
  }
}
