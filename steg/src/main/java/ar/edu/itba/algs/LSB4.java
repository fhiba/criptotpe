package ar.edu.itba.algs;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import ar.edu.itba.Algorithm;

public class LSB4 implements Algorithm {
    private Integer bitsUsed = 4;

    @Override
    public int embed(byte[] message, byte[] output, int offset) {
        int currentOffset = offset;
        for (byte b : message) {
            int highNibble = (b >> 4) & 0x0F;
            output[currentOffset] = (byte) ((output[currentOffset] & 0xF0) | highNibble);
            currentOffset++;

            int lowNibble = b & 0x0F;
            output[currentOffset] = (byte) ((output[currentOffset] & 0xF0) | lowNibble);
            currentOffset++;
        }
        return currentOffset;
    }

    private byte extractByte(byte[] inputBytes, int startOffset) {
        if (startOffset + 1 >= inputBytes.length) {
            throw new IllegalStateException("Unexpected end of file while extracting bits");
        }

        int highNibble = inputBytes[startOffset] & 0x0F;
        int lowNibble = inputBytes[startOffset + 1] & 0x0F;

        return (byte) ((highNibble << 4) | lowNibble);
    }

    @Override
    public int extract(byte[] inputBytes, ByteArrayOutputStream contentByte, int offset) {

        byte[] sizeBytes = new byte[MSG_SIZE_BYTE];
        for (int i = 0; i < MSG_SIZE_BYTE; i++) {
            sizeBytes[i] = extractByte(inputBytes, offset);
            offset += Byte.SIZE / bitsUsed;
        }

        int firstSize = ByteBuffer.wrap(sizeBytes).getInt();
        long maxSize = (inputBytes.length - HEADER_SIZE) / 2;
        if (firstSize <= 0 || firstSize > maxSize) {
            System.out.println("firstSize :" + firstSize);
            System.out.println("maxSize :" + maxSize);
            throw new IllegalStateException("Invalid extracted size: " + firstSize);
        }

        for (int i = 0; i < firstSize; i++) {
            contentByte.write(extractByte(inputBytes, offset));
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
