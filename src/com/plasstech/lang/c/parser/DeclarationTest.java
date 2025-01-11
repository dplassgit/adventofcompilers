package com.plasstech.lang.c.parser;

import static com.google.common.truth.Truth.assertThat;

import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class DeclarationTest {
  @Test
  public void hasStorageClassNone() {
    Declaration d = new FunDecl("name", ImmutableList.of(), Optional.empty(), Optional.empty());
    assertThat(d.hasStorageClass(StorageClass.STATIC)).isFalse();
    assertThat(d.hasStorageClass(StorageClass.EXTERN)).isFalse();
  }

  @Test
  public void hasStorageClassExtern() {
    Declaration d =
        new FunDecl("name", ImmutableList.of(), Optional.empty(), Optional.of(StorageClass.EXTERN));
    assertThat(d.hasStorageClass(StorageClass.STATIC)).isFalse();
    assertThat(d.hasStorageClass(StorageClass.EXTERN)).isTrue();
  }
}
