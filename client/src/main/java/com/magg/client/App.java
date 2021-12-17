package com.magg.client;

import com.magg.client.config.Configuration;
import com.magg.client.service.GrpcUploadClient;
import com.magg.client.utils.KeyUtils;
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



        try
        {
            commandLine = parser.parse(options, args);

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
                processFile(commandLine.getOptionValue("f"));

            }


            if (commandLine.hasOption("k"))
            {
                log.info("Option k is present.  The value is: ");
                log.info(commandLine.getOptionValue("k"));
                KeyUtils.readKey(commandLine.getOptionValue("k"));
            }
        }
        catch (ParseException e)
        {
            System.out.println("SEVERE: Failed to parse command line properties " + e);
            help();
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


    private static void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();
        formater.printHelp("val [-f] <arg> [-k <arg>] | -g", options);
        System.exit(0);
    }
}
