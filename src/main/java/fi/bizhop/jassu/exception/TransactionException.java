package fi.bizhop.jassu.exception;

import static fi.bizhop.jassu.exception.TransactionException.Type.UNKNOWN;

public class TransactionException extends Exception{
    private final Type TYPE;

    public TransactionException(Type type) {
        super("default");
        if(type == null) type = UNKNOWN;
        this.TYPE = type;
    }

    public TransactionException(Type type, String message) {
        super(message);
        if(type == null) type = UNKNOWN;
        this.TYPE = type;
    }

    public Type getType() {
        return this.TYPE;
    }

    public enum Type {
        LOCK, TIMEOUT, INTERNAL, UNKNOWN
    }
}
