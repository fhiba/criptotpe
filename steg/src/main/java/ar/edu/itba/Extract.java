package ar.edu.itba;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import ar.edu.itba.exceptions.DecryptionErrorException;
import ar.edu.itba.exceptions.EncryptionErrorException;

public class Extract {
    private String encryptedBitmapFilePath;
    private String bitmapFile;
    private String outFilePath;
    private Algorithm alg;
    private Encryption enc;
    private String pass;

    public void extract(String[] args) {
        encryptedBitmapFilePath = args[2];
        outFilePath = args[4];
        alg = AlgEnum.getAlg(args[6]).get();
        EncModeEnum mode = EncModeEnum.getMode(args[10]);
        if (args[12] != null)
            pass = args[12];
        EncEnum encAlg = EncEnum.getEncryption(args[8]);
        enc = new Encryption(mode, encAlg, pass);
        // enc.setMode(mode);
    }

    public void retrieve() throws IOException, EncryptionErrorException, DecryptionErrorException {
        File bmpFile = new File(encryptedBitmapFilePath);
        File outFile = new File(outFilePath);
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
        int byteCounter = 3;
        int x = 0, y = 0;

        int pixelIndex = 0;
        byte blue = (byte) (pixelData[pixelIndex] & 0xFF); // Blue
        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                blue = (byte) (pixelData[pixelIndex] & 0xFF); // Blue

                alg.extract(blue, fullSize, byteCounter, bitCounter);

                if (bitCounter + 3 * alg.getBitsUsed() >= 8) {
                    byteCounter++;
                    bitCounter = (bitCounter + 3 * alg.getBitsUsed()) % 8;
                } else {
                    bitCounter += 3 * alg.getBitsUsed();
                }
                pixelIndex++;
                if (pixelIndex == 3)// voy adelantando de a 1, se que el tamanio ocupa si o si 4 bytes
                    break;
            }
        }

        long realSize = ByteBuffer.wrap(fullSize).getLong() - 4;
        byte[] msg = new byte[(int) realSize];
        bitCounter = 0;
        byteCounter = 0;
        byte forExtraction;
        long aux = realSize;
        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                forExtraction = (byte) (pixelData[pixelIndex] & 0xFF);
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
        byte[] decriptedMsg = enc.decrypt(msg);
        forExtraction = 0;
        aux = realSize;
        boolean eofFlag = false;
        StringBuilder reverseExtension = new StringBuilder();
        int extensionCounter = 0;
        while (forExtraction != '.') {
            forExtraction = decriptedMsg[(int) aux];
            if (!eofFlag) {
                if (forExtraction == '\0')
                    eofFlag = true;
                else
                    continue;
            } else {
                reverseExtension.append((char) forExtraction);
            }
        }
        String extension = reverseExtension.reverse().toString();

        // TODO: falta poner el msj en un file y appendearle la extension.
    }

}
