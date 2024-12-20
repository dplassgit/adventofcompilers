package com.plasstech.lang.c.codegen.tacky;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.typecheck.Resolver;

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

  @Test
  public void generateAndAnd() {
    String input = "int main(void) { return (1+2*3)&&(3+4>5); }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    System.out.println(Joiner.on("\n").join(instructions));
  }

  @Test
  public void generateOrOr() {
    String input = "int main(void) { return 1||3; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    System.out.println(Joiner.on("\n").join(instructions));
  }

  @Test
  public void generateDeclaration() {
    String input = "int main(void) { int a = 3; return a; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    prog = new Resolver().validate(prog);
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    System.out.println(Joiner.on("\n").join(instructions));
  }

  @Test
  public void generateDeclarationWithAndWithoutInit() {
    String input = """
        int main(void) {
          int b;
          int a = 10 + 1;
          b = a * 2;
          return b;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    prog = new Resolver().validate(prog);
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    System.out.println(Joiner.on("\n").join(instructions));
  }
}
