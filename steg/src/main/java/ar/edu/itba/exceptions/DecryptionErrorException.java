package ar.edu.itba.exceptions;

public class DecryptionErrorException extends Exception {

    public DecryptionErrorException(String message) {
        super("Error in decryption: "+message);
    }
}
