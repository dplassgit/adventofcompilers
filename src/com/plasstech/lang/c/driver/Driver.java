package com.plasstech.lang.c.driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.Token;
import com.plasstech.lang.c.lex.TokenType;

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
    if (args.length > 1 && args[1].equals("--lex")) {
      String input = readStdin();

      // Run the lexer
      Scanner s = new Scanner(input);
      Token t = s.nextToken();
      while (t.type() != TokenType.EOF && t.type() != TokenType.ERROR) {
        //        System.err.printf("Token type %s%s value %s\n", t.type().toString(),
        //            t.isKeyword() ? " (keyword)" : "", t.value());
        t = s.nextToken();
      }
      if (t.type() == TokenType.ERROR) {
        System.exit(-1);
      }
      System.exit(0);
    }
    // TODO: instantiate the lexer & parser, read from stdin, and write to stdout
    System.out.println("   .globl main");
    System.out.println("main:");
    System.out.println("   movl $2, %eax");
    System.out.println("   ret");
  }
}
