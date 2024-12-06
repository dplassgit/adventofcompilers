package com.plasstech.lang.c.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.ScannerException;
import com.plasstech.lang.c.lex.Token;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.parser.Parser;
import com.plasstech.lang.c.parser.ParserException;

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
    String input = readStdin();
    Scanner s = new Scanner(input);
    if (args.length > 1) {
      if (args[1].equals("--lex")) {
        System.exit(justLex(s));
      }
      if (args[1].equals("--parse")) {
        System.exit(justParse(s));
      }
    }
    System.out.println("   .globl main");
    System.out.println("main:");
    System.out.println("   movl $2, %eax");
    System.out.println("   ret");
  }

  private static int justParse(Scanner s) {
    Parser p = new Parser(s);
    try {
      p.parse();
      return 0;
    } catch (ParserException pe) {
      System.err.println(pe.getMessage());
      return -1;
    }
  }

  private static int justLex(Scanner s) {
    // Run the lexer
    try {
      Token t = s.nextToken();
      while (t.type() != TokenType.EOF) {
        //        System.err.printf("Token type %s%s value %s\n", t.type().toString(),
        //            t.isKeyword() ? " (keyword)" : "", t.value());
        t = s.nextToken();
      }
      return 0;
    } catch (ScannerException se) {
      System.err.println(se.getMessage());
      return -1;
    }
  }
}
