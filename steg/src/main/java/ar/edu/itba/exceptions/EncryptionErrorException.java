package ar.edu.itba.exceptions;

public class EncryptionErrorException extends Exception {

    public EncryptionErrorException(String message) {
        super("Error in encryption: "+message);
    }
}
