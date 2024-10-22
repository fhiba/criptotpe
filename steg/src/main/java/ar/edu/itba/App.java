package ar.edu.itba;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Supplier;

public class App {
    Embed embed;
    Extract extract;

    CommandLine cmd;
    Algorithm algorithm;
    Encryption encryption;

    public void setUp(String[] args) {
        // Define CLI options
        Options opts = new Options();

        // Embedding and extraction options
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

        // Input and output files
        opts.addOption(Option.builder()
                .longOpt("in")
                .desc("Input file for embedding")
                .required(false)
                .hasArg()
                .argName("FILE")
                .type(String.class)
                .build());

        opts.addOption(Option.builder()
                .longOpt("out")
                .desc("Output file: Modified BMP (embed) or message (extract)")
                .required(true)
                .hasArg()
                .argName("FILE")
                .type(String.class)
                .build());

        opts.addOption(Option.builder()
                .option("p")
                .required(true)
                .hasArg()
                .argName("BMP")
                .desc("BMP file for embedding/extracting")
                .build());

        // Steganography and encryption options
        opts.addOption(Option.builder()
                .longOpt("steg")
                .desc("Steganography method: LSB1 | LSB4 | LSBI")
                .required(true)
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

        // Initialize parser and formatter
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {

            // Parse command-line arguments
            cmd = parser.parse(opts, args);

            // Mutually exclusive check for embed/extract
            if ((cmd.hasOption("embed") && cmd.hasOption("extract"))
                    || (!cmd.hasOption("embed") && !cmd.hasOption("extract"))) {
                System.out.println("Error: You must choose either embed or extract.");
                formatter.printHelp("steg", opts);
                System.exit(1);
            }

            // Password is required if encryption options are present
            if ((cmd.hasOption("m") || cmd.hasOption("a")) && !cmd.hasOption("pass")) {
                System.out.println("Error: You must provide a password for encryption.");
                formatter.printHelp("steg", opts);
                System.exit(1);
            }

            // Input file is required for embedding
            if (cmd.hasOption("embed") && !cmd.hasOption("in")) {
                System.out.println("Error: You must provide an input file for embedding.");
                formatter.printHelp("steg", opts);
                System.exit(1);
            }

            Supplier<Algorithm> alg = AlgEnum.getAlg(cmd.getOptionValue("steg"));
            if (alg == null) {
                System.exit(1);
            }

            algorithm = alg.get();

            if (cmd.hasOption("pass")) {
                encryption = new Encryption(EncModeEnum.getMode(cmd.getOptionValue("m")),
                        EncEnum.getEncryption(cmd.getOptionValue("a")), cmd.getOptionValue("pass"));
            }

        } catch (ParseException e) {
            System.out.println("Error parsing command line arguments: " + e.getMessage());
            formatter.printHelp("steg", opts);
            System.exit(1);

        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.setUp(args);
        try {
            if (app.cmd.hasOption("embed")) {
                app.embed = new Embed();
                app.embed.embed(app.cmd.getOptionValue("in"), app.cmd.getOptionValue("out"),
                        app.cmd.getOptionValue("p"), app.algorithm, app.encryption);
                app.embed.hide();
            } else {
                app.extract = new Extract();
                app.extract.extract(app.cmd.getOptionValue("p"), app.cmd.getOptionValue("out"), app.algorithm,
                        app.encryption);
                app.extract.retrieve();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);

        }

    }

}
