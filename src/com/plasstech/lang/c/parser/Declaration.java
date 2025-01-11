package com.plasstech.lang.c.parser;

import java.util.Optional;

public interface Declaration extends BlockItem {
  Optional<StorageClass> storageClass();

  default boolean isExtern() {
    return storageClass().filter(sc -> sc == StorageClass.EXTERN).isPresent();
  }
}
