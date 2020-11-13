package io.github.alansanchezp.gnomy.util;

public class GnomyCurrencyException extends Exception {

    public GnomyCurrencyException(String message) {
        super(message);
    }

    public GnomyCurrencyException(Exception e) {
        super(e);
    }

    public GnomyCurrencyException(String message, Exception e) {
        super(message, e);
    }
}
