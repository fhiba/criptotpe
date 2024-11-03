package ar.edu.itba;

public enum EncEnum {
    AES128("aes128", 16),
    AES192("aes192", 16),
    AES256("aes256", 16),
    DES3("3des", 8);

    public String encryption;
    public int BLOCK_SIZE;

    private EncEnum(String encryption, int BLOCK_SIZE) {
        this.encryption = encryption;
        this.BLOCK_SIZE = BLOCK_SIZE;
    }

    public static EncEnum getEncryption(String encryption) {
        for (EncEnum aux : EncEnum.values()) {
            if (aux.encryption.equals(encryption))
                return aux;
        }
        return null;
    }

    public int getKeySize() {
        return switch (this.encryption) {
            case "aes128" -> 128;
            case "aes192" -> 192;
            case "aes256" -> 256;
            case "3des" -> 192;
            default -> 128;
        };
    }

    public String getAlgName() {
        return switch (this.encryption) {
            case "aes128" -> "AES";
            case "aes192" -> "AES";
            case "aes256" -> "AES";
            case "3des" -> "DESede";
            default -> "AES";
        };
    }

    public int getBLOCK_SIZE() {
        return this.BLOCK_SIZE;
    }
}
