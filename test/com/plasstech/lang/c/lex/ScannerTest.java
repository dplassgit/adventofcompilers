package com.plasstech.lang.c.lex;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

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
    Scanner s =
        new Scanner("int return void if else do while for break continue extern static long");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.INT);
    assertThat(t.isKeyword()).isTrue();
    assertThat(s.nextToken().type()).isEqualTo(TokenType.RETURN);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.VOID);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.IF);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.ELSE);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DO);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.WHILE);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.FOR);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.BREAK);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.CONTINUE);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EXTERN);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.STATIC);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.LONG);
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
  public void nextTokenLongConstant() {
    Scanner s = new Scanner("0L 1L 123123123123l");
    Token t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.LONG_LITERAL);
    assertThat(t.value()).isEqualTo("0");
    t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.LONG_LITERAL);
    assertThat(t.value()).isEqualTo("1");
    t = s.nextToken();
    assertThat(t.type()).isEqualTo(TokenType.LONG_LITERAL);
    assertThat(t.value()).isEqualTo("123123123123");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenBadIntConstant() {
    assertThrows(ScannerException.class, () -> new Scanner("0a").nextToken());
    assertThrows(ScannerException.class, () -> new Scanner("23B").nextToken());
    assertThrows(ScannerException.class, () -> new Scanner("234.").nextToken());
    assertThrows(ScannerException.class, () -> new Scanner("234L.").nextToken());
  }

  @Test
  public void nextTokenSymbol() {
    Scanner s =
        new Scanner("{} /* {} */ ( ) //\n;\n - ~ + * / % && & || | ! != < <= == > >= = ? : ,");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.OBRACE);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.CBRACE);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.OPAREN);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.CPAREN);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.SEMICOLON);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.MINUS);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.TWIDDLE);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.PLUS);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.STAR);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.SLASH);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.PERCENT);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DOUBLE_AMP);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.AMP);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DOUBLE_BAR);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.BAR);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.BANG);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.NEQ);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.LT);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.LEQ);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EQEQ);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.GT);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.GEQ);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EQ);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.QUESTION);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.COLON);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.COMMA);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenSymbolNoSpaces() {
    Scanner s = new Scanner("---&&&|||");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DECREMENT);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.MINUS);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DOUBLE_AMP);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.AMP);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DOUBLE_BAR);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.BAR);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenDoubleSymbols() {
    Scanner s = new Scanner("----");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DECREMENT);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DECREMENT);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenDecrement() {
    Scanner s = new Scanner("--");
    assertThat(s.nextToken().type()).isEqualTo(TokenType.DECREMENT);
    assertThat(s.nextToken().type()).isEqualTo(TokenType.EOF);
  }

  @Test
  public void nextTokenBadSymbol() {
    assertThrows(ScannerException.class, () -> new Scanner("@").nextToken());
    assertThrows(ScannerException.class, () -> new Scanner(".").nextToken());
    assertThrows(ScannerException.class, () -> new Scanner("\\").nextToken());
    assertThrows(ScannerException.class, () -> new Scanner("/*").nextToken());
  }
}
