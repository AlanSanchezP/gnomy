package io.github.alansanchezp.gnomy.customView;

import android.content.Intent;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.YearMonth;

import java.util.Locale;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.mock.MockActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MonthToolbarInstrumentedTest {
    private Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
            MockActivity.class)
            .putExtra(
                    MockActivity.EXTRA_LAYOUT_TAG,
                    R.layout.m_activity_month_toolbar);
    @Rule
    public ActivityScenarioRule<MockActivity> activityRule =
            new ActivityScenarioRule<>(intent);

    @BeforeClass
    public static void set_system_locale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Before
    public void system_date_is_set_to_january_2000() {
        // TODO: Find a way to avoid this step as it gets annoying over time
        assertEquals("2000-01", YearMonth.now().toString());
    }

    @Test
    public void uses_month_arrows() {
        onView(ViewMatchers.withId(R.id.month_name_view))
                .check(matches(
                        withText("January")
                ));

        onView(withId(R.id.next_month_btn))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)
                ));

        onView(withId(R.id.prev_month_btn))
                .perform(click())
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("November 1999")
                ));

        onView(withId(R.id.next_month_btn))
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("December 1999")
                ));
    }

    @Test
    public void uses_return_to_today() {
        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("January")
                ));

        onView(withId(R.id.return_to_today_bth))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        onView(withId(R.id.prev_month_btn))
                .perform(click())
                .perform(click());

        onView(withId(R.id.return_to_today_bth))
                .perform(click());

        onView(withId(R.id.month_name_view))
                .check(matches(
                        withText("January")
                ));
    }
}
