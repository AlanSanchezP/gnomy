package io.github.alansanchezp.gnomy.database;

public class GnomyIllegalQueryException extends RuntimeException {
    public GnomyIllegalQueryException(String message) {
        super(message);
    }
    public GnomyIllegalQueryException(String message, Exception e) {
        super(message, e);
    }
    public GnomyIllegalQueryException(Exception e) {
        super(e);
    }
}
