package ar.edu.itba;

import java.io.FileInputStream;

public interface Algorithm {

    public int embed(byte[] message, byte[] output, int offset);

    public byte extract(byte[] inputBytes, int startOffset);

    Integer getBitsUsed();
}
