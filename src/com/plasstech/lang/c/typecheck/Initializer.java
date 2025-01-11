package com.plasstech.lang.c.typecheck;

/**
 * Represents the initial integer of a file-scope variable declaration.
 */
public record Initializer(int value) implements InitialValue {
}
