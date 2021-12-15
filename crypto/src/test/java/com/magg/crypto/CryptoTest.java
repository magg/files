package com.magg.crypto;

import com.magg.crypto.key.AESKeyGenerator;
import com.magg.crypto.key.CryptoAlgorithm;
import com.magg.crypto.key.KeyParams;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import org.junit.Test;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static org.junit.Assert.assertArrayEquals;

public class CryptoTest extends AbstractCryptoTest
{

    @Test
    public void testEnrollEncryptionAndDecryptionWithRSAKeysAndSymmetricKey() throws UnsupportedEncodingException, GeneralSecurityException
    {
        String body = "{\"externalId\":\"externalId\",\"appPNS\":\"appPNS\"}";
        testEncryptionAndDecryption(body);
    }


    private void testEncryptionAndDecryption(String body) throws UnsupportedEncodingException,
                                                                              GeneralSecurityException {
        byte[] bodyBytes = body.getBytes("UTF-8");

        AESKeyGenerator aesKeyGenerator = new AESKeyGenerator();
        SecretKey symmetricKeyPSA = aesKeyGenerator.generateKey(new KeyParams(CryptoAlgorithm.AES.getDefaultSize()));
        byte[] iv = getIV();
        AESBouncyEncryption aesBouncyEncryption = new AESBouncyEncryption();
        SymmetricEncryptionIVParams paramsForBodyEncryption = new SymmetricEncryptionIVParams(bodyBytes, iv, symmetricKeyPSA);
        byte[] encryptedBodyBytes = aesBouncyEncryption.encrypt(paramsForBodyEncryption);
        String testBody = printBase64Binary(encryptedBodyBytes);


        SymmetricEncryptionIVParams paramsForBodyDecryption = new SymmetricEncryptionIVParams(encryptedBodyBytes, iv, symmetricKeyPSA);
        byte[] decryptedBodyBytes = aesBouncyEncryption.decrypt(paramsForBodyDecryption);
        assertArrayEquals("Plain text bytes and decrypted text bytes are not equal", bodyBytes, decryptedBodyBytes);
    }
}
