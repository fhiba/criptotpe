package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

public class LSBI implements Algorithm {
    private Integer bitsUsed = 1;
    // In RGB format, pixels are stored as RGB (3 bytes per pixel)
    private static final int BYTES_PER_PIXEL = 3;
    private static final int RED_OFFSET = 0;  // Red is the first byte in RGB format

    @Override
    public int embed(byte[] message, byte[] output, int offset) {
        int currentOffset = offset;

        for (byte b : message) {
            // Process each bit of the byte
            for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                if (currentOffset >= output.length) {
                    throw new IllegalStateException("Output buffer overflow");
                }

                // Get the bit to embed
                int messageBit = (b >> bitIndex) & 1;

                // Get the current pixel value
                int pixelValue = output[currentOffset] & 0xFF;

                // Calculate if we should flip the LSB based on LSBI algorithm
                boolean shouldFlip = shouldFlipLSB(pixelValue, messageBit);

                if (shouldFlip) {
                    // Flip the LSB
                    output[currentOffset] = (byte) (pixelValue ^ 1);
                }

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

            // Get pixel value
            int pixelValue = inputBytes[startOffset] & 0xFF;

            // Extract bit using LSBI algorithm
            int bit = extractLSBI(pixelValue);

            // Place the bit in the correct position
            extractedByte |= (byte) (bit << bitIndex);
            startOffset++;
        }

        return extractedByte;
    }

    private boolean shouldFlipLSB(int pixelValue, int messageBit) {
        // Get the two least significant bits
        int lsb = pixelValue & 1;
        int secondLsb = (pixelValue >> 1) & 1;

        // LSBI algorithm: flip LSB if it minimizes the change to the cover image
        return (lsb != messageBit) && (secondLsb == 1);
    }

    private int extractLSBI(int pixelValue) {
        // Get the two least significant bits
        int lsb = pixelValue & 1;
        int secondLsb = (pixelValue >> 1) & 1;

        // LSBI algorithm: if second LSB is 1, invert the LSB
        return (secondLsb == 1) ? lsb ^ 1 : lsb;
    }

    @Override
    public Integer getBitsUsed() {
        return bitsUsed;
    }


}
