package com.plasstech.lang.c.lex;

public enum TokenType {
  EOF,
  INT(true),
  RETURN(true),
  VOID(true),
  IDENTIFIER,
  //  STRING_LITERAL,
  INT_LITERAL,
  OBRACE("{"),
  CBRACE("}"),
  OPAREN("("),
  CPAREN(")"),
  //  OBRACK("["),
  //  CBRACK("]"),
  //  COMMA(","),
  //  STAR("*"),
  //  PLUS("+"),
  MINUS("-"),
  DECREMENT("--"),
  TWIDDLE("~"),
  //  SLASH("/"),
  SEMICOLON(";");

  public final String text;
  public final boolean isKeyword;

  TokenType(String text) {
    this.text = text;
    this.isKeyword = false;
  }

  TokenType() {
    this(false);
  }

  TokenType(boolean kw) {
    this.text = null;
    this.isKeyword = kw;
  }

  public boolean isSymbol() {
    return !isKeyword && text != null;
  }

  @Override
  public String toString() {
    if (isSymbol()) {
      return text;
    }
    if (isKeyword) {
      return name().toLowerCase();
    }
    return name();
  }
}
