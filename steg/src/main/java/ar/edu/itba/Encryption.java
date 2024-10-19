package ar.edu.itba;
import ar.edu.itba.exceptions.DecryptionErrorException;
import ar.edu.itba.exceptions.EncryptionErrorException;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Locale;

public class Encryption {
    private EncModeEnum mode;
    private EncEnum algorithm;
    private String password;

    private static final byte[] SALT = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final byte[] IV = new byte[16];
    private static final String DEFAULT_PADDING = "PKCS5Padding";


    public Encryption(EncModeEnum mode, EncEnum alg, String password) {
        this.password = password;
        this.mode = mode != null ? mode : EncModeEnum.CBC;
        this.algorithm = alg != null ? alg : EncEnum.AES128;
    }

    private SecretKey generateKeyFromPassword(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, 65536, this.algorithm.getKeySize());
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec);
    }

    private String generateTransformationStr(){
        return this.algorithm.getAlgName() + "/" + this.mode.mode.toUpperCase() + "/" + DEFAULT_PADDING;
    }

    private byte[] cryptoOperation(byte[] message, int cypherEncryptMode) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = generateKeyFromPassword(this.password);
        Cipher cipher = Cipher.getInstance(generateTransformationStr());
        System.out.println("Transf string: "+generateTransformationStr());
        cipher.init(cypherEncryptMode, secretKey);
        return cipher.doFinal(message);

    }

    public byte[] encrypt(byte[] message) throws EncryptionErrorException {
        try {
            return cryptoOperation(message, Cipher.ENCRYPT_MODE);
        } catch (Exception e){
            throw new EncryptionErrorException(e.getMessage());
        }

    }

    public byte[] decrypt(byte[] encMessage) throws EncryptionErrorException, DecryptionErrorException {
        try {
            return cryptoOperation(encMessage, Cipher.DECRYPT_MODE);
        } catch (Exception e){
            throw new DecryptionErrorException(e.getMessage());
        }
    }
}
