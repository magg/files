package com.magg.crypto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Base class for parameters for encryption and decryption data.
 *
 */
@AllArgsConstructor
public class EncryptionParams {

    @Getter
    private byte[] data;
}
