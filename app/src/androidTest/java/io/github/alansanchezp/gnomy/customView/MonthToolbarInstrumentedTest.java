package io.github.alansanchezp.gnomy.customView;

import android.content.Intent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    @Test
    public void buttons_are_hidden_correctly() {
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

        onView(withId(R.id.next_month_btn))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                ));

        onView(withId(R.id.return_to_today_bth))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                ));

        onView(withId(R.id.return_to_today_bth))
                .perform(click());

        onView(withId(R.id.next_month_btn))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)
                ));

        onView(withId(R.id.return_to_today_bth))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));
    }
}
