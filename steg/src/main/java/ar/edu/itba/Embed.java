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

        byte[] header = new byte[54]; // salteamos el header de bitmap v3
        fis.read(header);

        byte[] pixelData = new byte[fis.available()];
        fis.read(pixelData);
        fis.close();

        BufferedImage image = ImageIO.read(bmpFile);

        int width = image.getWidth();
        int height = image.getWidth();

        int messageBitCounter = 0;
        int pixelIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int blue = pixelData[pixelIndex] & 0xFF; // Blue
                int green = pixelData[pixelIndex + 1] & 0xFF; // Green
                int red = pixelData[pixelIndex + 2] & 0xFF; // Red

                int rgb = (red << 16) | (green << 8) | blue;

                // LSB(blue,green,red,message,messageBitCounter);
                // UTILIZAR LSB aca para modificar los pixeles
                image.setRGB(x, height - y - 1, rgb);

                pixelIndex += 3;
                messageBitCounter += 3;
            }
        }

        // Save the image as a PNG (or any other format)
        ImageIO.write(image, "png", new File("output_image.png"));
        System.out.println("BMP converted to PNG and saved.");
    }
}
