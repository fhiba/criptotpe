package ar.edu.itba;

public enum Steg {
    LSB1("lsb1", LSB1Alg::new),
    LSB4("lsb4", LSB4Alg::new),
    LSBI("lsbi", LSBIAlg::new)

}
