package com.magg.crypto;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 * Base class for encryption and decryption messages.
 *
 */
public abstract class BaseEncryption implements Encryption {

    /**
     * Get cipher name.
     *
     * @return Cipher name
     */
    protected abstract String getCipherName();

    /**
     * Check parameters type.
     *
     * @param params Parameters for encryption or decryption
     */
    protected abstract void validateParamsType(EncryptionParams params);

    /**
     * Process encryption or decryption.
     *
     * @param params Parameters for encryption or decryption
     * @param mode   Decryption or encryption mode
     * @return Encrypted or decrypted bytes
     * @throws GeneralSecurityException if decryption or encryption failed
     */
    protected abstract byte[] doCryptoAction(EncryptionParams params, int mode) throws GeneralSecurityException;


    @Override
    public byte[] encrypt(EncryptionParams params) throws GeneralSecurityException {
        validateEncryptionParams(params);
        return doCryptoAction(params, Cipher.ENCRYPT_MODE);
    }

    @Override
    public byte[] decrypt(EncryptionParams params) throws GeneralSecurityException {
        validateDecryptionParams(params);
        return doCryptoAction(params, Cipher.DECRYPT_MODE);
    }

    /**
     * Get security provider.
     *
     * @return Security provider or null if need using of default security provider
     */
    protected Provider getProvider() {
        return null;
    }

    /**
     * Get cipher for encryption/decryption.
     *
     * @return Cipher object for encryption/decryption
     * @throws NoSuchPaddingException   If padding not supported
     * @throws NoSuchAlgorithmException If algorithm not found
     */
    protected Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        Provider provider = getProvider();
        return provider != null
            ? Cipher.getInstance(getCipherName(), provider)
            : Cipher.getInstance(getCipherName());
    }

    /**
     * Check parameters for encryption.
     *
     * @param params Parameters for encryption
     */
    protected void validateEncryptionParams(EncryptionParams params) {
        checkNullData(params, "Data for encryption cannot be null.");
        validateParamsType(params);
    }

    /**
     * Check parameters for decryption.
     *
     * @param params Parameters for decryption
     */
    protected void validateDecryptionParams(EncryptionParams params) {
        checkNullData(params, "Data for decryption cannot be null.");
        validateParamsType(params);
    }

    private void checkNullData(EncryptionParams params, String message) {
        if (params.getData() == null) {
            throw new IllegalArgumentException(message);
        }
    }


    /**
     *  Encrypts a file
     * @param params Parameters for encryption or decryption
     * @param mode   Decryption or encryption mode
     * @param fileName the file name
     * @throws GeneralSecurityException if decryption or encryption failed
     */
    protected abstract String encryptFile(EncryptionParams params, int mode, String fileName) throws GeneralSecurityException;


    /**
     *  Encrypts a file
     * @param params Parameters for encryption or decryption
     * @param fileName the file name
     * @throws GeneralSecurityException if decryption or encryption failed
     */
    public String encodeFile(EncryptionParams params, String fileName) throws GeneralSecurityException {
        validateEncryptionParams(params);
        return encryptFile(params, Cipher.ENCRYPT_MODE, fileName);
    }

    protected abstract void decrypt(EncryptionParams params, String fileName, int mode);

    /**
     *  Encrypts a file
     * @param params Parameters for encryption or decryption
     * @param fileName the file name
     * @throws GeneralSecurityException if decryption or encryption failed
     */
    public void decodeFile(EncryptionParams params, String fileName) throws GeneralSecurityException {
        validateEncryptionParams(params);
        decrypt(params, fileName, Cipher.DECRYPT_MODE);
    }

}
