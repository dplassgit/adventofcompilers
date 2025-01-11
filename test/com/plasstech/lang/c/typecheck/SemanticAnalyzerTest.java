package com.plasstech.lang.c.typecheck;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Ignore;
import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class SemanticAnalyzerTest {
  private SemanticAnalyzer validator = new SemanticAnalyzer();
  private SymbolTable symbols = validator.symbolTable();

  private Program validate(String input) {
    return validator.validate(new Parser(new Scanner(input)).parse());
  }

  @Test
  public void fnDef() {
    String input = """
        int main(void) {
          return 3;
        }
        """;
    validate(input);
    Symbol s = symbols.get("main");
    assertThat(s).isNotNull();
    assertThat(s.attribute().defined()).isTrue();
  }

  @Test
  public void fnCallTooNanyParams() {
    String input = """
        int main(void) {
          main(1);
          return 3;
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("wrong number of params; saw 1, expected 0");
  }

  @Test
  public void fnCallTooFewParams() {
    String input = """
        int main(int a, int b) {
          main(1);
          return 3;
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("wrong number of params; saw 1, expected 2");
  }

  @Test
  public void fnReDef() {
    String input = """
        int main(void) {
          return 3;
        }
        int main(int a) {
          return 3;
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("Incompatible function declarations");
  }

  @Test
  public void fnReDefSameSignature() {
    String input = """
        int main(void) {
          return 3;
        }
        int main(void) {
          return 3;
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("defined more than once");
  }

  @Test
  public void fnDecl() {
    String input = "int main(void);";
    validate(input);
    Symbol s = symbols.get("main");
    assertThat(s).isNotNull();
    assertThat(s.attribute().defined()).isFalse();
  }

  @Test
  public void fnDeclWithParams() {
    String input = "int main(int a, int b);";
    validate(input);
    Symbol s = symbols.get("main");
    assertThat(s).isNotNull();
    assertThat(s.attribute().defined()).isFalse();
    assertThat(s.type()).isInstanceOf(FunType.class);
    if (s.type() instanceof FunType f) {
      assertThat(f.paramCount()).isEqualTo(2);
    }
  }

  @Test
  public void redeclaredfnDecl() {
    String input = """
        int main(int a, int b);
        int main(void);
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("Incompatible function declarations");
  }

  @Test
  public void varRedeclaredAsFn() {
    String input = """
        int main(void) {
          int a;
          int a(void);
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void fnUsedAsVar() {
    String input = """
        int a(void) { return 0; }
        int main(void) {
          a = 3;
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("used as variable");
  }

  @Test
  public void varUsedAsFn() {
    String input = """
        int main(void) {
          int a = 3;
          return a();
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("called as function");
  }

  @Test
  public void symbolTableScopesAreWeird() {
    // Example from p 181
    String input = """
        int main(void) {
          int foo(int a, int b);
          return 0;
        }
        int foo(int a);
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage())
        .isEqualTo(
            "Incompatible function declarations; 'foo' already defined as 'int foo(int, int)'");
  }

  @Test
  @Ignore("This may not be an error?")
  public void cannotInitializeExternVar() {
    String input = """
        extern int a = 3;
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("Cannot initialize `extern` variable 'a'");
  }

  @Test
  public void cannotInitializeExternFun() {
    String input = """
        extern int fn(void) {
          return 3;
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("Cannot define extern function 'fn'");
  }

  @Test
  public void fileLevelVarRedefinesFn() {
    String input = """
        int foo(void);

        /* this conflicts with the previous declaration of foo as a function */
        int foo;

        int main(void) {
            return 0;
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("Function 'foo' redeclared as variable");
  }

  @Test
  public void redeclareFileScopeVarAsFun() {
    String input = """
        int foo = 10;

        int main(void) {
            /* Since this declaration has external linkage,
             * it refers to the same entity as the declaration
             * of foo above. But the earlier declaration declares
             * a variable and this one declares a function,
             * so they conflict.
             */
            int foo(void);
            return 0;
        }
                """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void staticBlockScopeFunDecl() {
    String input = """
        int main(void) {
            /* Can't have static storage class
             * on block-scope function declarations
             */
            static int foo(void);
            return foo();
        }

        static int foo(void) {
            return 0;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void staticForLoopCounter() {
    String input = """
        int main(void) {
            int x = 0;

            /* a variable declared in a for loop header cannot have a storage class. */
            for (static int i = 0; i < 10; i = i + 1) {
                x = x + 1;
            }

            return x;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void conflictingGlobalDefinitions() {
    String input = """
        /* This declaration of foo is also a definition,
         * since it includes an initializer.
         */
        int foo = 3;

        int main(void) {
            return 0;
        }

        /* This declaration of foo is also a definition,
         * since it includes an initializer.
         * This is illegal, because foo was already declared.
         */
        int foo = 4;
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void conflictingVariableLinkage() {
    String input = """
        /* This declares foo with internal linkage */
        static int foo;

        int main(void) {
            return foo;
        }

        /* This declares foo with external linkage,
         * which conflicts with the previous declaration
         */
        int foo = 3;
                    """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void conflictingVariableLinkage2() {
    String input = """
          int main(void) {
            /* A local variable with no linkage */
            int x = 3;
            {
                /* Because no other x identifier
                 * with any linkage has been declared,
                 * the 'extern' keyword gives this external
                 * linkage.
                 */
                extern int x;
            }
            return x;
        }

        /* This has internal linkage, so it conflicts
         * with the previous declaration of x.
         */
        static int x = 10;
                """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void redeclareFunAsFileScopeVar() {
    String input = """
                int foo(void);

        /* this conflict with the previous declaration of foo as a function */
        int foo;

        int main(void) {
            return 0;
        }""";
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void nonConstantSTaticInitializer() {
    String input = """
                int a = 10;
        /* b has static storage duration,
         * so its initializer must be constant.
         */
        int b = 1 + a;

        int main(void) {
            return b;
        }
                """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));

  }
}
