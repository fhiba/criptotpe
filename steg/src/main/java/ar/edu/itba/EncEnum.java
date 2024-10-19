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

    public static EncEnum getEncryption(String encryption) {
        for (EncEnum aux : EncEnum.values()) {
            if (encryption.equals(aux.encryption))
                return aux;
        }
        return null;
    }

    public int getKeySize(){
        return switch (this.encryption) {
            case "aes128" -> 128;
            case "aes192" -> 192;
            case "aes256" -> 256;
            case "des3" -> 168;
            default -> 128;
        };
    }

    public String getAlgName(){
        return switch (this.encryption) {
            case "aes128" -> "AES";
            case "aes192" -> "AES";
            case "aes256" -> "AES";
            case "des3" -> "DESede";
            default -> "AES";
        };
    }
}
