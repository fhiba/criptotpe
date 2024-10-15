package ar.edu.itba;

public class Encryption {
    private String password;
    private EncModeEnum mode;
    private EncEnum algorithm;

    public Encryption(String password, EncModeEnum mode, EncEnum alg) {
        this.password = password;
        this.mode = mode != null ? mode : EncModeEnum.CBC;
        this.algorithm = alg != null ? alg : EncEnum.AES128;
    }

}
