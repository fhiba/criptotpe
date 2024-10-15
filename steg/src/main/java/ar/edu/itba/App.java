package ar.edu.itba;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 * Hello world!
 *
 
 */

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

public class App {
    Embed embed;
    Extract extract;

    public void setUp(String[] args) {
        Options opts = new Options();

        opts.addOption(Option.builder()
                .longOpt("embed")
                .desc("Embed operation")
                .required(false)
                .build());
        opts.addOption(Option.builder()
                .longOpt("extract")
                .desc("Extract operation")
                .required(false)
                .build());
        opts.addOption(Option.builder()
                .longOpt("in")
                .desc("File to embed")
                .required(false)
                .hasArg()
                .argName("FILE")
                .type(String.class)
                .build());
        opts.addOption(Option.builder()
                .longOpt("out")
                .desc("Output file Modified BMP if embed, output file with the message if extract")
                .required()
                .hasArg()
                .argName("FILE")
                .type(String.class)
                .build());
        opts.addOption(Option.builder()
                .option("p")
                .required()
                .hasArg()
                .argName("BMP")
                .desc("BMP file to extract from/embed into")
                .build());
        opts.addOption(Option.builder()
                .longOpt("steg")
                .desc("Steganography method: LSB1 | LSB4 | LSBI")
                .required()
                .hasArg()
                .argName("METHOD")
                .type(String.class)
                .build());
        opts.addOption(Option.builder()
                .longOpt("pass")
                .desc("Password to encrypt the message")
                .required(false)
                .hasArg()
                .argName("PASSWORD")
                .type(String.class)
                .build());
        opts.addOption(Option.builder()
                .option("a")
                .desc("Encryption algorithm: aes128 | aes192 | aes256 | 3des")
                .required(false)
                .hasArg()
                .argName("ALGORITHM")
                .type(String.class)
                .build());
        opts.addOption(Option.builder()
                .option("m")
                .desc("Chain mode: ecb | cfb | ofb | cbc")
                .required(false)
                .hasArg()
                .argName("MODE")
                .type(String.class)
                .build());

        Algorithm alg = null;
        Encryption enc = null;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(opts, args);

            if ((cmd.hasOption("embed") && cmd.hasOption("extract"))
                    || (!cmd.hasOption("embed") && !cmd.hasOption("extract"))) {
                System.out.println("You must choose between embed or extract");
                formatter.printHelp("steg", opts);
                System.exit(1);
            }
            if ((cmd.hasOption("m") || cmd.hasOption("a")) && !cmd.hasOption("pass")) {
                System.out.println("You must provide a password to encrypt the message");
                formatter.printHelp("steg", opts);
                System.exit(1);
            }

            if (cmd.hasOption("embed") && !cmd.hasOption("in")) {
                System.out.println("You must provide an input file and an output file");
                formatter.printHelp("steg", opts);
                System.exit(1);
            }

            alg = AlgEnum.getAlg(cmd.getOptionValue("steg")).get();

            if (cmd.hasOption("pass")) {
                enc = new Encryption(cmd.getOptionValue("pass"), EncModeEnum.getMode(cmd.getOptionValue("m")),
                        EncEnum.getEncryption(cmd.getOptionValue("a")));
            }

            // TODO: Implement Operation abs class and pass encryption object
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void main(String[] args) {
        App app = new App();
        app.setUp(args);
        String action = args[0];
        if (action.equals("embed"))
            embed.embed(args);
        else if (action.equals("extract"))
            extract.extract(args);
    }

}
