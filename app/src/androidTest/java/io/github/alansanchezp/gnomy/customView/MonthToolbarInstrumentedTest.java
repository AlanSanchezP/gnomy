package io.github.alansanchezp.gnomy.customView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.ui.customView.MonthToolbarView;
import io.github.alansanchezp.gnomy.viewmodel.customView.MonthToolbarViewModel;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MonthToolbarInstrumentedTest {
    @Rule
    public final ViewScenarioRule viewRule =
            new ViewScenarioRule(R.layout.d_activity_month_toolbar);

    @Test
    public void buttons_are_hidden_correctly() {
        viewRule.getScenario().onActivity(activity -> ((MonthToolbarView) activity.findViewById(R.id.monthtoolbar))
                .setViewModel(new ViewModelProvider(
                        activity,
                        new SavedStateViewModelFactory(
                                activity.getApplication(),
                                activity
                        )).get(MonthToolbarViewModel.class)));

        onView(withId(R.id.return_to_today_bth))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        onView(withId(R.id.prev_month_btn))
                .perform(click());

        onView(withId(R.id.return_to_today_bth))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                ));

        onView(withId(R.id.return_to_today_bth))
                .perform(click());

        onView(withId(R.id.return_to_today_bth))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));
    }
}
