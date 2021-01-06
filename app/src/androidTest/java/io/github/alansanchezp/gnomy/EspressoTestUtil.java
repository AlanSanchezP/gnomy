package io.github.alansanchezp.gnomy;

import android.view.View;
import android.view.ViewParent;
import android.widget.Checkable;
import android.widget.FrameLayout;

import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Lifecycle.State;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
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

    public static void assertActivityState(State state, ActivityScenarioRule rule) {
        assertActivityState(state, rule.getScenario());
    }

    // TODO: Evaluate if this method is needed, or using only ActivityScenarioRule is enough
    public static void assertActivityState(State state, ActivityScenario scenario) {
        if (scenario.getState() != state) {
            fail("Expected Activity state [" + state + "] but got [" + scenario.getState() + "] instead");
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
                return isAssignableFrom(TextInputLayout.class);
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

    public static ViewAction nestedScrollTo() {
        return new ViewAction() {

            @Override
            public Matcher<View> getConstraints() {
                return Matchers.allOf(
                        isDescendantOfA(isAssignableFrom(NestedScrollView.class)),
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
            }

            @Override
            public String getDescription() {
                return "View is not NestedScrollView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                try {
                    NestedScrollView nestedScrollView = (NestedScrollView)
                            findFirstParentLayoutOfClass(view, NestedScrollView.class);
                    if (nestedScrollView != null) {
                        nestedScrollView.scrollTo(0, view.getTop());
                    } else {
                        throw new Exception("Unable to find NestedScrollView parent.");
                    }
                } catch (Exception e) {
                    throw new PerformException.Builder()
                            .withActionDescription(this.getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(e)
                            .build();
                }
                uiController.loopMainThreadUntilIdle();
            }

        };
    }

    private static View findFirstParentLayoutOfClass(View view, Class<? extends View> parentClass) {
        ViewParent parent = new FrameLayout(view.getContext());
        ViewParent incrementView = null;
        int i = 0;
        while (parent != null && !(parent.getClass() == parentClass)) {
            if (i == 0) {
                parent = findParent(view);
            } else {
                parent = findParent(incrementView);
            }
            incrementView = parent;
            i++;
        }
        return (View) parent;
    }

    private static ViewParent findParent(View view) {
        return view.getParent();
    }

    private static ViewParent findParent(ViewParent view) {
        return view.getParent();
    }
}
