package io.github.alansanchezp.gnomy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.dummy.BackButtonDummyActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class BackButtonActivityInstrumentedTest {
    @Rule
    public final ActivityScenarioRule<BackButtonDummyActivity> activityRule =
            new ActivityScenarioRule<>(BackButtonDummyActivity.class);

    @Test
    public void displays_dialog_on_back_press() {
        onView(isRoot())
                .perform(pressBack());

        onView(withText(R.string.confirmation_dialog_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void enables_actions_only_if_no_pending_operations() {
        activityRule.getScenario().onActivity(activity ->
                activity.simulatePendingOperations(false));
        onView(isRoot())
                .perform(pressBack());
        onView(withText(R.string.confirmation_dialog_no))
                .inRoot(isDialog())
                .perform(click());

        boolean[] actionsEnabled = new boolean[1];
        activityRule.getScenario().onActivity(activity ->
                actionsEnabled[0] = activity.actionsEnabled);
        assertTrue(actionsEnabled[0]);

        activityRule.getScenario().onActivity(activity ->
                activity.simulatePendingOperations(true));
        onView(isRoot())
                .perform(pressBack());
        onView(withText(R.string.confirmation_dialog_no))
                .inRoot(isDialog())
                .perform(click());

        activityRule.getScenario().onActivity(activity ->
                actionsEnabled[0] = activity.actionsEnabled);
        assertFalse(actionsEnabled[0]);
    }
}
