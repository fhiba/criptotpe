package ar.edu.itba;

import java.io.FileInputStream;

public interface Algorithm {

    public int embed(byte[] message, byte[] output, int offset);

    void extract(byte forExtraction, byte[] msg, int byteCounter, int bitCounter);

    Integer getBitsUsed();
}
