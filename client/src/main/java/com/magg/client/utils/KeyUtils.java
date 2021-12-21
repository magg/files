package com.magg.client.utils;

import com.magg.crypto.key.AESKeyGenerator;
import com.magg.crypto.key.CryptoAlgorithm;
import com.magg.crypto.key.KeyParams;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeyUtils
{
    public static void generateKey(String name) {

        AESKeyGenerator aesKeyGenerator = new AESKeyGenerator();
        try
        {

            String home = System.getProperty("user.home");
            String keyDir =  home + "/.file-keys";

            Files.createDirectories(Paths.get(keyDir));

            SecretKey ky = aesKeyGenerator.generateKey(new KeyParams(CryptoAlgorithm.AES.getDefaultSize()));

            String fl = keyDir + "/" + name+".key";
            FileOutputStream fos = new FileOutputStream(fl);
            byte[] kb = ky.getEncoded();
            fos.write(kb);
            fos.close();
            System.out.println("New key created in: "+ fl);
        }
        catch (GeneralSecurityException | IOException e)
        {
            log.error(e.getMessage());
            System.exit(-1);
        }
    }


    public static SecretKey readKey(String input)
    {

        AESKeyGenerator aesKeyGenerator = new AESKeyGenerator();
        String fl = input;
        FileInputStream fis;
        byte[] kb = null;
        try
        {
            fis = new FileInputStream(fl);
            int kl = fis.available();
            kb = new byte[kl];
            fis.read(kb);
            fis.close();
            System.out.println("pase");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return aesKeyGenerator.readKey(kb);
    }

}
