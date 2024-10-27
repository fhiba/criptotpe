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

        if (inputBytes.length < HEADER_SIZE + 32) {
            throw new IllegalStateException("Input file is too small");
        }

        byte[] sizeBytes = new byte[4];
        int inputOffset = HEADER_SIZE;
        for (int i = 0; i < 4; i++) {
            sizeBytes[i] = alg.extract(inputBytes, inputOffset);
            inputOffset += (alg.getBitsUsed() == 4) ? 2 : 8;
        }

        int firstSize = ByteBuffer.wrap(sizeBytes).getInt();
        System.out.println("First extracted size: " + firstSize);

        long maxSize = (inputBytes.length - HEADER_SIZE) / (alg.getBitsUsed() == 4 ? 2 : 8);
        if (firstSize <= 0 || firstSize > maxSize) {
            throw new IllegalStateException("Invalid extracted size: " + firstSize);
        }

        byte[] content = new byte[firstSize];
        for (int i = 0; i < firstSize; i++) {
            content[i] = alg.extract(inputBytes, inputOffset);
            inputOffset += (alg.getBitsUsed() == 4) ? 2 : 8;
        }

        byte[] messageContent;
        int realSize;

        if (enc != null) {
            byte[] decryptedContent = enc.decrypt(content);

            realSize = ByteBuffer.wrap(decryptedContent, 0, 4).getInt();
            System.out.println("Real file size after decryption: " + realSize);

            messageContent = new byte[realSize];
            System.arraycopy(decryptedContent, 4, messageContent, 0, realSize);

            ByteArrayOutputStream extensionBytes = new ByteArrayOutputStream();
            int extensionOffset = 4 + realSize;
            while (extensionOffset < decryptedContent.length && decryptedContent[extensionOffset] != 0) {
                extensionBytes.write(decryptedContent[extensionOffset]);
                extensionOffset++;
            }
            String extension = new String(extensionBytes.toByteArray(), StandardCharsets.UTF_8);
            System.out.println("Extracted extension: " + extension);

            try {
                String outPath = outFilePath;
                if (!extension.isEmpty()) {
                    if (!extension.startsWith(".")) {
                        outPath += ".";
                    }
                    outPath += extension;
                }
                Files.write(Path.of(outPath), messageContent);
            } catch (IOException e) {
                throw new IOException("Failed to create the extracted file: " + e.getMessage());
            }
        } else {
            messageContent = new byte[firstSize];
            System.arraycopy(content, 0, messageContent, 0, firstSize);

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
                inputOffset += (alg.getBitsUsed() == 4) ? 2 : 8;
            } while (extractedByte != 0);

            String extension = new String(extensionBytes.toByteArray(), StandardCharsets.UTF_8);
            System.out.println("Extracted extension: " + extension);

            try {
                String outPath = outFilePath;
                if (!extension.isEmpty()) {
                    if (!extension.startsWith(".")) {
                        outPath += ".";
                    }
                    outPath += extension;
                }
                Files.write(Path.of(outPath), messageContent);
            } catch (IOException e) {
                throw new IOException("Failed to create the extracted file: " + e.getMessage());
            }
        }
    }
}
