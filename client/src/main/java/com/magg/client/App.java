package com.magg.client;

import com.magg.client.config.Configuration;
import com.magg.client.service.GrcpDownloadClient;
import com.magg.client.service.GrpcUploadClient;
import com.magg.client.utils.KeyUtils;
import com.magg.crypto.AESBouncyEncryption;
import com.magg.crypto.SymmetricEncryptionIVParams;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.yaml.snakeyaml.Yaml;


/**
 * Hello world!
 *
 */
@Slf4j
public class App 
{
    private static Configuration config;
    private static Options options = new Options();
    private static SecretKey secretKey;

    public static void main( String[] args )
    {
        log.info( "Hello World!" );

        Yaml yaml = new Yaml();

        try {
            config = yaml.loadAs( App.class.getResourceAsStream("/application.yml"), Configuration.class );
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.error("SEVERE error: application.yml missing");
            System.exit(0);
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine =  null;

        options.addOption("f", "file", true, "A file");
        options.addOption("k", "key", true, "A key");
        options.addOption("g", "genkey", true, "Generate key");
        options.addOption("d", "decrypt", true, "Decrypt file");
        options.addOption("i", "download", true, "Download file by ID");


        try
        {
            commandLine = parser.parse(options, args);

            if (commandLine.hasOption("k"))
            {
                log.info("Option k is present.  The value is: ");
                log.info(commandLine.getOptionValue("k"));
                secretKey = KeyUtils.readKey(commandLine.getOptionValue("k"));
            }

            if (commandLine.hasOption("i"))
            {
                log.info("Option i is present.  The value is: ");
                log.info(commandLine.getOptionValue("i"));
                String file = downloadFile(commandLine.getOptionValue("i"));
                if (file != null) {
                    decryptFile(file);
                    System.exit(0);
                } else {
                    log.error("Error downloading or renaming a file");
                    System.exit(-1);
                }
            }

            if (commandLine.hasOption("g"))
            {
                log.info("Option g is present.  The value is: ");
                log.info(commandLine.getOptionValue("g"));
                KeyUtils.generateKey(commandLine.getOptionValue("g"));
                System.exit(0);

            }

            if (commandLine.hasOption("f"))
            {
                log.info("Option f is present.  The value is: ");
                log.info(commandLine.getOptionValue("f"));

                String encryptedFile = readAndEncryptFile(commandLine.getOptionValue("f"));

                if (encryptedFile != null) {
                    processFile(encryptedFile);
                } else {
                    System.out.println("File encrypting Error");
                    System.exit(-1);
                }
            }

            if (commandLine.hasOption("d"))
            {
                log.info("Option d is present.  The value is: ");
                log.info(commandLine.getOptionValue("d"));

                decryptFile(commandLine.getOptionValue("d"));

            }

        }
        catch (ParseException e)
        {
            System.out.println("SEVERE: Failed to parse command line properties " + e);
            help();
        }
    }


    private static String readAndEncryptFile(String file) {

        if (secretKey == null) {
            System.out.println("Please pass a key as an argument using the -k option");
            System.exit(-1);
        }
        String result = null;

        final int AES_NIVBITS = 128;
        try
        {
            byte[] bytes = Files.readAllBytes(Paths.get(file));
            byte[] ivData = new byte[AES_NIVBITS/8] ;
            new SecureRandom().nextBytes(ivData);

            SymmetricEncryptionIVParams paramsForBodyEncryption = new SymmetricEncryptionIVParams(bytes, ivData, secretKey);

            AESBouncyEncryption aesBouncyEncryption = new AESBouncyEncryption();
            result = aesBouncyEncryption.encodeFile(paramsForBodyEncryption, file);

        }
        catch (IOException | GeneralSecurityException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    private static void decryptFile(String file) {

        if (secretKey == null) {
            System.out.println("Please pass a key as an argument using the -k option");
            System.exit(-1);
        }

        final int AES_NIVBITS = 128;
        try
        {
            byte[] bytes = Files.readAllBytes(Paths.get(file));
            byte[] ivData = new byte[AES_NIVBITS/8] ;

            SymmetricEncryptionIVParams paramsForBodyEncryption = new SymmetricEncryptionIVParams(bytes, ivData, secretKey);

            AESBouncyEncryption aesBouncyEncryption = new AESBouncyEncryption();
            aesBouncyEncryption.decodeFile(paramsForBodyEncryption, file);

        }
        catch (IOException | GeneralSecurityException e)
        {
            e.printStackTrace();
        }

    }


    private static void processFile(String file)
    {
        GrpcUploadClient client = new GrpcUploadClient(config.getServer(), config.getPort());
        client.upload(file);
        try
        {
            client.shutdown();
        }
        catch (InterruptedException e)
        {
            log.error(e.getMessage());
        }
        System.exit(0);

    }

    private static String downloadFile(String id)
    {
        GrcpDownloadClient client = new GrcpDownloadClient(config.getServer(), config.getPort(), config.getDownloadPath());

        try
        {
            String file = client.downloadFile(id);
            client.shutdown();

            return file;
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
        }

        return null;
    }


    private static void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("val [-f] <arg> [-k <arg>] | -g", options);
        System.exit(0);
    }
}
