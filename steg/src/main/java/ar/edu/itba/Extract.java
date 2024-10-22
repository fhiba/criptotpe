package ar.edu.itba;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.ByteBuffer;

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
        long realSize = Long.valueOf(ByteBuffer.wrap(fullSize).getInt() - 4);
        System.out.println("Real size: " + realSize);
        byte[] msg = new byte[(int) realSize];
        bitCounter = 0;
        byteCounter = 0;
        byte forExtraction;
        long aux = realSize;
        for (; x < width; x++) {
            for (; y < height; y++) {
                forExtraction = (byte) pixelData[x + y];
                alg.extract(forExtraction, msg, byteCounter, bitCounter);

                if (bitCounter + alg.getBitsUsed() >= 8) {
                    byteCounter++;
                    bitCounter = (bitCounter + alg.getBitsUsed()) % 8;
                } else {
                    bitCounter += alg.getBitsUsed();
                }
                aux--;
                if (aux == 0)// se exactamente cuantos bytes hay del mensaje encriptado
                    break;
            }
        }
        System.out.println("took message");
        byte[] decriptedMsg = msg;
        System.out.println(decriptedMsg.length);
        if (enc != null)
            decriptedMsg = enc.decrypt(msg);
        forExtraction = 0;
        aux = realSize;
        boolean eofFlag = false;
        StringBuilder reverseExtension = new StringBuilder();
        while (((char) forExtraction) != '.') {
            System.out.println(aux - 1);
            forExtraction = decriptedMsg[(int) (aux - 1)];
            if (!eofFlag) {
                if ((char) forExtraction == '\0')
                    eofFlag = true;
                else
                    continue;
            } else {
                reverseExtension.append((char) forExtraction);
            }
            aux--;
        }
        System.out.println("jaiba se la mamo con el coso de la extension");
        String extension = reverseExtension.reverse().toString();
        int extensionLength = extension.length();
        aux = 4;
        try (FileWriter outputFile = new FileWriter(outFilePath.concat(extension))) {
            while (aux < realSize - extensionLength) {
                outputFile.append((char) decriptedMsg[(int) aux]);
                aux++;
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

}
