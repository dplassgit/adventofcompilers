package com.plasstech.lang.c.typecheck;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class ResolverTest {
  private static Program parse(String program) {
    return new Parser(new Scanner(program)).parse();
  }

  @Test
  public void noVariables_ok() {
    String input = """
        int main(void) {
          return -1;
        }
        """;
    Resolver resolver = new Resolver();
    Program program = parse(input);
    assertThat(resolver.validate(program)).isEqualTo(program);
  }

  @Test
  public void declared() {
    String input = """
        int main(void) {
          int a = 3;
          return a+1;
        }
        """;
    Resolver resolver = new Resolver();
    Program program = parse(input);
    System.err.println(resolver.validate(program));
  }

  @Test
  public void notDeclaredYet_error() {
    String input = """
        int main(void) {
          a=4;
        }
        """;
    Resolver resolver = new Resolver();
    Program program = parse(input);
    assertThrows(ResolverException.class, () -> resolver.validate(program));
  }

  @Test
  public void duplicateDeclaration_error() {
    String input = """
        int main(void) {
          int a=4;
          int a; // ERROR
        }
        """;
    Resolver resolver = new Resolver();
    Program program = parse(input);
    assertThrows(ResolverException.class, () -> resolver.validate(program));
  }

  @Test
  public void numericLvalue_error() {
    String input = """
        int main(void) {
          4=a;
        }
        """;
    Resolver resolver = new Resolver();
    Program program = parse(input);
    assertThrows(ResolverException.class, () -> resolver.validate(program));
  }

  @Test
  public void expLvalue_error() {
    String input = """
        int main(void) {
          int b;
          (1+b)=a;
        }
        """;
    Resolver resolver = new Resolver();
    Program program = parse(input);
    assertThrows(ResolverException.class, () -> resolver.validate(program));
  }

  @Test
  public void ownInit() {
    String input = """
        int main(void) {
          int b = b + 1;
        }
        """;
    Resolver resolver = new Resolver();
    Program program = parse(input);
    resolver.validate(program);
  }

  @Test
  public void cond() {
    String input = """
        int main(void) {
          int b = 1?2:3;
        }
        """;
    Resolver resolver = new Resolver();
    Program program = parse(input);
    System.err.println(resolver.validate(program));
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
    Resolver resolver = new Resolver();
    Program program = parse(input);
    System.err.println(resolver.validate(program));
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
    Resolver resolver = new Resolver();
    Program program = parse(input);
    assertThrows(ResolverException.class, () -> resolver.validate(program));
  }

  @Test
  public void undeclaredVarInIf() {
    String input = """
        int main(void) {
            if (c == 0) return 1;
            return 0;
        }""";
    Resolver resolver = new Resolver();
    Program program = parse(input);
    assertThrows(ResolverException.class, () -> resolver.validate(program));
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
    Resolver resolver = new Resolver();
    Program program = parse(input);
    System.err.println(resolver.validate(program));
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
    Resolver resolver = new Resolver();
    Program program = parse(input);
    System.err.println(resolver.validate(program));
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
    Resolver resolver = new Resolver();
    Program program = parse(input);
    assertThrows(ResolverException.class, () -> resolver.validate(program));
  }
}
