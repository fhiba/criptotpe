package ar.edu.itba;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;

public class Extract {
    private String encryptedBitmapFilePath;
    private String outFilePath;
    private Algorithm alg;
    private Encryption enc;
    private String pass;

    public void extract(String bmpFilePath, String outputFilePath, Algorithm algorithm, Encryption encryption)
            throws Exception {
        this.encryptedBitmapFilePath = bmpFilePath;
        this.outFilePath = outputFilePath;
        this.alg = algorithm;
        this.enc = encryption;
        this.pass = encryption == null ? null : encryption.getPassword();

        retrieve();
    }

    public void extract(String[] args) throws Exception {
        encryptedBitmapFilePath = args[2];
        outFilePath = args[4];
        alg = AlgEnum.getAlg(args[6]).get();
        EncModeEnum mode = EncModeEnum.getMode(args[10]);
        if (args[12] != null)
            pass = args[12];
        EncEnum encAlg = EncEnum.getEncryption(args[8]);
        enc = new Encryption(mode, encAlg, pass);

    }

    public void retrieve() throws Exception {
        BufferedImage image = ImageIO.read(new File(encryptedBitmapFilePath));
        int width = image.getWidth();
        int height = image.getHeight();

    }

}
