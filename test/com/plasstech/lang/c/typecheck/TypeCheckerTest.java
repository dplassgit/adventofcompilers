package com.plasstech.lang.c.typecheck;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Ignore;
import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.FunDecl;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.parser.Return;

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
      assertThat(f.paramTypes().size()).isEqualTo(2);
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
    assertThat(e.getMessage()).contains("Cannot define `extern` function 'fn'");
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
    assertThat(e.getMessage()).contains("Conflicting types for 'foo'");
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
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("'foo' already defined");
  }

  @Test
  public void goodCast() {
    String input = """
        int main(void) {
          int a = 3;
          return (long) a;
        }
        """;
    validate(input);
  }

  @Test
  public void notLongBecomesInt() {
    String input = """
        int main(long a) {
          return !a;
        }
        """;
    Program program = validate(input);
    FunDecl fd = (FunDecl) program.declarations().get(0);
    Return r = (Return) fd.nthItem(0);
    assertThat(r.exp().type()).isEqualTo(Type.INT);
  }

  @Test
  public void binLongAndIntBecomesLong() {
    String input = """
        long main(long a) {
          int b = 1;
          return b + a;
        }
        """;
    Program program = validate(input);
    FunDecl fd = (FunDecl) program.declarations().get(0);
    Return r = (Return) fd.nthItem(1);
    assertThat(r.exp().type()).isEqualTo(Type.LONG);
  }

  @Test
  public void returnTypeRules() {
    String input = """
        int main(long a) {
          int b = 1;
          return b + a;
        }
        """;
    Program program = validate(input);
    FunDecl fd = (FunDecl) program.declarations().get(0);
    Return r = (Return) fd.nthItem(1);
    assertThat(r.exp().type()).isEqualTo(Type.INT);
  }

  @Test
  public void redeclareFileScopeVarAsDifferentType() {
    String input = """
        extern int foo;
        extern long foo;
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("Conflicting types for 'foo'");
  }

  @Test
  public void castIsNotAnLvalue() {
    String input = """
        int main(void) {
            int i = 0;
            i = (long) i = 10;
            return 0;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void conflictingVariableTypesExtern() {
    String input = """
        long foo;

        int main(void) {
            /* This declaration refers to the global 'foo' variable,
             * but has a conflicting type.
             */
            extern int foo;
            return 0;
        }
        """;
    SemanticAnalyzerException e =
        assertThrows(SemanticAnalyzerException.class, () -> validate(input));
    assertThat(e.getMessage()).contains("Conflicting types for 'foo'");
  }

  @Test
  public void unsignedIntAndIntBecomesUnsignedInt() {
    String input = """
        unsigned int main(unsigned int a) {
          int b = 1;
          return b + a;
        }
        """;
    Program program = validate(input);
    FunDecl fd = (FunDecl) program.declarations().get(0);
    Return r = (Return) fd.nthItem(1);
    assertThat(r.exp().type()).isEqualTo(Type.UNSIGNED_INT);
  }

  @Test
  public void unsignedIntAndUnsignedLongBecomesUnsignedLong() {
    String input = """
        unsigned long main(unsigned long a) {
          unsigned int b = 1u;
          return b + a;
        }
        """;
    Program program = validate(input);
    FunDecl fd = (FunDecl) program.declarations().get(0);
    Return r = (Return) fd.nthItem(1);
    assertThat(r.exp().type()).isEqualTo(Type.UNSIGNED_LONG);
  }

  @Test
  public void unsignedIntAndLongBecomesLong() {
    String input = """
        unsigned int main(unsigned int a) {
          long b = 1L;
          return b + a; // it actually IS a long but then it gets cast AGAIN to the return type
        }
        """;
    Program program = validate(input);
    FunDecl fd = (FunDecl) program.declarations().get(0);
    Return r = (Return) fd.nthItem(1);
    assertThat(r.exp().type()).isEqualTo(Type.UNSIGNED_INT);
  }

  @Test
  public void unsignedLongAndLongBecomesUnsignedLong() {
    String input = """
        unsigned long main(unsigned long a) {
          signed long b = 1L;
          return b + a;
        }
        """;
    Program program = validate(input);
    FunDecl fd = (FunDecl) program.declarations().get(0);
    Return r = (Return) fd.nthItem(1);
    assertThat(r.exp().type()).isEqualTo(Type.UNSIGNED_LONG);
  }

  @Test
  public void staticInitUnsignedIntOverflow() {
    String input = """
        static unsigned int u = 4294967299L;
        """;
    validate(input);
    Symbol u = symbols.get("u");
    StaticAttr attr = (StaticAttr) u.attribute();
    assertThat(attr.init()).isInstanceOf(Initializer.class);
    Initializer initialValue = (Initializer) attr.init();
    assertThat(initialValue.staticInit()).isInstanceOf(UIntInit.class);
    UIntInit staticInit = (UIntInit) initialValue.staticInit();
    // whoa, this actually works. Thanks Java.
    assertThat(staticInit.value()).isEqualTo(3);
  }

  @Test
  public void staticInitIntOverflow() {
    String input = """
        static int u = 4294967246u;
        """;
    validate(input);
    Symbol u = symbols.get("u");
    StaticAttr attr = (StaticAttr) u.attribute();
    assertThat(attr.init()).isInstanceOf(Initializer.class);
    Initializer initialValue = (Initializer) attr.init();
    assertThat(initialValue.staticInit()).isInstanceOf(IntInit.class);
    IntInit staticInit = (IntInit) initialValue.staticInit();
    // whoa, this actually works. Thanks Java.
    assertThat(staticInit.value()).isEqualTo(-50);
  }
}
