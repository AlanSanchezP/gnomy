package io.github.alansanchezp.gnomy;

import android.view.View;
import android.widget.Checkable;

import org.hamcrest.BaseMatcher;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.fail;

public class EspressoTestUtil {
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

    public static ViewAction setChecked(final boolean checked) {
        return new ViewAction() {
            @Override
            public BaseMatcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public void describeTo(org.hamcrest.Description description) {
                    }

                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);
            }
        };
    }
}
