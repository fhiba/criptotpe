package ar.edu.itba;

import java.io.FileInputStream;

public interface Algorithm {

    byte[] run(byte blue, byte green, byte red, byte[] message, int messageByteCounter, int messageBitCounter);
}
