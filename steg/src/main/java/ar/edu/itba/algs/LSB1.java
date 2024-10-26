package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

public class LSB1 implements Algorithm {

    Integer bitsUsed = 1;

    @Override
    public int embed(int color, byte[] message, int messageByteCounter, int messageBitCounter) {

        int currentBit = (message[messageByteCounter] >> (7 - messageBitCounter)) & 1;

        if (currentBit == 1) {
            color |= 0x01;
        } else {
            color &= 0xFE;
        }

        return color;

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
