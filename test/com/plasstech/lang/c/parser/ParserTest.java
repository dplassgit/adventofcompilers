package com.plasstech.lang.c.parser;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.typecheck.Type;

public class ParserTest {

  private static Program parse(String input) {
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);
    return p.parse();
  }

  @Test
  public void chapter1Parser() {
    String input = "int main(void) { return 1; }";
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.name()).isEqualTo("main");
    BlockItem statement = fn.body().get().items().get(0);
    Return returnStmt = (Return) statement;
    assertThat(returnStmt.exp()).isEqualTo(Constant.of(1));
  }

  @Test
  public void chapter1ParserWithCommentsAndNewlines() {
    String withoutComments = "int main(void) { return 1; }";
    Program programWithoutComments = parse(withoutComments);

    String withComments = """
        // This is a comment
        int main(void) {
          /* So is
          this! */
          return 1;
        }
        """;
    Program programWithComments = parse(withComments);
    assertThat(programWithoutComments).isEqualTo(programWithComments);
  }

  @Test
  public void missingCloseBrace() {
    String input = "int main(void) { return 1;";
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void nullExpr() {
    String input = "int main(void) { ; }";
    parse(input);
  }

  @Test
  public void missingSemi() {
    String input = "int main(void) { return 1}";
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter1ExtraCode() {
    String input = "int main(void) { return 1;} int";
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter2ParserParentheses() {
    String input = """
        int main(void) {
          return (1);
        }
        """;
    Program prog = parse(input);
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
    parse(input);
  }

  @Test
  public void chapter2ParserNegativeParens() {
    String input = """
        int main(void) {
          return -(-(1));
        }
        """;
    parse(input);
  }

  @Test
  public void chapter2ParserTwiddle() {
    String input = """
        int main(void) {
          return ~1;
        }
        """;
    parse(input);
  }

  @Test
  public void chapter2ParserDoubleTwiddle() {
    String input = """
        int main(void) {
          return ~~1;
        }
        """;
    parse(input);
  }

  @Test
  public void chapter2ParserNegativeMissingConstant() {
    String input = """
        int main(void) {
          return -;
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter2ParserParenthesisNegative() {
    String input = """
        int main(void) {
          return (-);
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter2ParserDecrementConstant() {
    String input = """
        int main(void) {
          return --3;
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter3BinExp() {
    String input = """
        int main(void) {
          return 3+4;
        }
        """;
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    VarDecl d = (VarDecl) (Declaration) statement;
    assertThat(d.name()).isEqualTo("i");
  }

  @Test
  public void initializedDeclarationConstant() {
    String input = """
        int main(void) {
          int i = 1;
        }
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    VarDecl d = (VarDecl) statement;
    assertThat(d.name()).isEqualTo("i");
    assertThat(d.init()).hasValue(Constant.of(1));
  }

  @Test
  public void initializedDeclaration() {
    String input = """
        int main(void) {
          int i = a+b;
        }
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    VarDecl d = (VarDecl) statement;
    assertThat(d.name()).isEqualTo("i");
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    parse(input);
  }

  @Test
  public void assignment() {
    String input = """
        int main(void) {
          a=b+c;
        }
        """;
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    String inputWithoutParens = """
        int main(void) {
          a=b=c;
        }
        """;
    Program withoutParens = parse(inputWithoutParens);
    String inputWithParens = """
        int main(void) {
          a=(b=c);
        }
        """;
    Program withParens = parse(inputWithParens);
    assertThat(withoutParens).isEqualTo(withParens);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    VarDecl a = (VarDecl) statement;
    Exp exp = a.init().get();
    assertThat(exp).isInstanceOf(Conditional.class);
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
    Program prog = parse(input);
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
    parse(input);
  }

  @Test
  public void chapter8Break() {
    String input = "int main(void) { break; }";
    parse(input);
  }

  @Test
  public void chapter8Continue() {
    String input = "int main(void) { continue; }";
    parse(input);
  }

  @Test
  public void chapter8WhileNull() {
    String input = """
        int main(void) {
          while (0) ;
        }
        """;
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    Program prog = parse(input);
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
    parse(input);
  }

  @Test
  public void chapter8ForMinimal() {
    String input = """
        int main(void) {
          for (; ;) ;
        }
        """;
    parse(input);
  }

  @Test
  public void chapter8ForFull() {
    String input = """
        int main(void) {
          for (int i = 0; i < 10; i=i+1) {
          }
        }
        """;
    parse(input);
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
    parse(input);
  }

  @Test
  public void chapter8ForFunctionDecl() {
    String input = """
        int main(void) {
          for (int i(int a, int b); i < 10; i=i+1) {
          }
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
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
    parse(input);
  }

  @Test
  public void chapter8ForNoDecl() {
    String input = """
        int main(void) {
          for (i; i < 10; i=i+1) {
          }
        }
        """;
    parse(input);
  }

  @Test
  public void chapter8ForMissingPost() {
    String input = """
        int main(void) {
          for (int i = 0; i < 10) {
          }
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter8ForExtraExpr() {
    String input = """
        int main(void) {
          for (int i = 0; i < 10; i = i + 1; j = j + 1) {
          }
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9MultipleFns() {
    String input = """
        int main1(void) {}
        int main2(void) {}
        """;
    parse(input);
  }

  @Test
  public void chapter9Extern() {
    String input = """
        int main1(void);
        int main2(void) {}
        """;
    parse(input);
  }

  @Test
  public void chapter9FnWithParams() {
    String input = """
        int main2(int a, int b) {}
        """;
    parse(input);
  }

  @Test
  public void chapter9FnMissingParam() {
    String input = """
        int main2(int a, ) {}
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9FnMissingParamName() {
    String input = """
        int main2(int) {}
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9FnExtraVoid() {
    String input = """
        int main2(void, void) {}
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9FnCall() {
    String input = """
        int main2(int a, int b) {
          return main2(a+b, b*a+3);
        }
        """;
    parse(input);
  }

  @Test
  public void chapter9FnCallVoid() {
    String input = """
        int main2(void) {
          return main2();
        }
        """;
    parse(input);
  }

  @Test
  public void chapter9FnCallEof() {
    String input = """
        int main2(void) {
          return main2(
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9BadFnCallArgs() {
    String input = """
        int main2(int a, int b) {
          return main2(a+b b*a+3);
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9FnCallTrailingComma() {
    String input = """
        int main2(int a, int b) {
          return main2(a,b,);
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9FnDeclTrailingComma() {
    String input = """
        int main2(int a,) {
          return main2(a,b);
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9BadTopLevelFnCall() {
    String input = """
        main2(a+b b*a+3);
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter9NestedFnDecl() {
    String input = """
        int main(void) {
          int foo(void);

          int x = foo();
          if (x > 0) {
             int foo  = 3;
             x = x + foo;
          }
          return x;
        }

        int foo(void) {
             return 4;
        }
        """;
    parse(input);
  }

  @Test
  public void chapter10TopLevelVarDecl() {
    String input = "int a;";
    parse(input);
  }

  @Test
  public void chapter10TopLevelVarDeclWithInit() {
    String input = "int a = 3;";
    Program p = parse(input);
    Declaration declaration = p.declarations().get(0);
    assertThat(declaration).isInstanceOf(VarDecl.class);
    if (declaration instanceof VarDecl vd) {
      assertThat(vd.storageClass()).isEmpty();
    }
  }

  @Test
  public void chapter10TopLevelStaticVarDecl() {
    Program staticInt = parse("static int a;");
    Program intStatic = parse("int static a;");
    assertThat(staticInt).isEqualTo(intStatic);

    Declaration declaration = staticInt.declarations().get(0);
    assertThat(declaration).isInstanceOf(VarDecl.class);
    if (declaration instanceof VarDecl vd) {
      assertThat(vd.storageClass()).hasValue(StorageClass.STATIC);
    }
  }

  @Test
  public void chapter10FnDeclarationWithStorageClass() {
    String input = """
        static int fn(void);
        """;
    Program program = parse(input);

    Declaration declaration = program.declarations().get(0);
    assertThat(declaration).isInstanceOf(FunDecl.class);
    if (declaration instanceof FunDecl fd) {
      assertThat(fd.storageClass()).hasValue(StorageClass.STATIC);
      assertThat(fd.body()).isEmpty();
    }
  }

  @Test
  public void chapter10FnDefinitionWithStorageClass() {
    // This isn't allowed by the type checker but the parser allows it.
    String input = """
        extern  int fn(void) {
          return 0;
        }
        """;
    Program program = parse(input);

    Declaration declaration = program.declarations().get(0);
    assertThat(declaration).isInstanceOf(FunDecl.class);
    if (declaration instanceof FunDecl fd) {
      assertThat(fd.storageClass()).hasValue(StorageClass.EXTERN);
      assertThat(fd.body()).isPresent();
    }
  }

  @Test
  public void chapter10TopLevelExternVarDecl() {
    Program externInt = parse("extern int a;");
    Program intExtern = parse("int extern a;");
    assertThat(externInt).isEqualTo(intExtern);

    Declaration declaration = externInt.declarations().get(0);
    assertThat(declaration).isInstanceOf(VarDecl.class);
    if (declaration instanceof VarDecl vd) {
      assertThat(vd.storageClass()).hasValue(StorageClass.EXTERN);
    }
  }

  @Test
  public void chapter10TopLevelVarDeclBadTypes() {
    String input = """
        int int a;
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter10TopLevelVarDeclBadClasses() {
    String input = """
        int extern static a;
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter10StaticLocal() {
    String input = """
        int main(void) {
             static int i = 2;
             return 0;
        }
        """;
    parse(input);
  }

  @Test
  public void chapter10ForBadExternInt() {
    String input = """
        int main(void) {
          for (extern int i = 0; ;) {
          }
        }
        """;
    parse(input);
  }

  @Test
  public void chapter10ForBadIntExtern() {
    String input = """
        int main(void) {
          for (int extern i = 0; ;) {
          }
        }
        """;
    parse(input);
  }

  @Test
  public void chapter10ForStaticIntOK() {
    String input = """
        int main(void) {
          for (static int i = 0; ;) {
          }
        }
        """;
    parse(input);
  }

  @Test
  public void chapter10ForIntStaticOK() {
    String input = """
        int main(void) {
          for (int static i = 0; ;) {
          }
        }
        """;
    parse(input);
  }

  @Test
  public void chapter11IntLocal() {
    String input = """
        int main(void) {
           int a;
        }
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    if (statement instanceof VarDecl vd) {
      assertThat(vd.type()).isEqualTo(Type.INT);
    }
  }

  @Test
  public void chapter11LongLocal() {
    String input = """
        int main(void) {
           long a;
        }
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    if (statement instanceof VarDecl vd) {
      assertThat(vd.type()).isEqualTo(Type.LONG);
    }
  }

  @Test
  public void chapter11LongIntLocal() {
    String input = """
        int main(void) {
           long int a;
        }
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    if (statement instanceof VarDecl vd) {
      assertThat(vd.type()).isEqualTo(Type.LONG);
    }
  }

  @Test
  public void chapter11IntLongLocal() {
    String input = """
        int main(void) {
           long int a;
        }
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    BlockItem statement = fn.body().get().items().get(0);
    assertThat(statement).isInstanceOf(VarDecl.class);
    if (statement instanceof VarDecl vd) {
      assertThat(vd.type()).isEqualTo(Type.LONG);
    }
  }

  @Test
  public void chapter11IntIntBad() {
    String input = """
        int main(void) {
           int int a;
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter11LongIntFn() {
    String input = """
        long int main(void) { return 0L;}
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.funType().ret()).isEqualTo(Type.LONG);
  }

  @Test
  public void chapter11IntFn() {
    String input = """
        int main(void) { return 0L;}
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.funType().ret()).isEqualTo(Type.INT);
  }

  @Test
  public void chapter11LongIntParam() {
    String input = """
        int main(long int p) { return 0;}
        """;
    Program prog = parse(input);
    FunDecl fn = prog.funDecls().get(0);
    assertThat(fn.funType().params().get(0)).isEqualTo(Type.LONG);
  }

  @Test
  public void chapter11Cast() {
    String input = """
        long int main(int p) {
          long p2 = (long) p;
          return p2;
        }
        """;
    Program prog = parse(input);
    System.err.println(prog);
  }

  @Test
  public void chapter11BadCastToConstant() {
    String input = """
        long main(int p) {
          long p2 = (3) p;
          return p2;
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }

  @Test
  public void chapter11BadCast() {
    String input = """
        long main(int p) {
          long p2 = (p) p;
          return p2;
        }
        """;
    assertThrows(ParserException.class, () -> parse(input));
  }
}
