package io.github.alansanchezp.gnomy;

import android.view.View;
import android.widget.Checkable;

import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

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

    public static final int START_ICON = 1;
    public static final int END_ICON = 2;
    public static final int ERROR_ICON = 3;

    public static ViewAction clickIcon(final int which) {
        return new ViewAction() {

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(TextInputLayout.class);
            }

            @Override
            public String getDescription() {
                return "Clicks the end or start icon";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextInputLayout item = (TextInputLayout) view;
                // Reach in and find the icon view since we don't have a public API to get a reference to it
                int resId;
                switch (which) {
                    case END_ICON:
                        resId = R.id.text_input_end_icon;
                        break;
                    case ERROR_ICON:
                        resId = R.id.text_input_error_icon;
                        break;
                    case START_ICON:
                    default:
                        resId = R.id.text_input_start_icon;
                }
                CheckableImageButton iconView =
                        item.findViewById(resId);
                iconView.performClick();
            }
        };
    }
}
