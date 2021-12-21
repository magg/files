package com.magg.crypto;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.apache.commons.io.FilenameUtils;

import static com.magg.crypto.CryptoUtil.AES_NIVBITS;

/**
 * Base class for symmetric block cipher encryption/decryption with initialization vector.
 *
 */
public abstract class SymmetricBlockIVEncryption extends BaseEncryption {

    @Override
    protected byte[] doCryptoAction(EncryptionParams params, int mode) throws GeneralSecurityException {
        SymmetricEncryptionIVParams symmetricEncryptionParams = (SymmetricEncryptionIVParams) params;
        SecretKey secretKey = symmetricEncryptionParams.getSecretKey();

        IvParameterSpec ivSpec = new IvParameterSpec(symmetricEncryptionParams.getIv());
        Cipher cipher = getCipher();
        cipher.init(mode, secretKey, ivSpec);
        return cipher.doFinal(symmetricEncryptionParams.getData());

    }

    @Override
    protected void validateParamsType(EncryptionParams params) {
        if (!(params instanceof SymmetricEncryptionIVParams)) {
            throw new IllegalArgumentException("Invalid params type.");
        }
    }

    @Override
    protected String encryptFile(EncryptionParams params, int mode, String fileName) throws GeneralSecurityException
    {

        SymmetricEncryptionIVParams symmetricEncryptionParams = (SymmetricEncryptionIVParams) params;
        SecretKey secretKey = symmetricEncryptionParams.getSecretKey();

        IvParameterSpec ivSpec = new IvParameterSpec(symmetricEncryptionParams.getIv());
        Cipher cipher = getCipher();
        cipher.init(mode, secretKey, ivSpec);
        String name = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        String path = FilenameUtils.getPath(fileName);
        String outPutFileName = File.separatorChar+path+ name+"_encrypted."+extension;

        try (
            FileOutputStream fileOut = new FileOutputStream(outPutFileName);
            CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)
        ) {
            fileOut.write(symmetricEncryptionParams.getIv());
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


        String name = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        String path = FilenameUtils.getPath(fileName);

        byte[] ivData = new byte[AES_NIVBITS/8] ;
        try (FileInputStream fileIn = new FileInputStream(fileName)) {
            fileIn.read(ivData);
            IvParameterSpec ivSpec = new IvParameterSpec(ivData);

            Cipher cipher = getCipher();
            cipher.init(mode, secretKey, ivSpec);

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
                    //System.out.println(new String(data, "UTF-8").trim());
                }

            }

        }
        catch (NoSuchPaddingException | IOException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

    }
}

