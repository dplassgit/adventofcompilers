package com.plasstech.lang.c.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.codegen.AsmCodeGen;
import com.plasstech.lang.c.codegen.AsmProgramNode;
import com.plasstech.lang.c.codegen.CodeGen;
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
import com.plasstech.lang.c.typecheck.Symbol;

public class Driver {

  private static Map<String, Symbol> symbolTable;

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

  public static void main(String args[]) {
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

  private static void tackyCodeGen(Scanner s) {
    Program program = validate(s);
    // This throws on error, doesn't really generate anything
    new TackyCodeGen().generate(program);
  }

  private static List<String> generateAsm(Scanner s) {
    Program prog = validate(s);
    TackyProgram tp = new TackyCodeGen().generate(prog);
    AsmProgramNode an = new TackyToAsmCodeGen().generate(tp);
    return new AsmCodeGen(symbolTable).generate(an);
  }

  private static AsmProgramNode codeGen(Scanner s) {
    Program program = validate(s);
    // This only does chapter 1. Not sure if it ever should be run after chapter 1...
    return new CodeGen().generate(program);
  }

  private static void prettyPrint(Scanner s) {
    Program program = parse(s);
    new PrettyPrinter().prettyPrint(program);
  }

  private static Program validate(Scanner s) {
    Parser p = new Parser(s);
    Program program = p.parse();
    SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
    Program output = semanticAnalyzer.validate(program);
    symbolTable = semanticAnalyzer.symbolTable();
    return output;
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
