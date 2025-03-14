package com.plasstech.lang.c.lex;

public class ScannerException extends RuntimeException {
  public ScannerException(Position position, String message) {
    super(String.format("Line %d, column %d: %s", position.line(), position.column(), message));
  }
}
