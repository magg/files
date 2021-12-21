package com.magg.crypto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.FilenameUtils;

/**
 * Base class for encryption and decryption messages with symmetric keys and block cipher.
 *
 */
public abstract class SymmetricBlockEncryption extends BaseEncryption {

    /**
     * Get algorithm.
     *
     * @return Cryptographic algorithm name
     */
    protected abstract String getAlgorithm();

    @Override
    protected byte[] doCryptoAction(EncryptionParams params, int mode) throws GeneralSecurityException {
        SymmetricEncryptionParams symmetricEncryptionParams = (SymmetricEncryptionParams) params;
        SecretKey secretKey = symmetricEncryptionParams.getSecretKey();
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), getAlgorithm());
        Cipher cipher = getCipher();
        cipher.init(mode, keySpec);
        return cipher.doFinal(params.getData());

    }

    @Override
    protected void validateParamsType(EncryptionParams params) {
        if (!(params instanceof SymmetricEncryptionParams)) {
            throw new IllegalArgumentException("Invalid params type.");
        }
    }

    @Override
    protected String encryptFile(EncryptionParams params, int mode, String fileName) throws GeneralSecurityException
    {

        SymmetricEncryptionParams symmetricEncryptionParams = (SymmetricEncryptionParams) params;
        SecretKey secretKey = symmetricEncryptionParams.getSecretKey();
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), getAlgorithm());
        Cipher cipher = getCipher();
        cipher.init(mode, keySpec);

        String name = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        String path = FilenameUtils.getPath(fileName);
        String outPutFileName = File.separatorChar+path+ name+"_encrypted."+extension;

        try (
            FileOutputStream fileOut = new FileOutputStream(outPutFileName);
            CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)
        ) {
            cipherOut.write(params.getData());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return outPutFileName;
    }


    public void decrypt(EncryptionParams params, String fileName, int mode) {

        SymmetricEncryptionIVParams symmetricEncryptionParams = (SymmetricEncryptionIVParams) params;
        SecretKey secretKey = symmetricEncryptionParams.getSecretKey();
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), getAlgorithm());


        String name = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        String path = FilenameUtils.getPath(fileName);


        try (FileInputStream fileIn = new FileInputStream(fileName)) {
            //fileIn.read(ivSpec.getIV());

            Cipher cipher = getCipher();
            cipher.init(mode, keySpec);

            try (
                FileOutputStream fileOut = new FileOutputStream(File.separatorChar+path+name+"_decrypted."+extension);

                CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
                InputStreamReader inputReader = new InputStreamReader(cipherIn);
                BufferedReader reader = new BufferedReader(inputReader)
            ) {

                byte[] data = new byte[1024];
                int read = cipherIn.read(data);
                while (read != -1) {
                    fileOut.write(data, 0, read);
                    read = cipherIn.read(data);
                    System.out.println(new String(data, "UTF-8").trim());
                }

            }

        }
        catch (NoSuchPaddingException | IOException | InvalidKeyException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

    }
}

