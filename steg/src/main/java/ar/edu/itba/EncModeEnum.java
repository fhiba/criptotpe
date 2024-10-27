package ar.edu.itba;

public enum EncModeEnum {
    CBC("cbc"),
    CFB("cfb"),
    OFB("ofb"),
    ECB("ecb");

    public String mode;

    private EncModeEnum(String mode) {
        this.mode = mode;
    }

    public static EncModeEnum getMode(String mode) {
        for (EncModeEnum aux : EncModeEnum.values()) {
            if (aux.mode.equals(mode))
                return aux;
        }
        return null;
    }
}
