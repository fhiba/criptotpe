package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

public class LSB1 implements Algorithm {

  Integer bitsUsed = 1;

  @Override
  public byte[] run(byte blue, byte green, byte red, byte[] message, int messageByteCounter, int messageBitCounter) {
    byte[] bytes = new byte[3];
    bytes[0] = blue; // Original blue value
    bytes[1] = red; // Original red value
    bytes[2] = green; // Original green value

    // Modify LSBs based on message bits, checking array bounds
    for (int i = 0; i < 3; i++) {
      if (messageByteCounter < message.length) {
        int currentBit = (message[messageByteCounter] >> (7 - messageBitCounter)) & 1;
        if (currentBit == 1) {
          bytes[i] |= 0b00000001; // Set LSB to 1
        } else {
          bytes[i] &= 0b11111110; // Set LSB to 0
        }
        messageBitCounter++;
        if (messageBitCounter == 8) {
          messageBitCounter = 0;
          messageByteCounter++;
        }
      }
    }

    return bytes;
  }

  @Override
  public void extract(byte forExtraction, byte[] msg, int byteCounter, int bitCounter) {
    int currentBit;

    currentBit = forExtraction & 1;

    msg[byteCounter] = (byte) (msg[byteCounter] | (currentBit << (7 - bitCounter)));
  }

  @Override
  public Integer getBitsUsed() {
    return bitsUsed;
  }
}
