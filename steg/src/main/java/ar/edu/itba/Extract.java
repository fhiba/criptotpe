package ar.edu.itba;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Extract {
    private String encryptedBitmapFilePath;
    private String outFilePath;
    private Algorithm alg;
    private Encryption enc;
    private String pass;
    private final static int HEADER_SIZE = 54;
    private final static int SIZE_SIZE = 4;
    private final static int NULL_TERMINATION = 1;

    public void extract(String bmpFilePath, String outputFilePath, Algorithm algorithm, Encryption encryption)
            throws Exception {
        this.encryptedBitmapFilePath = bmpFilePath;
        this.outFilePath = outputFilePath;
        this.alg = algorithm;
        this.enc = encryption;
        this.pass = encryption == null ? null : encryption.getPassword();

        retrieve();
    }

    public void retrieve() throws Exception {
        final File input = new File(encryptedBitmapFilePath);
        byte[] inputBytes = Files.readAllBytes(input.toPath());

        // Verify we have enough bytes for the header and size
        if (inputBytes.length < HEADER_SIZE + 32) {
            throw new IllegalStateException("Input file is too small");
        }

        // Extract the size (4 bytes)
        byte[] sizeBytes = new byte[4];
        int inputOffset = HEADER_SIZE;

        // Extract size bytes
        for (int i = 0; i < 4; i++) {
            sizeBytes[i] = alg.extract(inputBytes, inputOffset);
            // Adjust offset based on algorithm
            inputOffset += (alg.getBitsUsed() == 4) ? 2 : 8;
        }

        // Convert size bytes to integer (big endian)
        int totalSize = ByteBuffer.wrap(sizeBytes).getInt();
        System.out.println("Extracted file size: " + totalSize);

        // Validate total size based on algorithm
        long maxSize;
        if (alg.getBitsUsed() == 4) {
            maxSize = (inputBytes.length - HEADER_SIZE) / 2; // LSB4 uses 2 bytes per message byte
        } else {
            maxSize = (inputBytes.length - HEADER_SIZE) / 8; // LSB1 and LSBI use 8 bytes per message byte
        }

        if (totalSize <= 0 || totalSize > maxSize) {
            throw new IllegalStateException("Invalid extracted size: " + totalSize);
        }

        // Extract the file content
        byte[] fileContent = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            fileContent[i] = alg.extract(inputBytes, inputOffset);
            // Adjust offset based on algorithm
            inputOffset += (alg.getBitsUsed() == 4) ? 2 : 8;
        }

        // If encryption is used, decrypt the content
        if (enc != null) {
            fileContent = enc.decrypt(fileContent);
        }

        // Extract extension - read until null byte
        ByteArrayOutputStream extensionBytes = new ByteArrayOutputStream();
        byte extractedByte;
        do {
            if (inputOffset >= inputBytes.length) {
                throw new IllegalStateException("No null terminator found for extension");
            }
            extractedByte = alg.extract(inputBytes, inputOffset);
            if (extractedByte != 0) {
                extensionBytes.write(extractedByte);
            }
            // Adjust offset based on algorithm
            inputOffset += (alg.getBitsUsed() == 4) ? 2 : 8;
        } while (extractedByte != 0);

        String extension = new String(extensionBytes.toByteArray(), StandardCharsets.UTF_8);
        System.out.println("Extracted extension: " + extension);

        // Save the file with the correct extension
        try {
            String outPath = outFilePath;
            if (!extension.isEmpty()) {
                if (!extension.startsWith(".")) {
                    outPath += ".";
                }
                outPath += extension;
            }
            Files.write(Path.of(outPath), fileContent);
        } catch (IOException e) {
            throw new IOException("Failed to create the extracted file: " + e.getMessage());
        }
    }
}
