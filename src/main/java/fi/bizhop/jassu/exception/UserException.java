package fi.bizhop.jassu.exception;

public class UserException extends Exception {
    public UserException() {
        super("default");
    }

    public UserException(String message) {
        super(message);
    }
}
