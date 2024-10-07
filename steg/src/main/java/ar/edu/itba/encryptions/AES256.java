package ar.edu.itba.encryptions;

import ar.edu.itba.Encryption;

public class AES256 implements Encryption {
    private String mode;

    public AES256() {
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public void encrypt() {
        System.out.println("Encrypting using AES256");
    }

}
