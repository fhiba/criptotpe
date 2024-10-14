package ar.edu.itba;

public enum EncEnum {
    AES128("aes128"),
    AES192("aes192"),
    AES256("aes256"),
    DES3("3des");

    public String encryption;

    private EncEnum(String encryption) {
        this.encryption = encryption;
    }
}
