package com.plasstech.lang.c.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.base.Joiner;
import com.plasstech.lang.c.codegen.AsmCodeGen;
import com.plasstech.lang.c.codegen.AsmProgramNode;
import com.plasstech.lang.c.codegen.CodeGen;
import com.plasstech.lang.c.codegen.tacky.TackyCodeGen;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.ScannerException;
import com.plasstech.lang.c.lex.Token;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.ParserException;
import com.plasstech.lang.c.parser.PrettyPrinter;
import com.plasstech.lang.c.parser.Program;

public class Driver {

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
          justLex(s);
        }
        if (args[1].equals("--parse")) {
          justParse(s);
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
    Program program = justParse(s);
    // This throws on error, doesn't really generate anything
    new TackyCodeGen().generate(program);
  }

  private static List<String> generateAsm(Scanner s) {
    AsmProgramNode program = codeGen(s);
    AsmCodeGen acg = new AsmCodeGen();
    return acg.generate(program);
  }

  private static AsmProgramNode codeGen(Scanner s) {
    Program program = justParse(s);
    return new CodeGen().generate(program);
  }

  private static void prettyPrint(Scanner s) {
    Program program = justParse(s);
    new PrettyPrinter().prettyPrint(program);
  }

  private static Program justParse(Scanner s) {
    Parser p = new Parser(s);
    return p.parse();
  }

  private static void justLex(Scanner s) {
    // Run the lexer
    Token t = s.nextToken();
    while (t.type() != TokenType.EOF) {
      t = s.nextToken();
    }
  }
}
