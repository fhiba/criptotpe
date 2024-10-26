package ar.edu.itba;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
        if (encryption != null) {
            mode = encryption.getMode();
            pass = encryption.getPassword();
            enc = encryption;
        }
        hide();
    }

    public boolean canStore(long imageSize, long messageSize) {
        return messageSize * 8 <= imageSize * alg.getBitsUsed();
    }

    public void hide() throws Exception {
        File bmpFile = new File(bitmapFile);
        File msgFile = new File(filePath);
        FileInputStream fis = new FileInputStream(bmpFile);
        FileInputStream message = new FileInputStream(msgFile);
        byte[] messageBytes = message.readAllBytes();
        System.out.println("File path: " + filePath);

        String extension = null;
        long fullSize = 0;

        String[] auxStrings = filePath.split("\\.");

        if (auxStrings.length > 1) {
            extension = auxStrings[auxStrings.length - 1];
            System.out.println("Extracted extension: " + extension);

            long msgFileLength = msgFile.length();
            System.out.println("Message file length: " + msgFileLength);

            fullSize = msgFileLength + 4 + 2 + extension.length();
            System.out.println("Full size: " + fullSize);
        } else {
            System.out.println("Error: No extension found in the file path.");
        }

        if (fullSize <= 0) {
            fis.close();
            message.close();
            throw new RuntimeException("Full size calculation resulted in an invalid size.");
        }

        if (!canStore(bmpFile.length() - 54, fullSize)) {
            fis.close();
            message.close();
            throw new RuntimeException("Cannot store the message in the image.");
        }

        byte[] header = new byte[54];
        fis.read(header);

        BufferedImage image = ImageIO.read(bmpFile);
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] storeSize = ByteBuffer.allocate(Integer.BYTES).putInt((int) fullSize).array();
        int messageIndex = 0;
        Integer bitCounter = 0;
        Integer byteCounter = 0;
        int[] colors = new int[3];
        int x = 0, y = 0;
        int modifiedBmpBytes = 1;
        // solo para embedear el tamaño total del mensaje
        for (int j = y; j < height; j++) {
            for (int i = x; i < width; i++) {
                colors = getColors(i, j, image);
                for (int k = 0; k < 3; k++) {
                    if (modifiedBmpBytes >= 4) {
                        x = i;
                        y = j;
                        break;
                    }
                    colors[k] = alg.embed(colors[k], storeSize, byteCounter, bitCounter);
                    bitCounter += alg.getBitsUsed();
                    if (bitCounter >= 8) {
                        byteCounter++;
                        bitCounter = 0;
                    }
                    modifiedBmpBytes++;
                }
                if (modifiedBmpBytes >= 4)
                    break;
                setNewColors(i, j, colors, image);
            }
            if (modifiedBmpBytes >= 4)
                break;
        }
        System.out.println("sali del size");
        bitCounter = 0;
        byteCounter = 0;
        int msgSize = (int) (fullSize - extension.length() - 6); // el magic number 6 viene de 4 por el size y 2 del . y
                                                                 // \0
        // embed del mensaje completo sin extension
        for (int j = y; j < height; j++) {
            for (int i = x; i < width; i++) {
                if (msgSize == 1) { // Changed from msgSize == 1
                    x = i;
                    y = j;
                    break;
                }

                colors = getColors(i, j, image);

                for (int k = 0; k < 3; k++) {
                    if (byteCounter >= messageBytes.length) {
                        break;
                    }
                    colors[k] = alg.embed(colors[k], messageBytes, byteCounter, bitCounter);
                    bitCounter += alg.getBitsUsed();
                    if (bitCounter >= 8) {
                        byteCounter++;
                        bitCounter = 0;
                        msgSize--;
                    }
                }
                System.out.println((char) messageBytes[byteCounter]);
                setNewColors(i, j, colors, image);
            }
            if (msgSize == 1)
                break;
        }
        System.out.println("sali del msj");
        bitCounter = 0;
        byteCounter = 0;
        int extensionSize = extension.length();
        byte[] extensionBytes = extension.getBytes();
        for (int j = y; j < height; j++) {
            for (int i = x; i < width; i++) {
                if (extensionSize == 1) {
                    x = i;
                    y = j;
                    break;
                }
                colors = getColors(i, j, image);

                for (int k = 0; k < 3; k++) {
                    if (byteCounter >= extensionBytes.length) {
                        break;
                    }
                    colors[k] = alg.embed(colors[k], extensionBytes, byteCounter, bitCounter);
                    bitCounter += alg.getBitsUsed();
                    if (bitCounter >= 8) {
                        byteCounter++;
                        bitCounter = 0;
                        extensionSize--;
                    }
                }
                setNewColors(i, j, colors, image);
            }
            if (extensionSize == 1)
                break;
        }
        System.out.println("sali de la extension");
        try {
            File out = new File(outFile);
            ImageIO.write(image, "bmp", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[] getColors(int x, int y, BufferedImage image) {
        int pixel = image.getRGB(x, y);
        Color color = new Color(pixel, true);
        return new int[] { color.getBlue(), color.getGreen(), color.getRed() };
    }

    private void setNewColors(int i, int j, int[] colors, BufferedImage image) {
        Color newColor = new Color(colors[2], colors[1], colors[0]);
        image.setRGB(i, j, newColor.getRGB());
    }

}
