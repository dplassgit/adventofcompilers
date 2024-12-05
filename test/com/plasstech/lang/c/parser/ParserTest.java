package com.plasstech.lang.c.parser;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;

public class ParserTest {

  @Test
  public void chapter1Parser() {
    String input = "int main(void) { return 1; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    // This is a bit of a change detector test but it works.
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    assertThat(fn.name()).isEqualTo("main");
    Statement statement = fn.body();
    assertThat(statement).isInstanceOf(Return.class);
    Return returnStmt = (Return) statement;
    Exp expr = returnStmt.expr();
    assertThat(expr).isInstanceOf(Constant.class);
    Constant<Integer> constant = (Constant<Integer>) expr;
    assertThat(constant.value()).isEqualTo(1);
  }

  @Test
  public void chapter1ParserWithCommentsAndNewlines() {
    String input = """
        // This is a comment
        int main(void) {
          /* So is
          this! */
          return 1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }

  @Test
  public void chapter1MissingCloseBrace() {
    String input = "int main(void) { return 1;";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    assertThrows(ParserException.class, () -> p.parse());
  }

  @Test
  public void chapter1MissingSemi() {
    String input = "int main(void) { return 1}";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    assertThrows(ParserException.class, () -> p.parse());
  }

  @Test
  public void chapter1ExtraCode() {
    String input = "int main(void) { return 1;} int";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    assertThrows(ParserException.class, () -> p.parse());
  }
}
