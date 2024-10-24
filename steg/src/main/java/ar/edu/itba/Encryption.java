package ar.edu.itba;

import ar.edu.itba.exceptions.DecryptionErrorException;
import ar.edu.itba.exceptions.EncryptionErrorException;
import ar.edu.itba.utils.KeyIvPair;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Locale;

public class Encryption {
    private EncModeEnum mode;
    private EncEnum algorithm;
    private String password;

    private static final byte[] SALT = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    private static final byte[] IV = new byte[16];
    private static final String DEFAULT_PADDING = "PKCS5Padding";

    public Encryption(EncModeEnum mode, EncEnum alg, String password) {
        this.password = password;
        this.mode = mode != null ? mode : EncModeEnum.CBC;
        this.algorithm = alg != null ? alg : EncEnum.AES128;
    }

    private KeyIvPair generateKeyAndIvFromPassword(String password)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, 65536, this.algorithm.getKeySize() + 128); // Add
                                                                                                                  // 128
                                                                                                                  // bits
                                                                                                                  // (16
                                                                                                                  // bytes)
                                                                                                                  // for
                                                                                                                  // the
                                                                                                                  // IV
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyAndIv = factory.generateSecret(spec).getEncoded();

        byte[] keyBytes = new byte[this.algorithm.getKeySize() / 8];
        byte[] ivBytes = new byte[16];

        System.arraycopy(keyAndIv, 0, keyBytes, 0, keyBytes.length);
        System.arraycopy(keyAndIv, keyBytes.length, ivBytes, 0, ivBytes.length);

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, this.algorithm.getAlgName());
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        return new KeyIvPair(secretKey, ivSpec);
    }

    private String generateTransformationStr() {
        return this.algorithm.getAlgName() + "/" + this.mode.mode.toUpperCase() + "/" + DEFAULT_PADDING;
    }

    private byte[] cryptoOperation(byte[] message, int cypherEncryptMode)
            throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        KeyIvPair keyIvPair = generateKeyAndIvFromPassword(this.password);
        Cipher cipher = Cipher.getInstance(generateTransformationStr());
        if (this.mode == EncModeEnum.ECB) {
            cipher.init(cypherEncryptMode, keyIvPair.getSecretKey());
        } else {
            cipher.init(cypherEncryptMode, keyIvPair.getSecretKey(), keyIvPair.getIv());
        }

        return cipher.doFinal(message);
    }

    public byte[] encrypt(byte[] message) throws EncryptionErrorException {
        try {
            return cryptoOperation(message, Cipher.ENCRYPT_MODE);
        } catch (Exception e) {
            throw new EncryptionErrorException(e.getMessage());
        }

    }

    public byte[] decrypt(byte[] encMessage) throws EncryptionErrorException, DecryptionErrorException {
        try {
            return cryptoOperation(encMessage, Cipher.DECRYPT_MODE);
        } catch (Exception e) {
            throw new DecryptionErrorException(e.getMessage());
        }
    }

    public EncModeEnum getMode() {
        return mode;
    }

    public EncEnum getAlgorithm() {
        return algorithm;
    }

    public String getPassword() {
        return password;
    }

    public static byte[] getSalt() {
        return SALT;
    }

    public static byte[] getIv() {
        return IV;
    }

    public static String getDefaultPadding() {
        return DEFAULT_PADDING;
    }
}
