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

    public void extract(String bmpFilePath, String outputFilePath, Algorithm algorithm, Encryption encryption) {
        this.encryptedBitmapFilePath = bmpFilePath;
        this.outFilePath = outputFilePath;
        this.alg = algorithm;
        this.enc = encryption;
        this.pass = encryption == null ? null : encryption.getPassword();
    }

    public void extract(String[] args) {
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
        File bmpFile = new File(encryptedBitmapFilePath);
        FileInputStream fis = new FileInputStream(bmpFile);
        byte[] header = new byte[54];

        fis.read(header);

        byte[] pixelData = new byte[fis.available()];
        fis.read(pixelData);
        fis.close();

        BufferedImage image = ImageIO.read(bmpFile);

        int width = image.getWidth();
        int height = image.getWidth();

        byte[] fullSize = new byte[4];
        int bitCounter = 0;
        int byteCounter = 0;
        int x = 0, y = 0;

        int pixelIndex = 0;
        byte infoPixel = (byte) pixelData[pixelIndex];
        System.out.println("funco??");
        for (x = 0; x < width; x++) {
            for (y = 0; y < height; x++) {
                infoPixel = (byte) pixelData[x + y];

                alg.extract(infoPixel, fullSize, byteCounter, bitCounter);

                if (bitCounter + alg.getBitsUsed() >= 8) {
                    byteCounter++;
                    bitCounter = (bitCounter + alg.getBitsUsed()) % 8;
                } else {
                    bitCounter += alg.getBitsUsed();
                }

                if (byteCounter == 3)// voy adelantando de a 1, se que el tamanio ocupa si o si 4 bytes
                    break;

            }
            if (byteCounter == 3)
                break;

        }
        long realSize = Long.valueOf(ByteBuffer.wrap(fullSize).order(ByteOrder.BIG_ENDIAN).getInt() - 4);
        System.out.println("Real size: " + realSize);
        byte[] msg = new byte[(int) realSize];
        bitCounter = 0;
        byteCounter = 0;
        byte forExtraction;
        long aux = realSize;
        for (; x < width; x++) {
            for (; y < height; y++) {
                forExtraction = pixelData[x + y];

                alg.extract(forExtraction, msg, byteCounter, bitCounter);

                if (bitCounter + alg.getBitsUsed() >= 8) {
                    byteCounter++;
                    bitCounter = (bitCounter + alg.getBitsUsed()) % 8;
                } else {
                    bitCounter += alg.getBitsUsed();
                }

                aux--;
                if (aux == 0)
                    break;
            }
            if (aux == 0)
                break;
        }
        System.out.println("took message");
        byte[] decriptedMsg = ByteBuffer.wrap(msg).order(ByteOrder.BIG_ENDIAN).array();
        if (enc != null)
            decriptedMsg = enc.decrypt(msg);

        forExtraction = 0;
        aux = 4;// arranco despues de donde estaba el tamanio
        boolean eofFlag = false;
        StringBuilder body = new StringBuilder();

        int lastPunto = 0;
        while (aux < realSize) {
            forExtraction = decriptedMsg[(int) aux];
            if ((char) forExtraction == '.')
                lastPunto = (int) aux;
            body.append(forExtraction);
            aux++;
        }
        String extension = body.substring(lastPunto);
        // hay que ver si hace falta saltar 4 bytes denuevo o no
        String actualBody = body.substring(0, lastPunto);
        System.out.println("primeros bits " + actualBody.substring(0, 5));
        System.out.println("Extracted extension: " + extension);
        int extensionLength = extension.length();
        aux = 4;
        System.out.println(outFilePath);
        try (FileWriter outputFile = new FileWriter(outFilePath.concat(extension))) {
            // outputFile.append(actualBody.toCharArray());
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

}
