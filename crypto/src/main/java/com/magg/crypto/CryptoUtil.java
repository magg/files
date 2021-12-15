package com.magg.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Slf4j
public class CryptoUtil
{

    String password = "";
    final int iterations = 12000;
    final int keyLength = 256;
    final String KEY_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";
    static final int AES_NIVBITS = 128;
    static final String  UTF8="UTF-8";
    final byte[] pwdSalt = new byte[] {42,93,-116,80,-32,59,125,-37,-62,-67, 76,19,19,114,91,-115,-70,-89,36,-62};

    CryptoUtil(String password){
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        this.password = password;
    }

    public KeyParameter getAesKey(String passphrase){
        byte[] rawKey;

        try{
            if(passphrase == null || passphrase.isEmpty())
                throw new InvalidParameterException("passphrase is null or empty");

            PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), pwdSalt, iterations, keyLength);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            rawKey = keyFactory.generateSecret(keySpec).getEncoded();
            return new KeyParameter(rawKey);
        } catch (Exception e){
            log.error("Key factory init failed with the following error: {}", e.getMessage());
        }

        return null;
    }


    public void encodeStream(InputStream inputStream, OutputStream streamOut) throws GeneralSecurityException, IOException
    {
        byte[] ivData = new byte[AES_NIVBITS/8];
        new SecureRandom().nextBytes(ivData);

        //Select encrypt algo and padding: AES with CBC and PCKS7
        //Encrypt input stream using key+iv
        KeyParameter keyParam = getAesKey(this.password);
        CipherParameters params = new ParametersWithIV(keyParam, ivData);

        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher blockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
        blockCipher.reset();
        blockCipher.init(true, params);

        streamOut.write(ivData);
        CipherOutputStream cipherOut = new CipherOutputStream(streamOut, blockCipher);
        IOUtils.copy(inputStream, cipherOut);
        cipherOut.close();
    }

    public void decryptStream(InputStream encStream, OutputStream unEcnOutStream) throws IOException
    {
        //Extract the IV, which si stored in the next N bytes at the start of fileStream.
        int nIvBytes = (int) AES_NIVBITS/ 8;
        byte[] ivBytes = new byte[nIvBytes];
        encStream.read(ivBytes, 0, nIvBytes);

        KeyParameter keyParam = getAesKey(this.password);
        CipherParameters params = new ParametersWithIV(keyParam, ivBytes);
        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher blockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
        blockCipher.reset();
        blockCipher.init(false, params);

        CipherInputStream cipherIn = new CipherInputStream(encStream, blockCipher);
        IOUtils.copy(cipherIn, unEcnOutStream);
        cipherIn.close();
    }

    public void decryptFileToFile(File fileIn, String filePathOut) throws IOException
    {
        InputStream inStr = new FileInputStream(fileIn);
        FileOutputStream fileOutStream = new FileOutputStream(filePathOut);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        this.decryptStream(inStr, bos);
        bos.writeTo(fileOutStream);
        bos.close();
        fileOutStream.flush();
        fileOutStream.close();
    }

}
