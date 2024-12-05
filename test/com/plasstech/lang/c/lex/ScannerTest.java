package com.plasstech.lang.c.lex;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class ScannerTest {

  @Test
  public void nextTokenEmpty() {
    Scanner s = new Scanner("");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenNewline() {
    Scanner s = new Scanner("\n");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenCommentToEol() {
    Scanner s = new Scanner(" // hi\n// bye\n\n");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenMultilineComment() {
    Scanner s = new Scanner("/* hi \n */\n");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenMultilineCommentWithStar() {
    Scanner s = new Scanner("/* hi \n * \n */");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenMultiMultilineComment() {
    Scanner s = new Scanner("/* hi \n ** \n */ /* another */\n");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenKeywords() {
    Scanner s = new Scanner("int return void");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.INT);
    assertThat(t.isKeyword()).isTrue();
    assertThat(s.nextToken().type()).isEqualTo(TokenType.RETURN);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.VOID);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenVariables() {
    Scanner s = new Scanner("INT a b3");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.IDENTIFIER);
    assertThat(t.value()).isEqualTo("INT");
    t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.IDENTIFIER);
    assertThat(t.value()).isEqualTo("a");
    t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.IDENTIFIER);
    assertThat(t.value()).isEqualTo("b3");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenIntConstant() {
    Scanner s = new Scanner("0 1 23");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.INT_LITERAL);
    assertThat(t.value()).isEqualTo("0");
    t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.INT_LITERAL);
    assertThat(t.value()).isEqualTo("1");
    t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.INT_LITERAL);
    assertThat(t.value()).isEqualTo("23");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenBadIntConstant() {
    Scanner s = new Scanner("0a");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.ERROR);
    s = new Scanner("23B");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.ERROR);
    s = new Scanner("234.");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.ERROR);
  }

  @Test
  public void nextTokenSymbol() {
    Scanner s = new Scanner("{} /* {} */ ( ) //\n;\n");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.OBRACE);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.CBRACE);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.OPAREN);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.CPAREN);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.SEMICOLON);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenBadSymbol() {
    assertThat(new Scanner("@").nextToken().type()).isEqualTo(TokenType.ERROR);
    assertThat(new Scanner(".").nextToken().type()).isEqualTo(TokenType.ERROR);
    assertThat(new Scanner("\\").nextToken().type()).isEqualTo(TokenType.ERROR);
  }
}
