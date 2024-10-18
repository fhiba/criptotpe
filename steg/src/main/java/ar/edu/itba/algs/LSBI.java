package ar.edu.itba.algs;

import ar.edu.itba.Algorithm;

import java.io.FileInputStream;

public class LSBI implements Algorithm {
    private Integer bitsUsed = 4; //TODO: no estoy seguro cual va aca.
    @Override
    public byte[] run(byte blue, byte green, byte red, byte[] message, int messageByteCounter, int messageBitCounter) {
        return new byte[1];
    }
    
    @Override
    public Integer getBitsUsed(){
        return bitsUsed;
    }
}
