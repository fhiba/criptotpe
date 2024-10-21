package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

public class LSB1 implements Algorithm {

    Integer bitsUsed = 1;

    @Override
    public byte[] run(byte blue, byte green, byte red, byte[] message, int messageByteCounter, int messageBitCounter) {
        int currentBit;
        byte[] bytes = new byte[3];
        bytes[0] = blue;
        bytes[1] = red;
        bytes[2] = green;
        for (int i = 0; i < 3; i++) {
            currentBit = (message[messageBitCounter] >> (7 - i)) & 1;
            if (currentBit == 1) {
                bytes[i] = (byte) (bytes[i] | 0b00000001); // Set the least significant bit to 1
            } else {
                bytes[i] = (byte) (bytes[i] & 0b11111110); // Set the least significant bit to 0
            }
            messageBitCounter++;
            if (messageBitCounter == 8) {
                messageByteCounter++;
                messageBitCounter = 0;
            }
        }
        return bytes;
    }

    @Override
    public void extract(byte forExtraction, byte[] msg, int byteCounter, int bitCounter) {
        int currentBit;
        currentBit = forExtraction & 1;

        msg[byteCounter] = (byte) (msg[byteCounter] | (currentBit << (7 - bitCounter)));

        bitCounter++;

        if (bitCounter == 8) {
            byteCounter++;
            bitCounter = 0;
        }

    }

    @Override
    public Integer getBitsUsed() {
        return bitsUsed;
    }
}
