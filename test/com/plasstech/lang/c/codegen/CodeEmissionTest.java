package com.plasstech.lang.c.codegen;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.codegen.tacky.AsmState;
import com.plasstech.lang.c.codegen.tacky.TackyCodeGen;
import com.plasstech.lang.c.codegen.tacky.TackyProgram;
import com.plasstech.lang.c.codegen.tacky.TackyToAsmCodeGen;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.typecheck.SemanticAnalyzer;
import com.plasstech.lang.c.typecheck.SymbolTable;

public class CodeEmissionTest {
  private SymbolTable symtab = new SymbolTable();

  private List<String> generate(String input) {
    Scanner s = new Scanner(input);
    Parser p = new Parser(s);

    Program prog = p.parse();
    SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(symtab);
    prog = semanticAnalyzer.validate(prog);
    TackyCodeGen cg = new TackyCodeGen(symtab);
    TackyProgram program = cg.generate(prog);
    System.err.println(program);
    TackyToAsmCodeGen cg2 = new TackyToAsmCodeGen(symtab);
    AsmState asmState = cg2.generate(program);
    CodeEmission ce = new CodeEmission(symtab);
    AsmProgram asmProgram = asmState.program();
    System.err.println(asmProgram);
    return ce.generate(asmProgram);
  }

  @Test
  public void multiplyBig() {
    String input = """
        long glob = 5l;

        int main(void) {
            // The multiply operation whose result we want to spill;
            // this is our best spill candidate b/c it has the most conflicts with other
            // pseudos and is tied for fewest uses. NOTE: optimizations must be enabled
            // so we propagate the temporary variable holding the result of this
            // expression instead of copying it into should_spill.
            long should_spill = glob * 4294967307l;
            return 0;
            }
        """;
    List<String> asm = generate(input);
    System.err.println(Joiner.on('\n').join(asm));
  }

  @Test
  public void cmpBig() {
    String input = """
        int compare_constants_2(void) {
            /* This exercises the rewrite rule for cmp where src is a large constant
             * and dst is a constant, because 8589934593 can't fit in an int.
             */
            return 255l < 8589934593l;
        }""";
    List<String> asm = generate(input);
    System.err.println(Joiner.on('\n').join(asm));
  }

}
