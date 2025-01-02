package com.plasstech.lang.c.typecheck;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class LoopLabelerTest {
  private Validator validator = new LoopLabeler();

  private Program validate(String input) {
    return validator.validate(new Parser(new Scanner(input)).parse());
  }

  @Test
  public void noLoopsOk() {
    String input = """
        int main(void) {
          return -1;
        }
        """;
    Program program = new Parser(new Scanner(input)).parse();
    assertThat(validator.validate(program)).isEqualTo(program);
  }

  @Test
  public void breakNoLoopError() {
    String input = """
        int main(void) {
          break;
          return -1;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void continueNoLoopError() {
    String input = """
        int main(void) {
          continue;
          return -1;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void breakOutsideLoopError() {
    String input = """
        int main(void) {
          while(1) {}
          break;
          return -1;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void continueOutsideLoopError() {
    String input = """
        int main(void) {
          while(1) {}
          continue;
          return -1;
        }
        """;
    assertThrows(SemanticAnalyzerException.class, () -> validate(input));
  }

  @Test
  public void whileOk() {
    String input = """
        int main(void) {
          while(1) {}
          return -1;
        }
        """;
    validate(input);
  }

  @Test
  public void whileBreakOk() {
    String input = """
        int main(void) {
          while(1) {
            break;
          }
          return -1;
        }
        """;
    validate(input);
  }

  @Test
  public void whileContinueOk() {
    String input = """
        int main(void) {
          while(1) {
            break;
          }
          return -1;
        }
        """;
    validate(input);
  }

  @Test
  public void whileBreakContinueOk() {
    String input = """
        int main(void) {
          while(1) {
            break;
            continue;
          }
          return -1;
        }
        """;
    validate(input);
  }

  @Test
  public void doWhileBreakContinueOk() {
    String input = """
        int main(void) {
          do {
            break;
            continue;
          } while(1);
          return -1;
        }
        """;
    validate(input);
  }

  @Test
  public void forBreakContinueOk() {
    String input = """
        int main(void) {
          for(;;)  {
            break;
            continue;
          }
          return -1;
        }
        """;
    validate(input);
  }

  @Test
  public void nestedLoops() {
    String input = """
        int main(void) {
          for(;;)  {
            continue;
            while(1) { continue; }
            do { continue; } while(1);
          }
          return -1;
        }
        """;
    validate(input);
  }

  @Test
  public void externalFunCall() {
    String input = """
        int decl(int a);
        int main(void) {
          return decl(1);
        }""";
    validate(input);
  }

  @Test
  public void internalFunCall() {
    String input = """
        int decl(int a) {
          return 1;
        }
        int main(void) {
          return decl(1);
        }""";
    System.err.println(validate(input));
  }
}
