package com.plasstech.lang.c.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.codegen.AsmProgram;
import com.plasstech.lang.c.codegen.CodeEmission;
import com.plasstech.lang.c.codegen.tacky.AsmState;
import com.plasstech.lang.c.codegen.tacky.TackyCodeGen;
import com.plasstech.lang.c.codegen.tacky.TackyProgram;
import com.plasstech.lang.c.codegen.tacky.TackyToAsmCodeGen;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.ScannerException;
import com.plasstech.lang.c.lex.Token;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.ParserException;
import com.plasstech.lang.c.parser.PrettyPrinter;
import com.plasstech.lang.c.parser.Program;
import com.plasstech.lang.c.typecheck.SemanticAnalyzer;
import com.plasstech.lang.c.typecheck.SymbolTable;

public class Driver {

  public static void main(String args[]) {
    new Driver().run(args);
  }

  private Program program;
  private final SymbolTable symbolTable = new SymbolTable();
  private final SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(symbolTable);

  private void run(String args[]) {
    try {
      String input = readStdin();
      Scanner s = new Scanner(input);
      if (args.length > 1) {
        if (args[1].equals("--lex")) {
          scan(s);
        }
        if (args[1].equals("--parse")) {
          parse(s);
        }
        if (args[1].equals("--validate")) {
          validate(s);
        }
        if (args[1].equals("--codegen")) {
          codeGen(s);
        }
        if (args[1].equals("--tacky")) {
          tackyCodeGen(s);
        }
        if (args[1].equals("--prettyprint")) {
          prettyPrint(s);
        }
        return;
      }

      // Generate asm:
      List<String> asm = generateAsm(s);
      System.out.println(Joiner.on('\n').join(asm));
    } catch (ParserException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    } catch (ScannerException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
  }

  private static String readStdin() {
    String input = "";
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    try {
      String line = reader.readLine();
      while (line != null) {
        input += line + "\n";
        line = reader.readLine();
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not read standard in", e);
    }
    return input;
  }

  private void tackyCodeGen(Scanner s) {
    validate(s);
    // This doesn't output anything.
    new TackyCodeGen(symbolTable).generate(program);
  }

  private List<String> generateAsm(Scanner s) {
    validate(s);
    TackyProgram tp = new TackyCodeGen(symbolTable).generate(program);
    AsmState state = new TackyToAsmCodeGen(symbolTable).generate(tp);
    return new CodeEmission(symbolTable).generate(state.program());
  }

  private AsmProgram codeGen(Scanner s) {
    validate(s);
    TackyProgram tp = new TackyCodeGen(symbolTable).generate(program);
    TackyToAsmCodeGen tackyToAsmCodeGen = new TackyToAsmCodeGen(symbolTable);
    AsmState asmState = tackyToAsmCodeGen.generate(tp);
    return asmState.program();
  }

  private void validate(Scanner s) {
    Program initialProgram = parse(s);
    this.program = semanticAnalyzer.validate(initialProgram);
  }

  private static void prettyPrint(Scanner s) {
    Program program = parse(s);
    new PrettyPrinter().prettyPrint(program);
  }

  private static Program parse(Scanner s) {
    Parser p = new Parser(s);
    return p.parse();
  }

  private static void scan(Scanner s) {
    // Run the scanner
    Token t = s.nextToken();
    while (t.type() != TokenType.EOF) {
      t = s.nextToken();
    }
  }
}
