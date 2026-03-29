package dev.nez.edge.exception;

@SuppressWarnings("unused")
public class DecodeMessageException extends RuntimeException {

    public DecodeMessageException(String message) {
        super(message);
    }

    public DecodeMessageException(Throwable cause) {
        super(cause);
    }

    public DecodeMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
