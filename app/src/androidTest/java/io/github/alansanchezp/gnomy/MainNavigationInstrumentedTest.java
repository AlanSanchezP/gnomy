package io.github.alansanchezp.gnomy;

import android.graphics.drawable.ColorDrawable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainNavigationInstrumentedTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void switches_to_accounts() {
        onView(withId(R.id.navigation_accounts))
                .perform(click());

        onView(withId(R.id.toolbar))
                .check(matches(hasDescendant(
                        withText(R.string.title_accounts)
                )));

        onView(withId(R.id.total_balance_lable))
                .check(matches(isDisplayed()));

        ColorDrawable appbarBg = (ColorDrawable) activityRule.getActivity()
                .findViewById(R.id.toolbar).getBackground();
        ColorDrawable toolbarBg = (ColorDrawable) activityRule.getActivity()
                .findViewById(R.id.monthtoolbar).getBackground();

        // TODO: Analyze what is the best way to test this
        // Options: - This way is fine
        //          - Retrieve the color using getResources()
        //          - Make BaseMainNavigationFragment.getAppbarColor() public
        assertEquals(0XFF2196F3, appbarBg.getColor());
        assertEquals(0XFF2196F3, toolbarBg.getColor());

        // same doubt here
        onView(withId(R.id.monthtoolbar))
                .check(matches(isDisplayed()));

        onView(withId(R.id.main_floating_action_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.toolbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_new))
                ));
    }
}
