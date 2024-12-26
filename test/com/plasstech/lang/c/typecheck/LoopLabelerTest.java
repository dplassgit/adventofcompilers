package com.plasstech.lang.c.typecheck;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;

public class LoopLabelerTest {
  private static Program parse(String program) {
    return new Parser(new Scanner(program)).parse();
  }

  private LoopLabeler validator = new LoopLabeler();

  @Test
  public void noLoopsOk() {
    String input = """
        int main(void) {
          return -1;
        }
        """;
    Program program = parse(input);
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
    Program program = parse(input);
    assertThrows(SemanticAnalyzerException.class, () -> validator.validate(program));
  }

  @Test
  public void continueNoLoopError() {
    String input = """
        int main(void) {
          continue;
          return -1;
        }
        """;
    Program program = parse(input);
    assertThrows(SemanticAnalyzerException.class, () -> validator.validate(program));
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
    Program program = parse(input);
    assertThrows(SemanticAnalyzerException.class, () -> validator.validate(program));
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
    Program program = parse(input);
    assertThrows(SemanticAnalyzerException.class, () -> validator.validate(program));
  }

  @Test
  public void whileOk() {
    String input = """
        int main(void) {
          while(1) {}
          return -1;
        }
        """;
    Program program = parse(input);
    validator.validate(program);
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
    Program program = parse(input);
    validator.validate(program);
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
    Program program = parse(input);
    validator.validate(program);
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
    Program program = parse(input);
    validator.validate(program);
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
    Program program = parse(input);
    System.err.println(validator.validate(program));
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
    Program program = parse(input);
    System.err.println(validator.validate(program));
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
    Program program = parse(input);
    System.err.println(validator.validate(program));
  }
}
