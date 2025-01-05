package com.plasstech.lang.c.codegen.tacky;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.codegen.AsmCodeGen;
import com.plasstech.lang.c.codegen.AsmFunctionNode;
import com.plasstech.lang.c.codegen.AsmProgramNode;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.typecheck.SemanticAnalyzer;

public class TackyToAsmCodeGenTest {

  private AsmProgramNode asmProgramNode;

  @Test
  public void generate() {
    String input = "int main(void) { return 1; }";
    printAsm(generateAsm(input));
  }

  @Test
  public void generateMultiUnary() {
    String input = "int main(void) { return -(~(-1)); }";
    printAsm(generateAsm(input));
  }

  @Test
  public void generateBinaryConstants() {
    String input = "int main(void) { return 1+2; }";
    printAsm(generateAsm(input));
  }

  @Test
  public void generateBinaryMultiple() {
    String input = "int main(void) { return 1+2+(3-4); }";
    printAsm(generateAsm(input));
  }

  @Test
  public void generateMultiply() {
    String input = "int main(void) { return 1*2+3; }";
    printAsm(generateAsm(input));
  }

  @Test
  public void generateChapter4() {
    String input = "int main(void) { return !((1>=2) || (3<4) > (5==6)); }";
    generateAsm(input);
  }

  @Test
  public void generateFnCall() {
    String input = """
        int simple(int param) { return param; }
        int main(void) {
          return simple(1);
        }
        """;
    printAsm(generateAsm(input));
    for (AsmFunctionNode fn : asmProgramNode.functions()) {
      System.err.println(Joiner.on("\n").join(fn.instructions()));
    }
  }

  @Test
  public void generateFnCallWith8Args() {
    String input =
        """
            int simple(int a, int b, int c, int d, int e, int f, int g, int h) { return a+b+c+d*e+f+g*h; }
            int main(void) {
              return simple(1,2,3,4,5,6,7,8);
            }
            """;
    printAsm(generateAsm(input));
    for (AsmFunctionNode fn : asmProgramNode.functions()) {
      System.err.println(Joiner.on("\n").join(fn.instructions()));
    }
  }

  private static void printAsm(List<String> asm) {
    System.out.println(Joiner.on("\n").join(asm));
  }

  private List<String> generateAsm(String input) {
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
    prog = semanticAnalyzer.validate(prog);
    TackyProgram tp = new TackyCodeGen().generate(prog);
    asmProgramNode = new TackyToAsmCodeGen().generate(tp);
    assertThat(asmProgramNode).isNotNull();
    return new AsmCodeGen(semanticAnalyzer.symbolTable()).generate(asmProgramNode);
  }
}
