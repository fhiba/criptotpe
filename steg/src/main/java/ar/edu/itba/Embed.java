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

    byte[] header = new byte[54];
    fis.read(header);

    BufferedImage image = ImageIO.read(bmpFile);
    int width = image.getWidth();
    int height = image.getHeight();
    int rowSize = (width * 3) & ~3; // Each row is padded to a multiple of 4 bytes

    byte[] pixelData = new byte[rowSize * height];
    fis.read(pixelData);
    fis.close();

    System.out.println("Image width: " + width);
    System.out.println("Image height: " + height);

    byte[] dataToEmbed;
    if (messageBytes.length == 0) {
      System.out.println("Empty message. Embedding size, file data, and extension.");

      dataToEmbed = new byte[4 + 2 + extension.length()];
      ByteBuffer.wrap(dataToEmbed, 0, 4).putInt((int) fullSize);
      ByteBuffer.wrap(dataToEmbed, 4, 2).putShort((short) extension.length());
      System.arraycopy(extension.getBytes(), 0, dataToEmbed, 6, extension.length());
    } else {
      dataToEmbed = messageBytes;
    }

    int messageBitCounter = 0;
    int messageByteCounter = 0;
    int pixelIndex = 0;
    byte[] modifiedPixels;
    byte blue, green, red;

    byte[] storeSize = ByteBuffer.allocate(Long.BYTES).putLong(fullSize).array();
    int sizeBitCounter = 0;
    int sizeByteCounter = 0;
    int x = 0, y = 0;

    // Steganography of the size + padding and extension
    for (y = 0; y < height; y++) {
      for (x = 0; x < width; x++) {
        pixelIndex = y * rowSize + x * 3;
        blue = (byte) (pixelData[pixelIndex] & 0xFF);
        green = (byte) (pixelData[pixelIndex + 1] & 0xFF);
        red = (byte) (pixelData[pixelIndex + 2] & 0xFF);

        if (sizeByteCounter < storeSize.length) {
          modifiedPixels = alg.run(blue, green, red, storeSize, sizeByteCounter, sizeBitCounter);
          if (sizeBitCounter + 3 * alg.getBitsUsed() >= 8) {
            sizeByteCounter++;
            sizeBitCounter = (sizeBitCounter + 3 * alg.getBitsUsed()) % 8;
          } else {
            sizeBitCounter += 3 * alg.getBitsUsed();
          }
        } else {
          modifiedPixels = new byte[] { blue, green, red };
        }

        int rgb = ((modifiedPixels[2] & 0xFF) << 16) | ((modifiedPixels[1] & 0xFF) << 8) | (modifiedPixels[0] & 0xFF);
        image.setRGB(x, height - y - 1, rgb);
      }
    }

    // Steganography of the message or the empty data
    for (; y < height; y++) {
      for (x = 0; x < width; x++) {
        pixelIndex = y * rowSize + x * 3;
        blue = (byte) (pixelData[pixelIndex] & 0xFF);
        green = (byte) (pixelData[pixelIndex + 1] & 0xFF);
        red = (byte) (pixelData[pixelIndex + 2] & 0xFF);
        if (messageByteCounter < dataToEmbed.length) {
          modifiedPixels = alg.run(blue, green, red, dataToEmbed, messageByteCounter, messageBitCounter);
          if (messageBitCounter + 3 * alg.getBitsUsed() >= 8) {
            messageByteCounter++;
            messageBitCounter = (messageBitCounter + 3 * alg.getBitsUsed()) % 8;
          } else {
            messageBitCounter += 3 * alg.getBitsUsed();
          }
        } else {
          modifiedPixels = new byte[] { blue, green, red };
        }

        int rgb = ((modifiedPixels[2] & 0xFF) << 16) | ((modifiedPixels[1] & 0xFF) << 8) | (modifiedPixels[0] & 0xFF);
        image.setRGB(x, height - y - 1, rgb);
      }
    }

    // Steganography of the extension
    byte[] extensionBytes = extension.getBytes();
    int exBitCounter = 0;
    int exByteCounter = 0;

    for (; y < height; y++) {
      for (x = 0; x < width; x++) {
        pixelIndex = y * rowSize + x * 3;
        blue = (byte) (pixelData[pixelIndex] & 0xFF);
        green = (byte) (pixelData[pixelIndex + 1] & 0xFF);
        red = (byte) (pixelData[pixelIndex + 2] & 0xFF);

        if (exByteCounter < extensionBytes.length) {
          modifiedPixels = alg.run(blue, green, red, extensionBytes, exByteCounter, exBitCounter);
          if (exBitCounter + 3 * alg.getBitsUsed() >= 8) {
            exByteCounter++;
            exBitCounter = (exBitCounter + 3 * alg.getBitsUsed()) % 8;
          } else {
            exBitCounter += 3 * alg.getBitsUsed();
          }
        } else {
          modifiedPixels = new byte[] { blue, green, red };
        }

        int rgb = ((modifiedPixels[2] & 0xFF) << 16) | ((modifiedPixels[1] & 0xFF) << 8) | (modifiedPixels[0] & 0xFF);
        image.setRGB(x, height - y - 1, rgb);
      }
    }

    ImageIO.write(image, "bmp", new File(outFile));
  }

}
