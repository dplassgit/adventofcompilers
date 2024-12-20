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
}
