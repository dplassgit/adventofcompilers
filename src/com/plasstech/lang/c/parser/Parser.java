package com.plasstech.lang.c.parser;

import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.Token;
import com.plasstech.lang.c.lex.TokenType;

public class Parser {
  private final Scanner scanner;
  private Token token;

  public Parser(Scanner scanner) {
    this.scanner = scanner;
    advance();
  }

  private void advance() {
    this.token = scanner.nextToken();
  }

  public Program parse() {
    FunctionDef fun = parseFunction();
    expect(TokenType.EOF);
    return new Program(fun);
  }

  private FunctionDef parseFunction() {
    // expect int identifier ( void ) { statement }
    expect(TokenType.INT);
    String varName = token.value();
    expect(TokenType.IDENTIFIER);
    expect(TokenType.OPAREN);
    expect(TokenType.VOID);
    expect(TokenType.CPAREN);
    expect(TokenType.OBRACE);
    Statement statement = parseStatement();
    expect(TokenType.CBRACE);

    return new FunctionDef(varName, statement);
  }

  private Statement parseStatement() {
    // return exp ;
    expect(TokenType.RETURN);
    Exp exp = parseExp();
    expect(TokenType.SEMICOLON);
    return new Return(exp);
  }

  private Exp parseExp() {
    // Only int literal
    String valueAsString = token.value();
    expect(TokenType.INT_LITERAL);
    int value = Integer.parseInt(valueAsString);
    return new Constant<Integer>(value);
  }

  private void error(String message) {
    throw new ParserException(message);
  }

  private void expect(TokenType tt) {
    if (token.type() == tt) {
      advance();
      return;
    }
    error(String.format("Expected %s, saw %s", tt.name(), token.type().name()));
  }
}
