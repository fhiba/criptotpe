package ar.edu.itba.algs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

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

    private byte extractByte(byte[] inputBytes, int startOffset) {
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

    public int extract(byte[] inputBytes, ByteArrayOutputStream contentBytes, int offset) {

        byte[] sizeBytes = new byte[MSG_SIZE_BYTE];
        for (int i = 0; i < MSG_SIZE_BYTE; i++) {
            sizeBytes[i] = extractByte(inputBytes, offset);
            offset += Byte.SIZE / bitsUsed;
        }

        int firstSize = ByteBuffer.wrap(sizeBytes).getInt();
        long maxSize = (inputBytes.length - HEADER_SIZE) / Byte.SIZE / bitsUsed;
        if (firstSize <= 0 || firstSize > maxSize) {
            throw new IllegalStateException("Invalid extracted size: " + firstSize);
        }

        for (int i = 0; i < firstSize; i++) {
            contentBytes.write(extractByte(inputBytes, offset));
            offset += Byte.SIZE / bitsUsed;
        }
        return offset;
    }

    @Override
    public Integer getBitsUsed() {
        return bitsUsed;
    }

    @Override
    public String extractExtension(byte[] inputBytes, int offset) {
        StringBuilder extension = new StringBuilder();
        while (offset < inputBytes.length) {
            byte b = extractByte(inputBytes, offset);
            offset += Byte.SIZE / bitsUsed;
            if (b == 0) {
                break;
            }
            extension.append((char) b);
        }
        return extension.toString();
    }
}
