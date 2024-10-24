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

    if (!msgFile.exists() || !msgFile.isFile()) {
      throw new RuntimeException("Message file not found or invalid file path.");
    }

    FileInputStream fis = new FileInputStream(bmpFile);
    FileInputStream message = new FileInputStream(msgFile);
    byte[] messageBytes = message.readAllBytes();
    message.close();

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
    int rowSize = (width * 3 + 3) & ~3;

    byte[] pixelData = new byte[rowSize * height];
    fis.read(pixelData);
    fis.close();

    System.out.println("Image width: " + width);
    System.out.println("Image height: " + height);

    byte[] storeSize = ByteBuffer.allocate(Long.BYTES).putLong(fullSize).array();
    int sizeBitCounter = 0;
    int sizeByteCounter = 0;

    int messageBitCounter = 0;
    int messageByteCounter = 0;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int pixelIndex = y * rowSize + x * 3;

        byte blue = (byte) (pixelData[pixelIndex] & 0xFF);
        byte green = (byte) (pixelData[pixelIndex + 1] & 0xFF);
        byte red = (byte) (pixelData[pixelIndex + 2] & 0xFF);

        if (sizeByteCounter < storeSize.length) {
          blue = (byte) ((blue & ~1) | ((storeSize[sizeByteCounter] >> sizeBitCounter) & 1));
          sizeBitCounter++;
          if (sizeBitCounter == 8) {
            sizeBitCounter = 0;
            sizeByteCounter++;
          }
        } else if (messageByteCounter < messageBytes.length) {
          blue = (byte) ((blue & ~1) | ((messageBytes[messageByteCounter] >> messageBitCounter) & 1));
          messageBitCounter++;
          if (messageBitCounter == 8) {
            messageBitCounter = 0;
            messageByteCounter++;
          }
        } else {
          break;
        }

        int rgb = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
        image.setRGB(x, height - y - 1, rgb);
      }
      if (messageByteCounter >= messageBytes.length && sizeByteCounter >= storeSize.length) {
        break;
      }
    }

    ImageIO.write(image, "bmp", new File(outFile));
    System.out.println("Message hidden successfully.");
  }

}
