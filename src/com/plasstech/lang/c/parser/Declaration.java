package com.plasstech.lang.c.parser;

import java.util.Optional;

public interface Declaration extends BlockItem {
  Optional<StorageClass> storageClass();

  default boolean hasStorageClass(StorageClass desired) {
    return storageClass().filter(sc -> sc == desired).isPresent();
  }
}
