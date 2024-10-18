package ar.edu.itba;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

public class Embed {
    private String filePath;
    private String bitmapFile;
    private String outFile;
    private Algorithm alg;
    private String mode;
    private String pass;
    private String enc;

    public void embed(String[] args) throws Exception {
        filePath = args[2];
        bitmapFile = args[4];
        outFile = args[6];
        alg = AlgEnum.getAlg(args[8]).get();
        enc = args[10];
        mode = args[12];
        if (args[14] != null)
            pass = args[14];
        // enc.setMode(mode);
        hide();
    }

    public void hide() throws Exception {
        File bmpFile = new File(bitmapFile);
        FileInputStream fis = new FileInputStream(bmpFile);
        FileInputStream message = new FileInputStream(new File(filePath));
        byte[] messageBytes = message.readAllBytes();
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
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte blue = (byte) (pixelData[pixelIndex] & 0xFF); // Blue
                byte green = (byte) (pixelData[pixelIndex + 1] & 0xFF); // Green
                byte red = (byte) (pixelData[pixelIndex + 2] & 0xFF); // Red

                modifiedPixels = alg.run(blue, green, red, messageBytes, messageByteCounter, messageBitCounter);

                // me fijo si puedo sumar 3 en el bit counter o si tengo que adelantar el byte
                if (messageBitCounter + 3 >= 7) {
                    messageByteCounter++;
                    messageBitCounter = (messageBitCounter + 3) % 8;
                } else {
                    messageBitCounter += 3;
                }

                int rgb = (modifiedPixels[0] << 16) | (modifiedPixels[1] << 8) | modifiedPixels[2];
                image.setRGB(x, height - y - 1, rgb);

                pixelIndex += 3;
            }
        }

        // Save the image as a PNG (or any other format)
        ImageIO.write(image, "png", new File(outFile));
        System.out.println("BMP converted to PNG and saved.");
    }
}
