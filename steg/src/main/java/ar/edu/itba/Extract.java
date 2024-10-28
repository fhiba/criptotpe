package ar.edu.itba;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import ar.edu.itba.algs.LSBI;
import ar.edu.itba.exceptions.DecryptionErrorException;
import ar.edu.itba.exceptions.EncryptionErrorException;

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

    private void retrieveLSBI(byte[] content) throws IOException, EncryptionErrorException, DecryptionErrorException {
        int dataSize = ByteBuffer.wrap(content, 0, Algorithm.MSG_SIZE_BYTE).getInt();
        int offset = Algorithm.MSG_SIZE_BYTE;
        byte[] data = new byte[dataSize];
        System.arraycopy(content, 4, data, 0, dataSize);
        String extension;
        if (enc != null) {
            byte[] decryptedContent = enc.decrypt(data);
            int realSize = ByteBuffer.wrap(decryptedContent, 0, 4).getInt();
            System.out.println("Real file size after decryption: " + realSize);

            byte[] messageContent = new byte[realSize];
            System.arraycopy(decryptedContent, 4, messageContent, 0, realSize);

            ByteArrayOutputStream extensionBytes = new ByteArrayOutputStream();
            int extensionOffset = 4 + realSize;
            while (extensionOffset < decryptedContent.length && decryptedContent[extensionOffset] != 0) {
                extensionBytes.write(decryptedContent[extensionOffset]);
                extensionOffset++;
            }
            extension = new String(extensionBytes.toByteArray(), StandardCharsets.UTF_8);
            System.out.println("Extracted extension: " + extension);

            saveMessage(messageContent, extension);
        } else {
            ByteArrayOutputStream extensionBytes = new ByteArrayOutputStream();
            offset += dataSize;
            while (content[offset] != 0) {
                extensionBytes.write(content[offset++]);
            }
            extension = new String(extensionBytes.toByteArray(), StandardCharsets.UTF_8);
            System.out.println("Extracted extension: " + extension);

            saveMessage(data, extension);
        }
    }

    public void retrieve() throws Exception {
        final File input = new File(encryptedBitmapFilePath);
        byte[] inputBytes = Files.readAllBytes(input.toPath());

        if (inputBytes.length < HEADER_SIZE + 32) {
            throw new IllegalStateException("Input file is too small");
        }

        ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
        int offset = alg.extract(inputBytes, contentBytes, HEADER_SIZE); // Just to check if the algorithm is valid
        byte[] content = contentBytes.toByteArray();
        if (alg instanceof LSBI) {
            retrieveLSBI(content);
            return;
        }
        byte[] messageContent;
        int realSize;
        String extension;
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
            extension = new String(extensionBytes.toByteArray(), StandardCharsets.UTF_8);
            System.out.println("Extracted extension: " + extension);

        } else {
            messageContent = content;
            extension = alg.extractExtension(inputBytes, offset);
        }

        saveMessage(messageContent, extension);
    }

    private void saveMessage(byte[] message, String extension) throws IOException {
        String outPath = outFilePath;
        if (!extension.isEmpty()) {
            if (!extension.startsWith(".")) {
                outPath += ".";
            }
            outPath += extension;
        }

        try {

            Files.write(Path.of(outPath), message);
        } catch (IOException e) {
            throw new IOException("Failed to create the extracted file: " + e.getMessage());
        }

    }
}
