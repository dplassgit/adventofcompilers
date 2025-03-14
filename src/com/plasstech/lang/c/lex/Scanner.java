package com.plasstech.lang.c.lex;

import java.util.Optional;

import com.plasstech.lang.c.typecheck.Type;

public class Scanner {
  private final String text;
  private int loc; // absolute location in text
  private char cc;
  private int line = 1, column = 0;

  public Scanner(String text) {
    this.text = text;
    advance();
  }

  private char advance() {
    if (loc < text.length()) {
      cc = text.charAt(loc);
    } else {
      // Indicates no more characters
      cc = 0;
    }
    loc++;
    column++;
    return cc;
  }

  private char peek() {
    if (loc < text.length()) {
      return text.charAt(loc);
    }
    // Indicates no more characters
    return 0;
  }

  public Token nextToken() {
    // skip unwanted whitespace
    Optional<Token> maybeEof = skipWhitespace();
    if (maybeEof.isPresent()) {
      return maybeEof.get();
    }

    if (Character.isDigit(cc) || cc == '.') {
      return makeNumber();
    }
    if (Character.isLetter(cc) || cc == '_') {
      return makeText();
    }
    if (cc != 0) {
      return makeSymbol();
    }

    return eofToken();
  }

  private Optional<Token> skipWhitespace() {
    while (true) {
      while (cc == ' ' || cc == '\n' || cc == '\t' || cc == '\r') {
        if (cc == '\n' || cc == '\r') {
          line++;
          column = 0;
        }
        advance();
      }
      if (cc != '/') {
        return Optional.empty();
      }
      char next = peek();
      if (next == '/') {
        // two slashes 
        advance(); // go past the two slashes
        advance(); // get the next char
        while (cc != 0 && cc != '\n') {
          advance();
        }
        if (cc == 0) {
          return Optional.of(eofToken());
        }
        line++;
        column = 0;
        advance(); // eat the \n
        continue;
      }
      if (next == '*') {
        // start of comment
        advance(); // go past the star
        advance(); // get the next char
        boolean foundClosing = false;
        while (!foundClosing) {
          while (cc != 0 && cc != '*') {
            advance();
          }
          // either EOF or we got a *
          if (cc == 0) {
            return Optional.of(error("Unclosed comment"));
          }
          advance(); // eat the star
          if (cc == '/') {
            advance(); // eat the slash
            foundClosing = true;
          }
        }
        continue;
      }
      return Optional.empty();
    }
  }

  private Token eofToken() {
    return new Token(TokenType.EOF, "", new Position(line, column));
  }

  private Token makeSymbol() {
    String symbol = String.valueOf(cc);
    advance();
    if (cc != 0) {
      // cc is already the next symbol
      String twoCharSymbol = symbol + cc;
      Optional<TokenType> tt = findSymbolByString(twoCharSymbol);
      if (tt.isPresent()) {
        advance();
        return new Token(tt.get(), twoCharSymbol, new Position(line, column));
      }
    }
    Optional<TokenType> tt = findSymbolByString(symbol);
    if (tt.isPresent()) {
      return new Token(tt.get(), symbol, new Position(line, column));
    }
    return error("Illegal character " + symbol);
  }

  private static Optional<TokenType> findSymbolByString(String symbol) {
    for (TokenType tt : TokenType.values()) {
      if (tt.isSymbol() && tt.text.equals(symbol)) {
        return Optional.of(tt);
      }
    }
    return Optional.empty();
  }

  private Token makeText() {
    StringBuilder sb = new StringBuilder();
    if (Character.isLetter(cc) || cc == '_') {
      sb.append(cc);
      advance();
    }
    while (Character.isLetterOrDigit(cc) || cc == '_') {
      sb.append(cc);
      advance();
    }

    String value = sb.toString();
    try {
      // Figure out if it's a keyword
      if (value.toLowerCase().equals(value)) {
        // all lower case
        TokenType maybeTt = TokenType.valueOf(value.toUpperCase());
        if (maybeTt.isKeyword) {
          return new Token(maybeTt, value, new Position(line, column));
        }
      }
    } catch (Exception e) {
    }
    // Not a keyword, must be a variable.
    return new Token(TokenType.IDENTIFIER, value, new Position(line, column));
  }

  private Token makeNumber() {
    String value = "";
    boolean parsingDouble = cc == '.';
    if (cc == '.') {
      // Leading dot in a floating point constant
      value += cc;
      advance();
    }
    value += makeInt();
    if (value.equals(".")) {
      return error("Illegal floating point constant '.'");
    }
    boolean longConstant = false;
    boolean unsignedConstant = false;
    char prevC = cc;
    if (value.length() > 0 && !parsingDouble) {
      if (cc == 'L' || cc == 'l') {
        // long constant
        advance();
        longConstant = true;
        if (cc == 'U' || cc == 'u') {
          // unsigned constant
          advance();
          unsignedConstant = true;
        }
      } else if (cc == 'U' || cc == 'u') {
        // unsigned constant
        advance();
        unsignedConstant = true;
        if (cc == 'L' || cc == 'l') {
          // long constant
          advance();
          longConstant = true;
        }
      }
    }
    // Page 302-304
    if ((cc == '.' || cc == 'E' || cc == 'e')
        && (prevC == 'L' || prevC == 'l' || prevC == 'U' || prevC == 'u')) {
      // I kind of hate this.
      return error("Illegal floating point constant " + value + prevC + '.');
    }
    if (cc == '.') {
      value += cc;
      advance();
      parsingDouble = true;
      // ###. (trailing dot) so far
      value += makeInt();
    }
    parsingDouble |= (cc == 'E' || cc == 'e');
    if (parsingDouble) {
      // Could be ###.###[Ee][+-]?###
      value += makeOptionalExponent();
    } else if (Character.isLetter(cc)) {
      return error("Illegal floating point constant " + value + cc);
    }
    if (parsingDouble) {
      return new Token(TokenType.NUMERIC_LITERAL, value, Type.DOUBLE, new Position(line, column));
    }

    if (unsignedConstant && longConstant) {
      return new Token(TokenType.NUMERIC_LITERAL, value, Type.UNSIGNED_LONG,
          new Position(line, column));
    }
    if (longConstant) {
      return new Token(TokenType.NUMERIC_LITERAL, value, Type.LONG, new Position(line, column));
    }
    if (unsignedConstant) {
      return new Token(TokenType.NUMERIC_LITERAL, value, Type.UNSIGNED_INT,
          new Position(line, column));
    }
    return new Token(TokenType.NUMERIC_LITERAL, value, Type.INT, new Position(line, column));
  }

  // Returns the exponent (if any). Assumes the next character is E or e, a non-number-starter:
  // [Ee][+-]?int
  // Throws if the following character (after the int) is "illegal", but it's possible I got it
  // wrong...
  private String makeOptionalExponent() {
    String value = "";
    if (cc == 'E' || cc == 'e') {
      value += cc;
      advance();
      if (cc == '+' || cc == '-') {
        value += cc;
        advance();
      }
      String exp = makeInt();
      if (exp.length() == 0) {
        error("Invalid floating point constant: " + value);
      }
      value += exp;
    }
    // gah. the trailing characters of a float constant can only be certain things and I can't
    // remember what they are... let's guess
    if (cc == '.' || Character.isAlphabetic(cc) || cc == '_') {
      error("Invalid character after floating point constant: " + cc);
    }
    return value;
  }

  // Returns the next sequence of just numbers or underscores. Throws exception if leading
  // underscore.
  private String makeInt() {
    String sb = "";
    if (cc == '_') {
      error("Cannot start number with underscore");
    }
    while (Character.isDigit(cc) || cc == '_') {
      sb += cc;
      advance();
    }
    return sb;
  }

  private Token error(String message) {
    throw new ScannerException(new Position(line, column), message);
  }
}
