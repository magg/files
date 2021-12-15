package com.magg.crypto;

import javax.crypto.SecretKey;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Class for params for encryption with symmetric key.
 *
 */
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class SymmetricEncryptionParams extends EncryptionParams {

    private SecretKey secretKey;

    /**
     * Construct parameters for encryption.
     *
     * @param data      Bytes for encryption
     * @param secretKey Secret key
     */
    @Builder
    public SymmetricEncryptionParams(byte[] data, SecretKey secretKey) {
        super(data);
        this.secretKey = secretKey;
    }
}
