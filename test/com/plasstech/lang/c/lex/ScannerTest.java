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
}
