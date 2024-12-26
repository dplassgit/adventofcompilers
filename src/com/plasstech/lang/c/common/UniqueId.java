package com.plasstech.lang.c.common;

public class UniqueId {
  private static int id;

  public static String makeUnique(String prefix) {
    return String.format("%s.%d", prefix, id++);
  }
}
