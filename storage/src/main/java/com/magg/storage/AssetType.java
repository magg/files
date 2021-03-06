package com.magg.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Asset type.
 */
@AllArgsConstructor
@Getter
public enum AssetType {
    TEST("test/");

    private final String prefix;
}