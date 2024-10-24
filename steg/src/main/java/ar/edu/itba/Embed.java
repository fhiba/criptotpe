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

    byte[] header = new byte[54]; // salteamos el header de bitmap v3
    fis.read(header);

    byte[] pixelData = new byte[fis.available()];
    fis.read(pixelData);
    fis.close();

    BufferedImage image = ImageIO.read(bmpFile);

    int width = image.getWidth();
    int height = image.getHeight();
    System.out.println("Image width: " + width);
    System.out.println("Image height: " + height);

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

    // byte[] encryptedMsg = enc.encrypt(messageBytes);
    byte[] encryptedMsg = messageBytes;

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
