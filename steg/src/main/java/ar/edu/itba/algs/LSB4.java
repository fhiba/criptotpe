package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

public class LSB4 implements Algorithm {
    private Integer bitsUsed = 4;

    @Override
    public int embed(byte[] message, byte[] output, int offset) {
        int currentOffset = offset;
        for (byte b : message) {
            // We need to spread each byte across two output bytes (4 bits each)
            // First output byte gets the high nibble
            int highNibble = (b >> 4) & 0x0F;
            output[currentOffset] = (byte) ((output[currentOffset] & 0xF0) | highNibble);
            currentOffset++;

            // Second output byte gets the low nibble
            int lowNibble = b & 0x0F;
            output[currentOffset] = (byte) ((output[currentOffset] & 0xF0) | lowNibble);
            currentOffset++;
        }
        return currentOffset;
    }

    @Override
    public byte extract(byte[] inputBytes, int startOffset) {
        if (startOffset + 1 >= inputBytes.length) {
            throw new IllegalStateException("Unexpected end of file while extracting bits");
        }

        // Extract high nibble from first byte
        int highNibble = inputBytes[startOffset] & 0x0F;

        // Extract low nibble from second byte
        int lowNibble = inputBytes[startOffset + 1] & 0x0F;

        // Combine nibbles into a byte
        return (byte) ((highNibble << 4) | lowNibble);
    }

    @Override
    public Integer getBitsUsed() {
        return bitsUsed;
    }
}
