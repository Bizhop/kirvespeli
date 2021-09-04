package fi.bizhop.jassu.exception;

public class KirvesGameException extends Exception {
    private final Type TYPE;

    public KirvesGameException() {
        super("default");
        this.TYPE = Type.DEFAULT;
    }

    public KirvesGameException(String message) {
        super(message);
        this.TYPE = Type.DEFAULT;
    }

    public KirvesGameException(String message, Type type) {
        super(message);
        this.TYPE = type;
    }

    public Type getType() {
        return this.TYPE;
    }

    public enum Type {
        DEFAULT, UNAUTHORIZED, BAD_REQUEST;
    }
}
