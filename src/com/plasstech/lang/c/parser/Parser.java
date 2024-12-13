package com.plasstech.lang.c.parser;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
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

  private static final Map<TokenType, Integer> PRECEDENCES = ImmutableMap.of(
      TokenType.PLUS, 45,
      TokenType.MINUS, 45,
      TokenType.STAR, 50,
      TokenType.SLASH, 50,
      TokenType.PERCENT, 50);

  private Exp parseExp() {
    return parseExp(0);
  }

  private Exp parseExp(int minPrec) {
    Exp left = parseFactor();
    while (PRECEDENCES.containsKey(token.type()) && PRECEDENCES.get(token.type()) >= minPrec) {
      TokenType tt = token.type();
      advance();
      Exp right = parseExp(PRECEDENCES.get(tt) + 1);
      left = new BinExp(left, tt, right);
    }
    return left;
  }

  private Exp parseFactor() {
    if (token.type() == TokenType.OPAREN) {
      advance();
      Exp innerExp = parseExp();
      expect(TokenType.CPAREN);
      return innerExp;
    }
    if (token.type() == TokenType.MINUS || token.type() == TokenType.TWIDDLE) {
      TokenType tt = token.type();
      advance();
      Exp innerExp = parseFactor();
      return new UnaryExp(tt, innerExp);
    }

    // Int literal
    String valueAsString = token.value();
    expect(TokenType.INT_LITERAL);
    int value = Integer.parseInt(valueAsString);
    return new Constant<Integer>(value);
  }

  private static void error(String message) {
    throw new ParserException(message);
  }

  private void expect(TokenType tt) {
    if (token.type() == tt) {
      advance();
      return;
    }
    error(String.format("Expected `%s`, saw `%s`", tt.toString(), token.type().toString()));
  }
}
