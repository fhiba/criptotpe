package ar.edu.itba;

import java.util.function.Supplier;

import ar.edu.itba.algs.*;

public enum AlgEnum {
    LSB1("lsb1", LSB1::new),
    LSB4("lsb4", LSB4::new),
    LSBI("lsbi", LSBI::new);

    String algString;
    Supplier<Algorithm> algorithm;

    AlgEnum(String algString, Supplier<Algorithm> algorithm) {
        this.algString = algString;
        this.algorithm = algorithm;
    }

    public static Supplier<Algorithm> getAlg(String argument) {
        Supplier<Algorithm> out = null;
        for (AlgEnum aux : AlgEnum.values()) {
            if (aux.algString.equals(argument))
                out = aux.algorithm;
        }
        if (out == null)
            System.out.println("No algorithm found for " + argument);
        return out;
    }

}
