package dev.nez.edge.exception;

@SuppressWarnings("unused")
public class MessageParseException extends RuntimeException {

    public MessageParseException(String message) {
        super(message);
    }

    public MessageParseException(Throwable cause) {
        super(cause);
    }

    public MessageParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
