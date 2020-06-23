package io.github.alansanchezp.gnomy;

import android.graphics.drawable.ColorDrawable;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.YearMonth;

import java.util.Locale;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
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

    @BeforeClass
    public static void set_system_locale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Before
    public void system_date_is_set_to_january_2000() {
        assertEquals("2000-01", YearMonth.now().toString());
    }

    // TODO: Move this test to its own file (as month toolbar should have independent behavior)
    @Test
    public void uses_month_toolbar() {
        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("January")
                ));

        onView(withId(R.id.next_month_btn))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)
                ));

        onView(withId(R.id.return_to_today_bth))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        onView(withId(R.id.prev_month_btn))
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("December 1999")
                ));

        onView(withId(R.id.prev_month_btn))
                .perform(click())
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("October 1999")
                ));

        onView(withId(R.id.next_month_btn))
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("November 1999")
                ));

        onView(withId(R.id.return_to_today_bth))
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("January")
                ));
    }


    @Test
    public void uses_return_to_today() {
        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("January")
                ));

        onView(withId(R.id.next_month_btn))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)
                ));

        onView(withId(R.id.prev_month_btn))
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("December 1999")
                ));

        onView(withId(R.id.next_month_btn))
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("January")
                ));
    }

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
                .findViewById(R.id.toolbar2).getBackground();

        // TODO: Analyze what is the best way to test this
        // Options: - This way is fine
        //          - Retrieve the color using getResources()
        //          - Make BaseMainNavigationFragment.getAppbarColor() public
        assertEquals(0XFF2196F3, appbarBg.getColor());
        assertEquals(0XFF2196F3, toolbarBg.getColor());

        // same doubt here
        onView(withId(R.id.toolbar2))
                .check(matches(isDisplayed()));
    }
}
