package ar.edu.itba;

import java.util.function.Supplier;

import ar.edu.itba.algs.*;
import ar.edu.itba.encryptions.*;

public class Embed {
    private String filePath;
    private String bitmapFile;
    private String outFile;
    private Algorithm alg;
    private Encryption enc;
    private String mode;
    private String pass;

    public void embed(String[] args) {
        filePath = args[2];
        bitmapFile = args[4];
        outFile = args[6];
        alg = AlgEnum.getAlg(args[8]).get();
        enc = EncEnum.getEnc(args[10]).get();
        mode = args[12];
        if (args[14] != null)
            pass = args[14];
        enc.setMode(mode);

    }

}
