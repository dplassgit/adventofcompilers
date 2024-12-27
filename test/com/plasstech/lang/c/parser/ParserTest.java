package com.plasstech.lang.c.parser;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.TokenType;

public class ParserTest {
  @Test
  public void chapter1Parser() {
    String input = "int main(void) { return 1; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.name()).isEqualTo("main");
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    assertThat(returnStmt.exp()).isEqualTo(Constant.of(1));
  }

  @Test
  public void chapter1ParserWithCommentsAndNewlines() {
    String withoutComments = "int main(void) { return 1; }";
    Parser p = new Parser(new Scanner(withoutComments));
    Program programWithoutComments = p.parse();

    String withComments = """
        // This is a comment
        int main(void) {
          /* So is
          this! */
          return 1;
        }
        """;
    p = new Parser(new Scanner(withComments));
    Program programWithComments = p.parse();
    assertThat(programWithoutComments).isEqualTo(programWithComments);
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
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.name()).isEqualTo("main");
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    Exp expr = returnStmt.exp();
    assertThat(expr).isEqualTo(Constant.of(1));
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
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isEqualTo(new BinExp(Constant.of(3), TokenType.PLUS, Constant.of(4)));
  }

  @Test
  public void chapter3BinExpAndFactor() {
    String input = """
        int main(void) {
          return (3+4)-(6*7);
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    BinExp bin = (BinExp) exp;
    Exp left = bin.left();
    assertThat(left).isEqualTo(new BinExp(Constant.of(3), TokenType.PLUS, Constant.of(4)));
    assertThat(bin.operator()).isEqualTo(TokenType.MINUS);
    Exp right = bin.right();
    assertThat(right).isEqualTo(new BinExp(Constant.of(6), TokenType.STAR, Constant.of(7)));
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
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    BinExp bin = (BinExp) exp;
    Exp left = bin.left();
    assertThat(left)
        .isEqualTo(new BinExp(Constant.of(1), TokenType.STAR, Constant.of(2)));
    assertThat(bin.operator()).isEqualTo(TokenType.MINUS);
    Exp right = bin.right();
    assertThat(right).isEqualTo(Constant.of(3));
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
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isEqualTo(new UnaryExp(TokenType.BANG, Constant.of(1)));
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
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isInstanceOf(BinExp.class);
    BinExp bin = (BinExp) exp;
    Exp left = bin.left();
    assertThat(bin.operator()).isEqualTo(TokenType.GT);
    assertThat(left).isEqualTo(Constant.of(1));
    Exp right = bin.right();
    assertThat(right).isEqualTo(new BinExp(Constant.of(2), TokenType.MINUS, Constant.of(3)));
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
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    VarDecl d = (VarDecl) (Declaration) statement;
    assertThat(d.identifier()).isEqualTo("i");
  }

  @Test
  public void initializedDeclarationConstant() {
    String input = """
        int main(void) {
          int i = 1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    VarDecl d = (VarDecl) statement;
    assertThat(d.identifier()).isEqualTo("i");
    assertThat(d.init()).hasValue(Constant.of(1));
  }

  @Test
  public void initializedDeclaration() {
    String input = """
        int main(void) {
          int i = a+b;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    VarDecl d = (VarDecl) statement;
    assertThat(d.identifier()).isEqualTo("i");
    BinExp expected = new BinExp(new Var("a"), TokenType.PLUS, new Var("b"));
    assertThat(d.init()).hasValue(expected);
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
    FunDecl fn = prog.funDecls().get(0);
    List<BlockItem> items = fn.body().get().items();
    assertThat(items).hasSize(3);
    assertThat(items.get(0)).isInstanceOf(VarDecl.class);
    assertThat(items.get(1)).isInstanceOf(NullStatement.class);
    assertThat(items.get(2)).isInstanceOf(Return.class);
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
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
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
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
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

  @Test
  public void assignment() {
    String input = """
        int main(void) {
          a=b+c;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    Expression exp = (Expression) statement;
    Assignment assn = (Assignment) exp.exp();
    assertThat(assn.lvalue()).isEqualTo(new Var("a"));
    BinExp rvalue = (BinExp) assn.rvalue();
    assertThat(rvalue.left()).isEqualTo(new Var("b"));
    assertThat(rvalue.operator()).isEqualTo(TokenType.PLUS);
    assertThat(rvalue.right()).isEqualTo(new Var("c"));
  }

  @Test
  public void rightAssocAssignment() {
    String input = """
        int main(void) {
          a=b=c;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    Expression exp = (Expression) statement;
    Assignment assn = (Assignment) exp.exp();
    assertThat(assn.lvalue()).isEqualTo(new Var("a"));
    Assignment rvalue = (Assignment) assn.rvalue();
    assertThat(rvalue.lvalue()).isEqualTo(new Var("b"));
    assertThat(rvalue.rvalue()).isEqualTo(new Var("c"));
  }

  @Test
  public void rightAssocAssignmentWithAndWithoutParens() {
    String withoutParens = """
        int main(void) {
          a=b=c;
        }
        """;
    String withParens = """
        int main(void) {
          a=(b=c);
        }
        """;
    Scanner s = new Scanner(withoutParens);
    Parser p = new Parser(s);
    Program prog1 = p.parse();

    Scanner s2 = new Scanner(withParens);
    Parser p2 = new Parser(s2);
    Program prog2 = p2.parse();
    assertThat(prog1).isEqualTo(prog2);
  }

  @Test
  public void chapter6If() {
    String input = """
        int main(void) {
          int a = 3;
          if (a == 3)
            a = a + 1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(1);
    assertThat(statement).isInstanceOf(If.class);
  }

  @Test
  public void chapter6IfElse() {
    String input = """
        int main(void) {
          int a = 3;
          if (a == 3)
            a = a + 1;
          else
            a = a - 1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(1);
    assertThat(statement).isInstanceOf(If.class);
  }

  @Test
  public void chapter6IfElseIf() {
    String input = """
        int main(void) {
          int a = 3;
          if (a == 3)
            a = a + 1;
          else if (a == 4)
            a = a - 1;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(1);
    assertThat(statement).isInstanceOf(If.class);
  }

  @Test
  public void chapter6IfElseIfElse() {
    String input = """
        int main(void) {
          int a = 3;
          if (a == 3)
            a = a + 1;
          else if (a == 4)
            a = a - 1;
          else
            a = a + 2;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(1);
    assertThat(statement).isInstanceOf(If.class);
  }

  @Test
  public void chapter6Conditional() {
    String input = """
        int main(void) {
          return 3?1:4;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    Exp exp = returnStmt.exp();
    assertThat(exp).isEqualTo(new Conditional(Constant.of(3), Constant.of(1), Constant.of(4)));
  }

  @Test
  public void chapter6ConditionalPrecedenceAboveAssignment() {
    String input = """
        int main(void) {
          int a=3?1:4;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    VarDecl a = (VarDecl) statement;
    Exp exp = a.init().get();
    assertThat(exp).isEqualTo(new Conditional(Constant.of(3), Constant.of(1), Constant.of(4)));
  }

  @Test
  public void chapter6ConditionalPrecedence() {
    String input = """
        int main(void) {
          int a= 1||2 ? 3 : 4;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    VarDecl a = (VarDecl) statement;
    Exp exp = a.init().get();
    assertThat(exp).isInstanceOf(Conditional.class);
    System.err.println(exp);
  }

  @Test
  public void chapter7Blocks() {
    String input = """
        int main(void) {
          int a = 1;
          {
            int b = 1;
            {
              int c = 1;
            }
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.body().get().items()).hasSize(2);
    BlockItem inner = fn.body().get().items().get(1);
    assertThat(inner).isInstanceOf(Compound.class);
  }

  @Test
  public void chapter7IfBlock() {
    String input = """
        int main(void) {
          int a = 1;
          if (a == 1) {
            int b = 1;
            a = b - 1;
          } else {
            int b = 1;
            a = b + 1;
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    p.parse();
  }

  @Test
  public void chapter8Break() {
    String input = "int main(void) { break; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    System.err.println(p.parse());
  }

  @Test
  public void chapter8Continue() {
    String input = "int main(void) { continue; }";
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    System.err.println(p.parse());
  }

  @Test
  public void chapter8WhileNull() {
    String input = """
        int main(void) {
          while (0) ;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.body().get().items()).hasSize(1);
    BlockItem inner = fn.body().get().items().get(0);
    assertThat(inner).isInstanceOf(While.class);
  }

  @Test
  public void chapter8WhileBreak() {
    String input = """
        int main(void) {
          while (0) break;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.body().get().items()).hasSize(1);
    BlockItem stmt = fn.body().get().items().get(0);
    assertThat(stmt).isInstanceOf(While.class);
    While whileLoop = (While) stmt;
    assertThat(whileLoop.body()).isInstanceOf(Break.class);
  }

  @Test
  public void chapter8WhileContinue() {
    String input = """
        int main(void) {
          while (0) continue;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.body().get().items()).hasSize(1);
    BlockItem stmt = fn.body().get().items().get(0);
    assertThat(stmt).isInstanceOf(While.class);
    While whileLoop = (While) stmt;
    assertThat(whileLoop.body()).isInstanceOf(Continue.class);
  }

  @Test
  public void chapter8WhileBreakAndContinue() {
    String input = """
        int main(void) {
          while (0) {
            continue;
            break;
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    System.err.println(p.parse());
  }

  @Test
  public void chapter8WhileBlockContinue() {
    String input = """
        int main(void) {
          while (0) {
            continue;
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.body().get().items()).hasSize(1);
    BlockItem stmt = fn.body().get().items().get(0);
    assertThat(stmt).isInstanceOf(While.class);
    While whileLoop = (While) stmt;
    assertThat(whileLoop.body()).isInstanceOf(Compound.class);
  }

  @Test
  public void chapter8DoWhileNull() {
    String input = """
        int main(void) {
          do ; while (0);
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.body().get().items()).hasSize(1);
    BlockItem inner = fn.body().get().items().get(0);
    assertThat(inner).isInstanceOf(DoWhile.class);
  }

  @Test
  public void chapter8DoWhileBreak() {
    String input = """
        int main(void) {
          do break; while (0);
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.body().get().items()).hasSize(1);
    BlockItem inner = fn.body().get().items().get(0);
    assertThat(inner).isInstanceOf(DoWhile.class);
  }

  @Test
  public void chapter8DoWhileContinue() {
    String input = """
        int main(void) {
          do continue; while (0);
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.body().get().items()).hasSize(1);
    BlockItem inner = fn.body().get().items().get(0);
    assertThat(inner).isInstanceOf(DoWhile.class);
  }

  @Test
  public void chapter8DoWhileBreakAndContinue() {
    String input = """
        int main(void) {
          do {
            break;
            continue;
          } while (0);
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    p.parse();
  }

  @Test
  public void chapter8ForMinimal() {
    String input = """
        int main(void) {
          for (; ;) ;
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    p.parse();
  }

  @Test
  public void chapter8ForFull() {
    String input = """
        int main(void) {
          for (int i = 0; i < 10; i=i+1) {
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    System.err.println(p.parse());
  }

  @Test
  public void chapter8ForBreak() {
    String input = """
        int main(void) {
          for (int i = 0; i < 10; i=i+1) {
            break;
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    System.err.println(p.parse());
  }

  @Test
  public void chapter8ForContinue() {
    String input = """
        int main(void) {
          for (int i = 0; i < 10; i=i+1) {
            continue;
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    System.err.println(p.parse());
  }

  @Test
  public void chapter8ForNoDecl() {
    String input = """
        int main(void) {
          for (i; i < 10; i=i+1) {
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    System.err.println(p.parse());
  }

  @Test
  public void chapter8ForMissingPost() {
    String input = """
        int main(void) {
          for (int i = 0; i < 10) {
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    assertThrows(ParserException.class, () -> p.parse());
  }

  @Test
  public void chapter8ForExtraExpr() {
    String input = """
        int main(void) {
          for (int i = 0; i < 10; i = i + 1; j = j + 1) {
          }
        }
        """;
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    assertThrows(ParserException.class, () -> p.parse());
  }
}
