package com.magg.crypto;

import javax.crypto.SecretKey;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Class for params for encryption with symmetric key and initialization vector for cipher.
 *
 */
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class SymmetricEncryptionIVParams extends EncryptionParams {

    private SecretKey secretKey;
    private byte[] iv;

    /**
     * Construct Parameters for encryption.
     *
     * @param data      Bytes for encryption
     * @param iv        Initial vector
     * @param secretKey Secret key
     */
    @Builder
    public SymmetricEncryptionIVParams(byte[] data, byte[] iv, SecretKey secretKey) {
        super(data);
        this.iv = iv;
        this.secretKey = secretKey;
    }
}
