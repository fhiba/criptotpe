package ar.edu.itba;

import java.io.FileInputStream;

public interface Algorithm {

    int embed(int color, byte[] message, int messageByteCounter, int messageBitCounter);

    void extract(byte forExtraction, byte[] msg, int byteCounter, int bitCounter);

    Integer getBitsUsed();
}
