package com.plasstech.lang.c.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
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
    List<FunDecl> funDecls = new ArrayList<>();
    while (token.type() != TokenType.EOF) {
      funDecls.add(parseFunDecl());

    }
    expect(TokenType.EOF);
    return new Program(funDecls);
  }

  private List<String> parseParamList() {
    return switch (token.type()) {
      case VOID -> {
        expect(TokenType.VOID);
        yield ImmutableList.of();
      }
      case INT -> {
        // parse list of params. Eventually this will be a list of pairs, shrug.
        List<String> params = new ArrayList<>();
        while (token.type() != TokenType.EOF) {
          if (params.size() > 0) {
            expect(TokenType.COMMA);
          }
          expect(TokenType.INT);
          if (token.type() != TokenType.IDENTIFIER) {
            error("Expected identifier, saw " + token);
            break;
          }
          params.add(token.value());
          expect(TokenType.IDENTIFIER);
          if (token.type() != TokenType.COMMA) {
            break;
          }
        }
        yield params;
      }
      default -> throw new IllegalArgumentException("Unexpected value: " + token.type());
    };
  }

  private Block parseBlock() {
    expect(TokenType.OBRACE);
    List<BlockItem> items = parseBlockItems();
    expect(TokenType.CBRACE);
    return new Block(items);
  }

  private List<BlockItem> parseBlockItems() {
    List<BlockItem> statements = new ArrayList<>();
    while (token.type() != TokenType.CBRACE) {
      BlockItem item = parseBlockItem();
      statements.add(item);
    }
    return statements;
  }

  private BlockItem parseBlockItem() {
    if (token.type() == TokenType.INT) {
      return parseDeclaration();
    }
    return parseStatement();
  }

  private Statement parseStatement() {
    Statement item = switch (token.type()) {
      case RETURN -> parseReturn();
      case SEMICOLON -> {
        advance();
        yield new NullStatement();
      }
      case IF -> parseIf();
      case CONTINUE -> parseContinue();
      case BREAK -> parseBreak();
      case WHILE -> parseWhile();
      case DO -> parseDo();
      case FOR -> parseFor();
      case OBRACE -> new Compound(parseBlock());
      default -> parseExpAsStatement();
    };
    return item;
  }

  private ForInit parseForInit() {
    // Is there a better way to do this? I fear...
    if (token.type() == TokenType.INT) {
      return new InitDecl(parseVarDeclaration());
    }
    return new InitExp(parseOptionalExp(TokenType.SEMICOLON));
  }

  private Optional<Exp> parseOptionalExp(TokenType separator) {
    Optional<Exp> maybeExp = Optional.empty();
    if (token.type() != separator) {
      maybeExp = Optional.of(parseExp());
    }
    expect(separator);
    return maybeExp;
  }

  // for(for-init [exp] ; [exp] ) statement. Page 150
  private For parseFor() {
    expect(TokenType.FOR);
    expect(TokenType.OPAREN);

    ForInit forInit = parseForInit();
    Optional<Exp> condition = parseOptionalExp(TokenType.SEMICOLON);
    Optional<Exp> post = parseOptionalExp(TokenType.CPAREN);
    Statement body = parseStatement();

    return new For(forInit, condition, post, body);
  }

  private DoWhile parseDo() {
    expect(TokenType.DO);
    Statement body = parseStatement();
    expect(TokenType.WHILE);
    expect(TokenType.OPAREN);
    Exp exp = parseExp();
    expect(TokenType.CPAREN);
    expect(TokenType.SEMICOLON);

    return new DoWhile(body, exp);
  }

  private While parseWhile() {
    expect(TokenType.WHILE);
    expect(TokenType.OPAREN);
    Exp exp = parseExp();
    expect(TokenType.CPAREN);
    Statement body = parseStatement();
    return new While(exp, body);
  }

  private Break parseBreak() {
    expect(TokenType.BREAK);
    expect(TokenType.SEMICOLON);
    return new Break();
  }

  private Continue parseContinue() {
    expect(TokenType.CONTINUE);
    expect(TokenType.SEMICOLON);
    return new Continue();
  }

  private If parseIf() {
    expect(TokenType.IF);
    expect(TokenType.OPAREN);
    Exp exp = parseExp();
    expect(TokenType.CPAREN);
    Statement then = parseStatement();
    Optional<Statement> elseStmt = Optional.empty();
    if (token.type() == TokenType.ELSE) {
      advance();
      elseStmt = Optional.of(parseStatement());
    }
    return new If(exp, then, elseStmt);
  }

  private Expression parseExpAsStatement() {
    Exp exp = parseExp();
    expect(TokenType.SEMICOLON);
    return new Expression(exp);
  }

  // TODO: right now this only parses block-level function declarations/definitions.
  private Declaration parseDeclaration() {
    // TODO: get the storage classes
    StorageClass storageClass = StorageClass.AUTO;
    expect(TokenType.INT);
    String varName = token.value();
    expect(TokenType.IDENTIFIER);
    if (token.type() == TokenType.SEMICOLON) {
      advance();
      return new VarDecl(varName, storageClass);
    }
    if (token.type() == TokenType.EQ) {
      //  Declaration with initialization
      expect(TokenType.EQ);
      Exp init = parseExp();
      expect(TokenType.SEMICOLON);
      return new VarDecl(varName, init, storageClass);
    }
    return parseFunDeclAfterName(varName);
  }

  private FunDecl parseFunDeclAfterName(String functionName) {
    expect(TokenType.OPAREN);
    List<String> params = parseParamList();
    expect(TokenType.CPAREN);
    Optional<Block> block = Optional.empty();
    if (token.type() != TokenType.SEMICOLON) {
      block = Optional.of(parseBlock());
    } else {
      expect(TokenType.SEMICOLON);
    }

    return new FunDecl(functionName, params, block, StorageClass.AUTO);
  }

  private FunDecl parseFunDecl() {
    // expect int identifier ( void ) { statements }
    expect(TokenType.INT);
    String functionName = token.value();
    expect(TokenType.IDENTIFIER);
    return parseFunDeclAfterName(functionName);
  }

  // int var [ = exp] ;
  private VarDecl parseVarDeclaration() {
    // TODO: get the storage classes
    StorageClass storageClass = StorageClass.AUTO;
    expect(TokenType.INT);
    String varName = token.value();
    expect(TokenType.IDENTIFIER);
    if (token.type() == TokenType.SEMICOLON) {
      advance();
      return new VarDecl(varName, storageClass);
    }
    // Declaration with initialization
    expect(TokenType.EQ);
    Exp init = parseExp();
    expect(TokenType.SEMICOLON);
    return new VarDecl(varName, init, storageClass);
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
          .put(TokenType.QUESTION, 3)
          .put(TokenType.EQ, 1).build();

  private Exp parseExp() {
    return parseExp(0);
  }

  private Exp parseExp(int minPrec) {
    Exp left = parseFactor();
    while (PRECEDENCES.containsKey(token.type()) && PRECEDENCES.get(token.type()) >= minPrec) {
      TokenType tt = token.type();
      advance();
      if (tt == TokenType.EQ) {
        // Stay same precedence
        Exp right = parseExp(PRECEDENCES.get(tt));
        left = new Assignment(left, right);
      } else if (tt == TokenType.QUESTION) {
        Exp middle = parseConditionalMiddle();
        Exp right = parseExp(PRECEDENCES.get(tt));
        left = new Conditional(left, middle, right);
      } else {
        Exp right = parseExp(PRECEDENCES.get(tt) + 1);
        left = new BinExp(left, tt, right);
      }
    }
    return left;
  }

  private Exp parseConditionalMiddle() {
    // we already have the condition
    Exp exp = parseExp();
    expect(TokenType.COLON);
    return exp;
  }

  private Exp parseFactor() {
    TokenType tt = token.type();
    return switch (tt) {
      case IDENTIFIER -> parseVarOrFnCall();

      case OPAREN -> {
        advance();
        Exp innerExp = parseExp();
        expect(TokenType.CPAREN);
        yield innerExp;
      }

      case INT_LITERAL -> {
        // Int literal
        String valueAsString = token.value();
        expect(TokenType.INT_LITERAL);
        int value = Integer.parseInt(valueAsString);
        yield new Constant<Integer>(value);
      }

      case MINUS, TWIDDLE, BANG -> {
        // Unary
        advance();
        Exp innerExp = parseFactor();
        yield new UnaryExp(tt, innerExp);
      }

      default -> {
        error("Unexpected token " + tt.name() + "; expected INT, unary operator or identifier");
        yield null;
      }
    };
  }

  private Exp parseVarOrFnCall() {
    String variableName = token.value();
    advance();
    if (token.type() != TokenType.OPAREN) {
      return new Var(variableName);
    }

    // function call
    expect(TokenType.OPAREN);
    List<Exp> args = new ArrayList<>();
    while (token.type() != TokenType.CPAREN && token.type() != TokenType.EOF) {
      if (args.size() > 0) {
        expect(TokenType.COMMA);
      }
      args.add(parseExp());
    }
    expect(TokenType.CPAREN);
    return new FunctionCall(variableName, args);
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
