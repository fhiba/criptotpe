package ar.edu.itba;

import java.io.ByteArrayOutputStream;

public interface Algorithm {
    final static int HEADER_SIZE = 54;
    final static int MSG_SIZE_BYTE = 4;

    public int embed(byte[] message, byte[] output, int offset);

    public int extract(byte[] inputBytes, ByteArrayOutputStream content, int offset);

    Integer getBitsUsed();

    public String extractExtension(byte[] inputBytes, int offset);
}
