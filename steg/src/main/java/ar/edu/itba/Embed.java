package ar.edu.itba;

import ar.edu.itba.exceptions.EncryptionErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Embed {
    private String filePath;
    private String bitmapFile;
    private String outFile;
    private Algorithm alg;
    private EncModeEnum mode;
    private String pass;
    private EncEnum encEnum;
    private Encryption enc;
    private final static int HEADER_SIZE = 54;
    private final static int SIZE_SIZE = 4;
    private final static int NULL_TERMINATION = 1;

    public void embed(String inputFile, String outputFile, String bmp, Algorithm algorithm, Encryption encryption)
            throws Exception {
        filePath = inputFile;
        bitmapFile = bmp;
        outFile = outputFile;
        alg = algorithm;
        if (encryption != null) {
            mode = encryption.getMode();
            pass = encryption.getPassword();
            enc = encryption;
        }
        hide();
    }

    private void hide() throws RuntimeException, IOException, EncryptionErrorException {
        final File msgFile = new File(filePath);
        final File output = new File(bitmapFile);
        final long outputSize = output.length();
        byte[] outputBytes = Files.readAllBytes(output.toPath());
        String extension = filePath.substring(filePath.lastIndexOf('.'));
        final int msgSize = (int) msgFile.length();
        byte[] msgBytes = Files.readAllBytes(msgFile.toPath());
        byte[] extensionBytes = extension.getBytes();

        if (notEnoughSize(outputSize, msgSize, extension.length())) {
            throw new RuntimeException("Not enough space for embedding file in bitmap");
        }

        byte[] sizeBytes = ByteBuffer.allocate(Integer.SIZE / 8).putInt(msgSize).array();

        if (enc != null) {
            byte[] dataToEncrypt = concatenate(sizeBytes, msgBytes, extensionBytes, new byte[] { 0 });
            byte[] encryptedData = enc.encrypt(dataToEncrypt);
            byte[] encryptedSizeBytes = ByteBuffer.allocate(Integer.SIZE / 8).putInt(encryptedData.length).array();
            byte[] toEmbed = concatenate(encryptedSizeBytes, encryptedData);

            if (notEnoughSize(outputSize, toEmbed.length, 0)) {
                throw new RuntimeException("Not enough space for embedding encrypted file in bitmap");
            }

            alg.embed(toEmbed, outputBytes, HEADER_SIZE);
        } else {
            byte[] toEmbed = concatenate(sizeBytes, msgBytes, extensionBytes, new byte[] { 0 });
            alg.embed(toEmbed, outputBytes, HEADER_SIZE);
        }

        try {
            saveImage(outFile, outputBytes);
        } catch (IOException e) {
            throw new IOException("Failed to create the embed files");
        }
    }

    private boolean notEnoughSize(long bmpSize, long msgSize, long extensionSize) {
        return bmpSize <= msgSize * alg.getBitsUsed() + HEADER_SIZE + SIZE_SIZE + extensionSize + NULL_TERMINATION;
    }

    private byte[] concatenate(byte[] array1, byte[] array2, byte[] array3, byte[] array4) {
        int totalLength = array1.length + array2.length + array3.length + array4.length;
        byte[] result = new byte[totalLength];

        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        System.arraycopy(array3, 0, result, array1.length + array2.length, array3.length);
        System.arraycopy(array4, 0, result, array1.length + array2.length + array3.length, array4.length);

        return result;
    }

    private byte[] concatenate(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    private void saveImage(String outFilePath, byte[] modifiedFile) throws IOException {
        Path path = Path.of(outFilePath);
        Files.write(path, modifiedFile);
    }
}
