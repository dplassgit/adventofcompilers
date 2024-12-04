package com.plasstech.lang.c.lex;

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
    boolean whitespace = true;
    while (whitespace) {
      while (cc == ' ' || cc == '\n' || cc == '\t' || cc == '\r') {
        advance();
      }
      if (cc == '/') {
        char next = peek();
        if (next == '/') {
          // two slashes 
          advance(); // go past the two slashes
          advance(); // get the next char
          while (cc != 0 && cc != '\n') {
            advance();
          }
          if (cc == 0) {
            return eofToken();
          }
          advance(); // eat the \n
        } else if (next == '*') {
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
              System.err.println("Unclosed comment");
              System.exit(-1);
            }
            advance(); // eat the star
            if (cc == '/') {
              advance(); // eat the slash
              foundClosing = true;
            }
          }
        }
      } else {
        whitespace = false;
      }
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

  private static Token eofToken() {
    return new Token(TokenType.EOF, "");
  }

  private Token makeSymbol() {
    return null;
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
    } catch (Exception e) {
    }
    // Not a keyword, must be a variable.
    return new Token(TokenType.VARIABLE, value);
  }

  private Token makeNumber() {
    return null;
  }
}
