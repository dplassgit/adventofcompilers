package com.plasstech.lang.c.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.plasstech.lang.c.lex.Scanner;
import com.plasstech.lang.c.lex.Token;
import com.plasstech.lang.c.lex.TokenType;
import com.plasstech.lang.c.typecheck.FunType;
import com.plasstech.lang.c.typecheck.Type;

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
    List<Declaration> fileLevelDecls = new ArrayList<>();
    while (token.type() != TokenType.EOF) {
      fileLevelDecls.add(parseDeclaration());
    }
    expect(TokenType.EOF);
    return new Program(fileLevelDecls);
  }

  private List<Type> parseTypeSpecifiers() {
    List<Type> types = new ArrayList<>();
    while (token.type() != TokenType.EOF
        && (token.type() == TokenType.INT || token.type() == TokenType.LONG)) {
      types.add(Type.fromTokenType(token.type()));
      advance();
    }

    return types;
  }

  private List<Param> parseParamList() {
    return switch (token.type()) {
      case VOID -> {
        expect(TokenType.VOID);
        yield ImmutableList.of();
      }
      case INT, LONG -> {
        List<Param> params = new ArrayList<>();
        while (token.type() != TokenType.EOF) {
          if (params.size() > 0) {
            expect(TokenType.COMMA);
          }
          // Ugh, gotta do the stupid long int thing here too
          Type type = extractType(parseTypeSpecifiers());
          if (token.type() != TokenType.IDENTIFIER) {
            error("Expected identifier, saw " + token);
            break;
          }
          params.add(new Param(token.value(), type));
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

  private static final ImmutableSet<TokenType> DECL_STARTERS = ImmutableSet.of(
      TokenType.INT,
      TokenType.LONG,
      // TokenType.VOID, unclear if this is allowed as of Chapter 10
      TokenType.EXTERN,
      TokenType.STATIC);

  private BlockItem parseBlockItem() {
    // Is there a better way to do this? I fear...
    if (DECL_STARTERS.contains(token.type())) {
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
    if (DECL_STARTERS.contains(token.type())) {
      Declaration maybeVarDecl = parseDeclaration();
      if (maybeVarDecl instanceof VarDecl varDecl) {
        return new InitDecl(varDecl);
      } else {
        error("Cannot declare function as `for` init");
        return null;
      }
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

  private record Param(String name, Type type) {
  }

  private record TypeAndStorageClass(Type type, Optional<StorageClass> storageClass) {
    TypeAndStorageClass(Type type, StorageClass storageClass) {
      this(type, Optional.of(storageClass));
    }

    TypeAndStorageClass(Type type) {
      this(type, Optional.empty());
    }
  }

  private TypeAndStorageClass parseTypeAndStorageClass() {
    List<StorageClass> storageClasses = new ArrayList<>();
    List<Type> types = new ArrayList<>();
    while (token.type() == TokenType.EOF
        // I kind of hate this
        || token.type() == TokenType.INT || token.type() == TokenType.LONG
        || token.type() == TokenType.EXTERN || token.type() == TokenType.STATIC) {
      switch (token.type()) {
        case EOF:
          error("Unexpected EOF");
          return null;

        case INT:
        case LONG:
          types.addAll(parseTypeSpecifiers());
          break;

        case STATIC:
        case EXTERN:
          storageClasses.add(StorageClass.of(token.type()));
          advance();
          break;

        default:
          // needs to break out of the while loop?
          break;
      }
    }
    if (storageClasses.size() > 1) {
      error("Too many storage classes: %s", storageClasses.toString());
      return null;
    }
    Type type = extractType(types);
    if (storageClasses.size() == 0) {
      return new TypeAndStorageClass(type);
    }
    return new TypeAndStorageClass(type, storageClasses.get(0));
  }

  private static final Set<Type> INT_LONG = ImmutableSet.of(Type.INT, Type.LONG);

  private Type extractType(List<Type> types) {
    if (types.size() == 0) {
      error("Must specify a type");
      return null;
    }
    // ing, or long (or short)
    if (types.size() == 1) {
      return types.get(0);
    }
    if (types.size() > 2) {
      error("Too many types specified (maximum 2). Saw: %s", types.toString());
      return null;
    }
    Set<Type> uniqueTypes = new HashSet<>(types);
    // Can only be int, long or long, int
    if (uniqueTypes.size() == 1) {
      error("Can only specify long int or int long; saw %s", types.toString());
      return null;
    }
    if (uniqueTypes.equals(INT_LONG)) {
      // it's int long or long int
      return Type.LONG;
    }
    error("Should never get here; types list was %s", types.toString());
    return null;
  }

  // int var [ = exp] ;
  // int var(int a);
  // int var(int a) { block; }
  private Declaration parseDeclaration() {
    // Eats the "int" too.
    TypeAndStorageClass tasc = parseTypeAndStorageClass();
    String varName = token.value();
    expect(TokenType.IDENTIFIER);
    if (token.type() == TokenType.SEMICOLON) {
      advance();
      return new VarDecl(varName, tasc.type, tasc.storageClass);
    }
    if (token.type() == TokenType.EQ) {
      //  Declaration with initialization
      expect(TokenType.EQ);
      Exp init = parseExp();
      expect(TokenType.SEMICOLON);
      return new VarDecl(varName, tasc.type, init, tasc.storageClass);
    }

    expect(TokenType.OPAREN);
    List<Param> params = parseParamList();
    expect(TokenType.CPAREN);
    Optional<Block> block = Optional.empty();
    if (token.type() != TokenType.SEMICOLON) {
      block = Optional.of(parseBlock());
    } else {
      expect(TokenType.SEMICOLON);
    }

    FunType funType = new FunType(tasc.type, params.stream().map(Param::type).toList());
    return new FunDecl(varName, funType, params.stream().map(Param::name).toList(), block,
        tasc.storageClass);
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
        Exp result = switch (token.type()) {
          case INT, LONG -> {
            // cast
            Type type = extractType(parseTypeSpecifiers());
            expect(TokenType.CPAREN);
            Exp innerExp = parseFactor();
            yield new Cast(type, innerExp);
          }

          default -> {
            // Just parentheses
            Exp innerExp = parseExp();
            expect(TokenType.CPAREN);
            yield innerExp;
          }
        };
        yield result;
      }

      case INT_LITERAL -> {
        String valueAsString = token.value();
        expect(TokenType.INT_LITERAL);
        long valueAsLong = Long.parseLong(valueAsString);
        if (valueAsLong <= 2147483647) {
          int value = Integer.parseInt(valueAsString);
          yield Constant.of(value);
        } else {
          yield Constant.of(valueAsLong);
        }
      }

      case LONG_LITERAL -> {
        String valueAsString = token.value();
        expect(TokenType.LONG_LITERAL);
        long value = Long.parseLong(valueAsString);
        yield Constant.of(value);
      }

      case UNSIGNED_INT_LITERAL -> {
        String valueAsString = token.value();
        expect(TokenType.UNSIGNED_INT_LITERAL);
        int valueAsInt = Integer.parseInt(valueAsString);
        // Not sure if this is right
        yield Constant.ofUnsignedInt(valueAsInt);
      }

      case UNSIGNED_LONG_LITERAL -> {
        String valueAsString = token.value();
        expect(TokenType.UNSIGNED_LONG_LITERAL);
        long value = Long.parseLong(valueAsString);
        yield Constant.ofUnsignedLong(value);
      }

      case MINUS, TWIDDLE, BANG -> {
        // Unary
        advance();
        Exp innerExp = parseFactor();
        yield new UnaryExp(tt, innerExp);
      }

      default -> {
        error("Unexpected token %s; expected type, unary operator or identifier", tt.text);
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

  private void error(String message, Object... params) {
    throw new ParserException(String.format(message, params));
  }

  private void expect(TokenType tt) {
    if (token.type() == tt) {
      advance();
      return;
    }
    error("Expected `%s`, saw `%s`", tt.toString(), token.type().toString());
  }
}
