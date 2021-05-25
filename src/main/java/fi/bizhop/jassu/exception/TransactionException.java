package fi.bizhop.jassu.exception;

import static fi.bizhop.jassu.exception.TransactionException.Type.UNKNOWN;

public class TransactionException extends Exception{
    private Type type;

    public TransactionException(Type type) {
        super("default");
        if(type == null) type = UNKNOWN;
        this.type = type;
    }

    public TransactionException(Type type, String message) {
        super(message);
        if(type == null) type = UNKNOWN;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        LOCK, TIMEOUT, INTERNAL, UNKNOWN
    }
}
