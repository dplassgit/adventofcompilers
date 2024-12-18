package com.plasstech.lang.c.parser;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.TokenType;

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
    BlockItem statement = fn.body().get(0);
    assertThat(statement).isInstanceOf(Return.class);
    Return returnStmt = (Return) statement;
    Exp expr = returnStmt.exp();
    assertThat(expr).isInstanceOf(Constant.class);
    @SuppressWarnings("unchecked")
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
  public void missingCloseBrace() {
    String input = "int main(void) { return 1;";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    assertThrows(ParserException.class, () -> p.parse());
  }

  @Test
  public void nullExpr() {
    String input = "int main(void) { ; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }

  @Test
  public void missingSemi() {
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

  @Test
  public void chapter2ParserParentheses() {
    String input = """
        int main(void) {
          return (1);
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }

  @Test
  public void chapter2ParserNegative() {
    String input = """
        int main(void) {
          return -1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }

  @Test
  public void chapter2ParserNegativeParens() {
    String input = """
        int main(void) {
          return -(-(1));
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }

  @Test
  public void chapter2ParserTwiddle() {
    String input = """
        int main(void) {
          return ~1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }

  @Test
  public void chapter2ParserDoubleTwiddle() {
    String input = """
        int main(void) {
          return ~~1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }

  @Test
  public void chapter2ParserNegativeMissingConstant() {
    String input = """
        int main(void) {
          return -;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    assertThrows(ParserException.class, () -> p.parse());
  }

  @Test
  public void chapter2ParserParenthesisNegative() {
    String input = """
        int main(void) {
          return (-);
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    assertThrows(ParserException.class, () -> p.parse());
  }

  @Test
  public void chapter2ParserDecrementConstant() {
    String input = """
        int main(void) {
          return --3;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    assertThrows(ParserException.class, () -> p.parse());
  }

  @Test
  public void chapter3BinExp() {
    String input = """
        int main(void) {
          return 3+4;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    BlockItem statement = fn.body().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isInstanceOf(BinExp.class);
    BinExp bin = (BinExp) exp;
    Exp left = bin.left();
    assertThat(left).isInstanceOf(Constant.class);
    assertThat(bin.operator()).isEqualTo(TokenType.PLUS);
    Exp right = bin.right();
    assertThat(right).isInstanceOf(Constant.class);
  }

  @Test
  public void chapter3BinExpAndFactor() {
    String input = """
        int main(void) {
          return (3+4)-(6+7);
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    BlockItem statement = fn.body().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isInstanceOf(BinExp.class);
    BinExp bin = (BinExp) exp;
    Exp left = bin.left();
    assertThat(left).isInstanceOf(BinExp.class);
    assertThat(bin.operator()).isEqualTo(TokenType.MINUS);
    Exp right = bin.right();
    assertThat(right).isInstanceOf(BinExp.class);
  }

  @Test
  public void chapter3BinExpPrecedence() {
    String input = """
        int main(void) {
          return 1*2-3;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    BlockItem statement = fn.body().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isInstanceOf(BinExp.class);
    BinExp bin = (BinExp) exp;
    Exp left = bin.left();
    assertThat(left).isInstanceOf(BinExp.class);
    assertThat(bin.operator()).isEqualTo(TokenType.MINUS);
    Exp right = bin.right();
    assertThat(right).isInstanceOf(Constant.class);
  }

  @Test
  public void chapter4UnaryNot() {
    String input = """
        int main(void) {
          return !1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    BlockItem statement = fn.body().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isInstanceOf(UnaryExp.class);
    UnaryExp unary = (UnaryExp) exp;
    assertThat(unary.operator()).isEqualTo(TokenType.BANG);
  }

  @Test
  public void chapter4Precedence() {
    String input = """
        int main(void) {
          return 1>2-3;  // should be 1 > (2-3)
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    BlockItem statement = fn.body().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isInstanceOf(BinExp.class);
    BinExp bin = (BinExp) exp;
    Exp left = bin.left();
    assertThat(left).isInstanceOf(Constant.class);
    assertThat(bin.operator()).isEqualTo(TokenType.GT);
    Exp right = bin.right();
    assertThat(right).isInstanceOf(BinExp.class);
    BinExp rightBin = (BinExp) right;
    assertThat(rightBin.operator()).isEqualTo(TokenType.MINUS);
  }

  @Test
  public void declaration() {
    String input = """
        int main(void) {
          int i;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    BlockItem statement = fn.body().get(0);
    assertThat(statement).isInstanceOf(Declaration.class);
    Declaration d = (Declaration) statement;
    assertThat(d.identifier()).isEqualTo("i");
  }

  @Test
  public void initializedDeclaration() {
    String input = """
        int main(void) {
          int i = 1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    BlockItem statement = fn.body().get(0);
    assertThat(statement).isInstanceOf(Declaration.class);
    Declaration d = (Declaration) statement;
    assertThat(d.identifier()).isEqualTo("i");
    assertThat(d.init()).hasValue(new Constant<Integer>(1));
  }

  @Test
  public void multipleStatements() {
    String input = """
        int main(void) {
          int i;
          ;
          return 1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    assertThat(fn.body()).hasSize(3);
    assertThat(fn.body().get(0)).isInstanceOf(Declaration.class);
    assertThat(fn.body().get(1)).isInstanceOf(NullStatement.class);
    assertThat(fn.body().get(2)).isInstanceOf(Return.class);
  }

  @Test
  public void expAsStatement() {
    String input = """
        int main(void) {
          1+1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    BlockItem statement = fn.body().get(0);
    assertThat(statement).isInstanceOf(Expression.class);
  }

  @Test
  public void variableAsExpression() {
    String input = """
        int main(void) {
          a;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunctionDef fn = prog.functionDef();
    // Surely there's a better way to do this, and don't call me Shirley.
    BlockItem statement = fn.body().get(0);
    Expression exp = (Expression) statement;
    Var var = (Var) exp.exp();
    assertThat(var.identifier()).isEqualTo("a");
  }

  @Test
  public void variablesInExpression() {
    String input = """
        int main(void) {
          return a+3*c-main>0;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }
}
