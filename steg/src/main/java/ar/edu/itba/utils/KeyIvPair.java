package ar.edu.itba.utils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeyIvPair {
    private SecretKey secretKey;
    private IvParameterSpec iv;

    public KeyIvPair(SecretKey secretKey, IvParameterSpec iv) {
        this.secretKey = secretKey;
        this.iv = iv;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public IvParameterSpec getIv() {
        return iv;
    }
}
