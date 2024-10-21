package ar.edu.itba;

import java.io.FileInputStream;

public interface Algorithm {

    byte[] run(byte blue, byte green, byte red, byte[] message, int messageByteCounter, int messageBitCounter);

    void extract(byte forExtraction, byte[] msg, int byteCounter, int bitCounter);

    Integer getBitsUsed();
}
