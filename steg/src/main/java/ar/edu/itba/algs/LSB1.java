package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

public class LSB1 implements Algorithm {

    Integer bitsUsed = 1;

    @Override
    public int embed(byte[] message, byte[] output, int offset) {
        int currentOffset = offset;

        for (byte b : message) {
            // Process each bit of the byte
            for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                // Extract the current bit from the byte to embed
                int bit = (b >> bitIndex) & 1;

                // Clear the LSB of the output byte and set it to our bit
                output[currentOffset] = (byte) ((output[currentOffset] & 0xFE) | bit);
                currentOffset++;
            }

        }
        return currentOffset;
    }

    @Override
    public byte extract(byte[] inputBytes, int startOffset) {
        byte extractedByte = 0;
        for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
            if (startOffset >= inputBytes.length) {
                throw new IllegalStateException("Unexpected end of file while extracting bits");
            }
            // Get LSB from current input byte
            int bit = inputBytes[startOffset] & 1;
            // Place the bit in the correct position
            extractedByte |= (byte) (bit << bitIndex);
            startOffset++;
        }
        return extractedByte;
    }



    @Override
    public Integer getBitsUsed() {
        return bitsUsed;
    }
}
