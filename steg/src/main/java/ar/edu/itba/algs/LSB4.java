package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;
import java.io.FileInputStream;

public class LSB4 implements Algorithm {
    private Integer bitsUsed;

    @Override
    public byte[] run(byte blue, byte green, byte red, byte[] message, int messageByteCounter, int messageBitCounter) {
        int currentBit;
        byte[] bytes = new byte[3];
        bytes[0] = blue;
        bytes[1] = red;
        bytes[2] = green;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                currentBit = (message[messageByteCounter] >> (7 - messageBitCounter)) & 1;

                bytes[i] = (byte) ((bytes[i] & ~(1 << j)) | (currentBit << j));

                messageBitCounter++;

                if (messageBitCounter == 8) {
                    messageByteCounter++;
                    messageBitCounter = 0;
                }
            }
        }

        return bytes;
    }

    @Override
    public Integer getBitsUsed() {
        return bitsUsed;
    }
}
