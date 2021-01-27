package fi.bizhop.jassu.exception;

public class KirvesGameException extends Exception {
    public KirvesGameException() {
        super("default");
    }

    public KirvesGameException(String message) { super(message); }
}
