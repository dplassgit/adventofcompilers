package com.plasstech.lang.c.typecheck;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class TypeCheckerTest {
  private SymbolTable symbols = new SymbolTable();
  private Validator validator = new TypeChecker(symbols);

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
    assertThat(s.defined()).isTrue();
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
    assertThat(s.defined()).isFalse();
  }

  @Test
  public void fnDeclWithParams() {
    String input = "int main(int a, int b);";
    validate(input);
    Symbol s = symbols.get("main");
    assertThat(s).isNotNull();
    assertThat(s.defined()).isFalse();
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
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("Incompatible function declarations");
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
    assertThat(e.getMessage()).contains("Cannot define `extern` function 'fn'");
  }
}
