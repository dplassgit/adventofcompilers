package com.plasstech.lang.c.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.Token;
import com.plasstech.lang.c.lex.TokenType;

public class Parser {
  private final Scanner scanner;
  private Token token;
  private static final Set<TokenType> UNARY_TOKENS =
      ImmutableSet.of(TokenType.MINUS, TokenType.TWIDDLE, TokenType.BANG);

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
    // expect int identifier ( void ) { statements }
    expect(TokenType.INT);
    String functionName = token.value();
    expect(TokenType.IDENTIFIER);
    expect(TokenType.OPAREN);
    expect(TokenType.VOID);
    expect(TokenType.CPAREN);
    expect(TokenType.OBRACE);
    List<BlockItem> statements = parseStatements();
    expect(TokenType.CBRACE);

    return new FunctionDef(functionName, statements);
  }

  private List<BlockItem> parseStatements() {
    // return exp ;
    List<BlockItem> statements = new ArrayList<>();
    while (token.type() != TokenType.CBRACE) {
      BlockItem item = switch (token.type()) {
        case RETURN -> parseReturn();
        case INT -> parseDeclaration();
        case SEMICOLON -> {
          advance();
          yield new NullStatement();
        }
        default -> parseExpAsStatement();
      };
      statements.add(item);
    }
    return statements;
  }

  private Expression parseExpAsStatement() {
    Exp exp = parseExp();
    expect(TokenType.SEMICOLON);
    return new Expression(exp);
  }

  // int var [ = exp] ;
  private Declaration parseDeclaration() {
    expect(TokenType.INT);
    String varName = token.value();
    expect(TokenType.IDENTIFIER);
    if (token.type() == TokenType.SEMICOLON) {
      advance();
      return new Declaration(varName);
    }
    // Declaration with initialization
    expect(TokenType.EQ);
    Exp init = parseExp();
    expect(TokenType.SEMICOLON);
    return new Declaration(varName, init);
  }

  // return exp;
  private Return parseReturn() {
    expect(TokenType.RETURN);
    // This doesn't handle return; yet.
    Exp exp = parseExp();
    expect(TokenType.SEMICOLON);
    return new Return(exp);
  }

  private static final Map<TokenType, Integer> PRECEDENCES =
      ImmutableMap.<TokenType, Integer>builder()
          .put(TokenType.STAR, 50)
          .put(TokenType.SLASH, 50)
          .put(TokenType.PERCENT, 50)
          .put(TokenType.PLUS, 45)
          .put(TokenType.MINUS, 45)
          .put(TokenType.LT, 35)
          .put(TokenType.LEQ, 35)
          .put(TokenType.GT, 35)
          .put(TokenType.GEQ, 35)
          .put(TokenType.EQEQ, 30)
          .put(TokenType.NEQ, 30)
          .put(TokenType.DOUBLE_AMP, 10)
          .put(TokenType.DOUBLE_BAR, 5)
          .put(TokenType.EQ, 1).build();

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
    TokenType tt = token.type();
    switch (tt) {
      case IDENTIFIER -> {
        String variableName = token.value();
        advance();
        return new Var(variableName);
      }

      case OPAREN -> {
        advance();
        Exp innerExp = parseExp();
        expect(TokenType.CPAREN);
        return innerExp;
      }

      case INT_LITERAL -> {
        // Int literal
        String valueAsString = token.value();
        expect(TokenType.INT_LITERAL);
        int value = Integer.parseInt(valueAsString);
        return new Constant<Integer>(value);
      }

      case MINUS, TWIDDLE, BANG -> {
        // Unary
        advance();
        Exp innerExp = parseFactor();
        return new UnaryExp(tt, innerExp);
      }

      default -> {
        error("Unexpected token " + tt.name() + "; expected INT, unary operator or identifier");
        return null;
      }
    }
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
