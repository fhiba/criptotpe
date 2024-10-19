package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

public class LSBI implements Algorithm {
  private Integer bitsUsed = 1;

  @Override
  public byte[] run(byte blue, byte green, byte red, byte[] message, int messageByteCounter, int messageBitCounter) {
    byte[] bytes = new byte[3];
    bytes[0] = blue;
    bytes[1] = red;
    bytes[2] = green;

    int[] bitsModified = new int[4];
    int[] bitsNotModified = new int[4];

    collectModificationData(bytes, message, messageByteCounter, messageBitCounter, bitsModified, bitsNotModified);

    int[] inversionBits = calculateInversionBits(bitsModified, bitsNotModified);

    return embedMessageBytes(bytes, message, messageByteCounter, messageBitCounter, inversionBits);
  }

  private void collectModificationData(byte[] bytes, byte[] message, int messageByteCounter, int messageBitCounter,
      int[] bitsModified, int[] bitsNotModified) {
    for (int i = 0; i < 3; i++) {
      if (i == 2) { // Skip red byte
        continue;
      }

      int currentBit = (message[messageByteCounter] >> (7 - messageBitCounter)) & 1;
      int patternIndex = (bytes[i] >> 1) & 0x03;

      if (currentBit == (bytes[i] & 1)) {
        bitsNotModified[patternIndex]++;
      } else {
        bitsModified[patternIndex]++;
      }

      messageBitCounter++;
      if (messageBitCounter == 8) {
        messageByteCounter++;
        messageBitCounter = 0;
      }
    }
  }

  private int[] calculateInversionBits(int[] bitsModified, int[] bitsNotModified) {
    int[] inversionBits = new int[4];
    for (int i = 0; i < 4; i++) {
      inversionBits[i] = bitsModified[i] > bitsNotModified[i] ? 1 : 0;
    }
    return inversionBits;
  }

  private byte[] embedMessageBytes(byte[] bytes, byte[] message, int messageByteCounter, int messageBitCounter,
      int[] inversionBits) {
    for (int i = 0; i < 3; i++) {
      if (i == 2)
        continue;

      int currentBit = (message[messageByteCounter] >> (7 - messageBitCounter)) & 1;
      int patternIndex = (bytes[i] >> 1) & 0x03;

      if (inversionBits[patternIndex] == 1) {
        currentBit ^= 1;
      }

      bytes[i] = (byte) ((bytes[i] & 0xFE) | currentBit);

      messageBitCounter++;
      if (messageBitCounter == 8) {
        messageByteCounter++;
        messageBitCounter = 0;
      }
    }

    return bytes;
  }

  @Override
  public Integer getBitsUsed() {
    return bitsUsed;
  }
}
