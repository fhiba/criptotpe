package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

public class LSB1 implements Algorithm {

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
                bytes[i] = (byte) (bytes[i] | 0b10000000);
            } else {
                bytes[i] = (byte) (bytes[i] & 0b01111111);
            }
            messageBitCounter++;
            if (messageBitCounter == 7) {
                messageByteCounter++;
                messageBitCounter = 0;
            }
        }
        return bytes;
    }

}
