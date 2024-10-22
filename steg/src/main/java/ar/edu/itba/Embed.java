package ar.edu.itba;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class Embed {
    private String filePath;
    private String bitmapFile;
    private String outFile;
    private Algorithm alg;
    private EncModeEnum mode;
    private String pass;
    private EncEnum encEnum;
    private Encryption enc;

    public void embed(String inputFile, String outputFile, String bmp, Algorithm algorithm, Encryption encryption)
            throws Exception {
        filePath = inputFile;
        bitmapFile = bmp;
        outFile = outputFile;
        alg = algorithm;
        mode = encryption.getMode();
        pass = encryption.getPassword();
        enc = encryption;
        hide();

    }

    public void embed(String[] args) throws Exception {
        filePath = args[2];
        bitmapFile = args[4];
        outFile = args[6];
        alg = AlgEnum.getAlg(args[8]).get();
        encEnum = EncEnum.getEncryption(args[10]);
        mode = EncModeEnum.getMode(args[12]);
        if (args[14] != null)
            pass = args[14];
        else {
            pass = null;
        }
        enc = new Encryption(mode, encEnum, pass);
        hide();
    }

    public boolean canStore(long imageSize, long messageSize) {
        return messageSize * 8 > imageSize * alg.getBitsUsed();
    }

    public void hide() throws Exception {
        File bmpFile = new File(bitmapFile);
        File msgFile = new File(filePath);
        FileInputStream fis = new FileInputStream(bmpFile);
        FileInputStream message = new FileInputStream(msgFile);
        byte[] messageBytes = message.readAllBytes();
        String[] auxStrings = filePath.split("[.].+$");
        String extension = auxStrings[auxStrings.length - 1];
        String auxFileString = filePath.substring(filePath.indexOf("."));
        long fullSize = msgFile.length() + 4 + 2 + extension.length();

        if (!canStore(bmpFile.length() - 54, fullSize)) {
            fis.close();
            message.close();
            throw new RuntimeException();
        }

        byte[] header = new byte[54]; // salteamos el header de bitmap v3
        fis.read(header);

        byte[] pixelData = new byte[fis.available()];
        fis.read(pixelData);
        fis.close();

        BufferedImage image = ImageIO.read(bmpFile);

        int width = image.getWidth();
        int height = image.getWidth();

        int messageBitCounter = 0;
        int messageByteCounter = 0;
        int pixelIndex = 0;
        byte[] modifiedPixels;
        byte blue = (byte) (pixelData[pixelIndex] & 0xFF); // Blue
        byte green = (byte) (pixelData[pixelIndex + 1] & 0xFF); // Green
        byte red = (byte) (pixelData[pixelIndex + 2] & 0xFF); // Red

        if (pass != null) {
            if (fullSize % 8 != 0)
                fullSize += fullSize % 8;
        }

        byte[] storeSize = ByteBuffer.allocate(Long.BYTES).putLong(fullSize).array();
        int sizeBitCounter = 0;
        int sizeByteCounter = 3;
        int x = 0, y = 0;
        // steganografia del tamaño + padding y extension
        for (y = 0; y < height; y++) {
            for (x = 0; x < 4; x++) {
                blue = (byte) (pixelData[pixelIndex] & 0xFF); // Blue
                green = (byte) (pixelData[pixelIndex + 1] & 0xFF); // Green
                red = (byte) (pixelData[pixelIndex + 2] & 0xFF); // Red

                modifiedPixels = alg.run(blue, green, red, storeSize, sizeByteCounter, sizeBitCounter);

                // me fijo si puedo sumar 3 en el bit counter o si tengo que adelantar el byte
                if (sizeBitCounter + 3 * alg.getBitsUsed() >= 8) {
                    sizeByteCounter++;
                    sizeBitCounter = (sizeBitCounter + 3 * alg.getBitsUsed()) % 8;
                } else {
                    sizeBitCounter += 3 * alg.getBitsUsed();
                }

                int rgb = (modifiedPixels[0] << 16) | (modifiedPixels[1] << 8) | modifiedPixels[2];
                image.setRGB(x, height - y - 1, rgb);

                pixelIndex += 3;
            }
        }

        byte[] encryptedMsg = enc.encrypt(messageBytes);

        // steganografia del mensaje
        for (; y < height; y++) {
            for (; x < width; x++) {
                blue = (byte) (pixelData[pixelIndex] & 0xFF); // Blue
                green = (byte) (pixelData[pixelIndex + 1] & 0xFF); // Green
                red = (byte) (pixelData[pixelIndex + 2] & 0xFF); // Red

                modifiedPixels = alg.run(blue, green, red, encryptedMsg, messageByteCounter, messageBitCounter);

                // me fijo si puedo sumar 3 en el bit counter o si tengo que adelantar el byte
                if (messageBitCounter + 3 * alg.getBitsUsed() >= 8) {
                    messageByteCounter++;
                    messageBitCounter = (messageBitCounter + 3 * alg.getBitsUsed()) % 8;
                } else {
                    messageBitCounter += 3 * alg.getBitsUsed();
                }

                int rgb = (modifiedPixels[0] << 16) | (modifiedPixels[1] << 8) | modifiedPixels[2];
                image.setRGB(x, height - y - 1, rgb);

                pixelIndex += 3;
            }
        }

        byte[] extensionBytes = extension.getBytes();
        int exBitCounter = 0;
        int exByteCounter = 0;

        // steganografia de la extension
        for (; y < height; y++) {
            for (; x < width; x++) {
                blue = (byte) (pixelData[pixelIndex] & 0xFF); // Blue
                green = (byte) (pixelData[pixelIndex + 1] & 0xFF); // Green
                red = (byte) (pixelData[pixelIndex + 2] & 0xFF); // Red

                modifiedPixels = alg.run(blue, green, red, extensionBytes, exByteCounter, exBitCounter);

                // me fijo si puedo sumar 3 en el bit counter o si tengo que adelantar el byte
                if (exBitCounter + 3 * alg.getBitsUsed() >= 8) {
                    exByteCounter++;
                    exBitCounter = (exBitCounter + 3 * alg.getBitsUsed()) % 8;
                } else {
                    exBitCounter += 3 * alg.getBitsUsed();
                }

                int rgb = (modifiedPixels[0] << 16) | (modifiedPixels[1] << 8) | modifiedPixels[2];
                image.setRGB(x, height - y - 1, rgb);

                pixelIndex += 3;
            }
        }

        ImageIO.write(image, "bmp", new File(outFile));
    }
}
