package com.plasstech.lang.c.driver;

public class Java2C {

  public static void main(String args[]) {
    // TODO: instantiate the lexer & parser, read from stdin, and write to stdout
    System.out.println("   .globl main");
    System.out.println("main:");
    System.out.println("   movl $2, %eax");
    System.out.println("   ret");
  }
}
