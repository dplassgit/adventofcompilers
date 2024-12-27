package com.plasstech.lang.c.codegen.tacky;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.typecheck.SemanticAnalyzer;

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
    assertThat(instructions.size()).isGreaterThan(1);
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
    assertThat(instructions.size()).isGreaterThan(1);
  }

  @Test
  public void generateDeclaration() {
    String input = "int main(void) { int a = 3; return a; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    prog = new SemanticAnalyzer().validate(prog);
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    assertThat(instructions.size()).isGreaterThan(1);
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
    prog = new SemanticAnalyzer().validate(prog);
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    assertThat(instructions.size()).isGreaterThan(1);
  }

  @Test
  public void generateIf() {
    String input = """
        int main(void) {
          int a = 1;
          if (a == 1)
            a = a + 1;
          return a;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    prog = new SemanticAnalyzer().validate(prog);
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    assertThat(instructions.size()).isGreaterThan(1);
  }

  @Test
  public void generateIfElse() {
    String input = """
        int main(void) {
          int a = 1;
          if (a == 1)
            a = a + 1;
          else
            a = a - 1;
          return a;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    prog = new SemanticAnalyzer().validate(prog);
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    assertThat(tp).isNotNull();
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    assertThat(instructions.size()).isGreaterThan(1);
  }

  @Test
  public void generateFor() {
    String input = """
        int main(void) {
            int sum = 0;
            int counter;
            for (int i = 0; i <= 10; i = i + 1) {
                counter = i;
                if (i % 2 == 0)
                    continue;
                sum = sum + 1;
            }

            return sum == 5 && counter == 10;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    prog = new SemanticAnalyzer().validate(prog);
    TackyCodeGen cg = new TackyCodeGen();
    TackyProgram tp = cg.generate(prog);
    List<TackyInstruction> instructions = tp.functionDef().instructions();
    System.out.println(Joiner.on("\n").join(instructions));
  }
}
