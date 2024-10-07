package ar.edu.itba;

public class Extract {
    private String encryptedBitmapFilePath;
    private String bitmapFile;
    private String outFilePath;
    private Algorithm alg;
    private Encryption enc;
    private String mode;
    private String pass;

    public void extract(String[] args) {
        encryptedBitmapFilePath = args[2];
        outFilePath = args[4];
        alg = AlgEnum.getAlg(args[6]).get();
        enc = EncEnum.getEnc(args[8]).get();
        mode = args[10];
        if (args[12] != null)
            pass = args[12];
        enc.setMode(mode);
    }
}