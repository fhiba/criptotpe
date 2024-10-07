package ar.edu.itba.encryptions;

import ar.edu.itba.Encryption;

public class AES128 implements Encryption {
    private String mode;

    public AES128() {

    }

    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public void encrypt() {
        System.out.println("Encrypting using AES128");
    }

}
