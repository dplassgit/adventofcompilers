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
  STAR("*"),
  PLUS("+"),
  MINUS("-"),
  DECREMENT("--"),
  TWIDDLE("~"),
  SLASH("/"),
  PERCENT("%"),
  BANG("!"),
  DOUBLE_BAR("||"),
  DOUBLE_AMP("&&"),
  BAR("|"),
  AMP("&"),
  EQEQ("==", true),
  GT(">", true),
  GEQ(">=", true),
  LT("<", true),
  LEQ("<=", true),
  NEQ("!=", true),
  SEMICOLON(";");

  public final String text;
  public final boolean isKeyword;
  public final boolean isConditional;

  TokenType() {
    this(false);
  }

  TokenType(boolean kw) {
    this.text = null;
    this.isConditional = false;
    this.isKeyword = kw;
  }

  TokenType(String text) {
    this.text = text;
    this.isConditional = false;
    this.isKeyword = false;
  }

  TokenType(String text, boolean conditional) {
    this.text = text;
    this.isConditional = conditional;
    this.isKeyword = false;
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
