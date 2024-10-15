package ar.edu.itba;

import java.io.FileInputStream;

public interface Algorithm {

    void run(int blue, int green, int red, FileInputStream message, int messageBitCounter);
}
