package com.plasstech.lang.c.lex;

import java.util.Optional;

public class Scanner {
  private final String text;
  private int loc; // absolute location in text
  private char cc;

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

    if (Character.isDigit(cc)) {
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

  private static Token eofToken() {
    return new Token(TokenType.EOF, "");
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
        return new Token(tt.get(), twoCharSymbol);
      }
    }
    Optional<TokenType> tt = findSymbolByString(symbol);
    if (tt.isPresent()) {
      return new Token(tt.get(), symbol);
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
          return new Token(maybeTt, value);
        }
      }
    } catch (Exception e) {
    }
    // Not a keyword, must be a variable.
    return new Token(TokenType.IDENTIFIER, value);
  }

  private Token makeNumber() {
    StringBuilder sb = new StringBuilder();
    while (Character.isDigit(cc) || cc == '_') {
      sb.append(cc);
      advance();
    }
    if (Character.isLetter(cc) || cc == '.') {
      return error("Illegal character " + cc);
    }

    String value = sb.toString();
    return new Token(TokenType.INT_LITERAL, value);
  }

  private Token error(String message) {
    throw new ScannerException(message);
  }
}
