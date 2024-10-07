package ar.edu.itba.encryptions;

import ar.edu.itba.Encryption;

public class DES3 implements Encryption {
    private String mode;

    public DES3() {
    }

    @Override
    public void encrypt() {
        System.out.println("Encrypting using 3DES");
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
