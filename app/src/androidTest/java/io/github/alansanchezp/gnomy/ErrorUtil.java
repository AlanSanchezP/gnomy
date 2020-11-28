package io.github.alansanchezp.gnomy;

import static org.junit.Assert.fail;

public class ErrorUtil {
    public static void assertThrows(Class<? extends Throwable> throwable,
                                    Runnable operation) {
        try {
            operation.run();
            fail();
        } catch (Throwable thr) {
            if (thr.getClass().equals(throwable)) assert true;
            else throw thr;
        }
    }
}
