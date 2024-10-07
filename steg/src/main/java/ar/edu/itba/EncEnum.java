package ar.edu.itba;

import java.util.function.Supplier;

import ar.edu.itba.encryptions.*;

public enum EncEnum {
    AES128("aes128", AES128::new),
    AES192("aes192", AES192::new),
    AES256("aes256", AES256::new),
    DES3("3des", DES3::new);

    String encString;
    Supplier<Encryption> encryption;

    EncEnum(String encString, Supplier<Encryption> encryption) {
        this.encString = encString;
        this.encryption = encryption;
    }

    public static Supplier<Encryption> getEnc(String argument) {
        Supplier<Encryption> out = null;
        for (EncEnum aux : EncEnum.values()) {
            if (argument.equals(aux.encString))
                out = aux.encryption;
        }
        if (out == null)
            System.out.println("No encryption found for " + argument);
        return out;

    }
}
