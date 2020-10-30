package io.github.alansanchezp.gnomy.account;

import android.content.Intent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ui.account.AddEditAccountActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EditAccountActivityInstrumentedTest {
    private final Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), AddEditAccountActivity.class)
            .putExtra(AddEditAccountActivity.EXTRA_ACCOUNT_ID, 1);
    @Rule
    public ActivityScenarioRule<AddEditAccountActivity> activityRule =
            new ActivityScenarioRule<>(intent);

    @Test
    public void dynamic_title_is_correct() {
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_card_modify)
                )));
    }

    @Test
    public void sets_proper_initial_background_color() {
        // TODO: Implement proper test when we find how to insert to test db
        assert true;
    }

    // TODO: Should we test again same features as in AddAccountActivityInstrumentedTest?
    //  Behavior should be the same except for these two methods and toast message
    //  but it's probably better to make sure through tests. I tried extending the test class
    //  and just overriding these methods but for some reason it triggered exceptions
    //  that do not happen in base class. These exceptions seem to be related to activityScenario
    //  not working properly. Either investigate (and, if possible, fix) them or let this test as it is.
}
