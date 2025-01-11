package com.plasstech.lang.c.typecheck;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class ResolverTest {
  private Validator validator = new Resolver();

  private Program validate(String input) {
    return validator.validate(new Parser(new Scanner(input)).parse());
  }

  @Test
  public void noVariables_ok() {
    String input = """
        int main(void) {
          return -1;
        }
        """;
    Program program = new Parser(new Scanner(input)).parse();
    assertThat(validator.validate(program)).isEqualTo(program);
  }

  @Test
  public void declared() {
    String input = """
        int main(void) {
          int a = 3;
          return a+1;
        }
        """;
    validate(input);
  }

  @Test
  public void notDeclaredYet_error() {
    String input = """
        int main(void) {
          a=4;
        }
        """;
    SemanticAnalyzerException exception =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(exception.getMessage()).contains("Undeclared variable 'a'");
  }

  @Test
  public void duplicateDeclaration_error() {
    String input = """
        int main(void) {
          int a=4;
          int a; // ERROR
        }
        """;
    SemanticAnalyzerException exception =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(exception.getMessage()).contains("Duplicate variable definition 'a'");
  }

  @Test
  public void numericLvalue_error() {
    String input = """
        int main(void) {
          4=a;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void expLvalue_error() {
    String input = """
        int main(void) {
          int b;
          (1+b)=a;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void ownInit() {
    String input = """
        int main(void) {
          int b = b + 1;
        }
        """;
    validate(input);
  }

  @Test
  public void cond() {
    String input = """
        int main(void) {
          int b = 1?2:3;
        }
        """;
    validate(input);
  }

  @Test
  public void ifStmt() {
    String input = """
        int main(void) {
          int a=0;
          if (a == 0)
            a=a+1;
          else if (a == 1)
            a = a - 1;
          else
            a = 2;
          return a;
        }
        """;
    validate(input);
  }

  @Test
  public void ternaryAssign() {
    String input = """
        int main(void) {
            int a = 2;
            int b = 1;
            a > b ? a = 1 : a = 0;
            return a;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void undeclaredVarInIf() {
    String input = """
        int main(void) {
            if (c == 0) return 1;
            return 0;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void shadowedBlockOk() {
    String input = """
        int main(void) {
            int c = 0;
            if (c == 0) {
              int c = 1;
            }
            return 0;
        }""";
    validate(input);
  }

  @Test
  public void shadowedArbitraryBlock() {
    // Example from p 138
    String input = """
        int main(void) {
          int x = 1;
          {
            int x = 2;
            if (x > 1) {
              x = 3;
              int x = 4;
            }
            return x;
          }
          return x;
        }
        """;
    validate(input);
  }

  @Test
  public void shadowedBlockError() {
    String input = """
        int main(void) {
            int c = 0;
            if (c == 0) {
              int c = 1;
              int c = 2;
            }
            return 0;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void forBlockError() {
    String input = """
        int main(void) {
            for (int i = 0; i < 10; i=i+1) {}
            int j = i;
            return 0;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void forRedeclOk() {
    String input = """
        int main(void) {
            int i = 0;
            for (int i = 0; i < 10; i=i+1) {
              return i;
            }
            return 0;
        }""";
    validate(input);
  }

  @Test
  public void forNotRedeclOk() {
    String input = """
        int main(void) {
            int i = 0;
            for (i = 0; i < 10; i=i+1) {
              return i;
            }
            return 0;
        }""";
    validate(input);
  }

  @Test
  public void forBadCond() {
    String input = """
        int main(void) {
            for (int i = 0; j < 10; ) {}
            return 0;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void forBadPost() {
    String input = """
        int main(void) {
            for (int i = 0; ; j =j+1) {}
            return 0;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void whileOk() {
    String input = """
        int main(void) {
            int i = 0;
            while (i == 0) {
              return i;
            }
            return 0;
        }""";
    validate(input);
  }

  @Test
  public void whileBlockReDeclOk() {
    String input = """
        int main(void) {
            int i = 0;
            while (i == 0) {
              int i = 1;
              int j = i;
            }
            return i;
        }""";
    validate(input);
  }

  @Test
  public void whileBlocknotOk() {
    String input = """
        int main(void) {
            int i = 0;
            while (i == 0) {
              int j = 0;
            }
            return j;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void doWhileOk() {
    String input = """
        int main(void) {
            int i = 0;
            do { } while (i == 0);
            return 0;
        }""";
    validate(input);
  }

  @Test
  public void doWhileBreak() {
    String input = """
        int main(void) {
            int i = 0;
            do {
              break;
            } while (i == 0);
            return 0;
        }""";
    validate(input);
  }

  @Test
  public void doWhileContinue() {
    String input = """
        int main(void) {
            int i = 0;
            do {
              continue;
            } while (i == 0);
            return 0;
        }""";
    validate(input);
  }

  @Test
  public void recursiveFnCall() {
    String input = """
        int main(void) {
          return main();
        }""";
    Program program = new Parser(new Scanner(input)).parse();
    assertThat(validator.validate(program)).isEqualTo(program);
  }

  @Test
  public void fnCallNotAFn() {
    // This is allowed by the resolver, but will be rejected by the type checker.
    String input = """
        int main(void) {
          int var = 0;
          return var();
        }""";
    validate(input);
  }

  @Test
  public void fnCallUndeclared() {
    String input = """
        int main(void) {
          undeclared();
          return 0;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void internalFnDeclThenCall() {
    String input = """
        int main(void) {
          int decl(void);
          decl();
          return 0;
        }""";
    Program program = new Parser(new Scanner(input)).parse();
    assertThat(validator.validate(program)).isEqualTo(program);
  }

  @Test
  public void nestedFnDecl() {
    String input = """
        int main(void) {
          int decl(void) {
            return 0;
          }
          decl();
          return 0;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void externalFnDeclThenCall() {
    String input = """
        int decl(void);
        int main(void) {
          decl();
          return 0;
        }""";
    Program program = new Parser(new Scanner(input)).parse();
    assertThat(validator.validate(program)).isEqualTo(program);
  }

  @Test
  public void duplicateExternalDecl() {
    // This is legal because they have the same signature. But really, the
    // resolver won't catch this anyway.
    String input = """
        int decl(void);
        int decl(void);
        """;
    System.err.println(validate(input));
  }

  @Test
  public void duplicateParamDecl() {
    String input = """
        int decl(int a, int a);
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void paramAndVarDecl() {
    String input = """
        int decl(int a) {
          int a = 3;
          return a;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void fileScopeVar() {
    String input = """
        int a = 3;
        """;
    Program program = new Parser(new Scanner(input)).parse();
    assertThat(validator.validate(program)).isEqualTo(program);
  }

  @Test
  public void externAndLocalVarDecl() {
    String input = """
        int decl(int a) {
          extern int b;
          int b;
          return a;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void localThenExternVarDecl() {
    String input = """
        int decl(int a) {
          int x = 3;
          extern int x;
          return x;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void staticThenExternVarDecl() {
    String input = """
        int decl(int a) {
          static int x = 3;
          extern int x;
          return x;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void staticAndLocalVarDecl() {
    String input = """
        int decl(int a) {
          static int b;
          int b;
          return a;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void externFunDeclWithBody() {
    String input = """
        extern int decl(void) {
          return 0;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void externInInternalScope() {
    String input = """
            int main(void) {
               {
                /* This declares a variable 'a'
                 * with external linkage
                 */
                extern int a;
               }
               /* a is no longer in scope after the end of the block */
               return a;
            }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void undeclaredGlobalStatic() {
    String input = """
        int main(void) {
            return x;
        }

        /* you must declare a file-scope variable before using it */
        int x = 0;
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }
}
