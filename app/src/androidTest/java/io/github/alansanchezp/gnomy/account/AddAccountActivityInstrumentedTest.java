package io.github.alansanchezp.gnomy.account;

import android.content.pm.ActivityInfo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ui.account.AddEditAccountActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasBackground;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AddAccountActivityInstrumentedTest {
    @Rule
    public ActivityScenarioRule<AddEditAccountActivity> activityRule =
            new ActivityScenarioRule<>(AddEditAccountActivity.class);

    @Test
    public void dynamic_title_is_correct() {
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_new)
                )));
    }

    @Test
    public void sets_proper_initial_background_color() {
        onView(withId(R.id.custom_appbar))
                .check(matches(
                        not(hasBackground(android.R.color.transparent))
                ));
    }

    @Test
    public void text_fields_trigger_error_if_empty() {
        onView(withId(R.id.addedit_account_name_input))
                .perform(typeText("test"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_account_name))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_name)
                )));

        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(typeText("40"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_account_initial_value))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_initial_value)
                )));
    }

    @Test
    public void text_fields_do_not_trigger_error_on_rotation_if_pristine() {
        activityRule.getScenario().onActivity(activity -> {
           activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
           activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        onView(withId(R.id.addedit_account_name))
                .check(matches(not(hasDescendant(
                        withText(R.string.account_error_name)
                ))));

        onView(withId(R.id.addedit_account_initial_value))
                .check(matches(not(hasDescendant(
                        withText(R.string.account_error_initial_value)
                ))));
    }


    @Test
    public void text_fields_keep_errors_on_rotation_if_not_pristine() {
        onView(withId(R.id.addedit_account_name_input))
                .perform(replaceText("test"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(replaceText("40"))
                .perform(replaceText(""));

        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        onView(withId(R.id.addedit_account_name))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_name)
                )));

        onView(withId(R.id.addedit_account_initial_value))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_initial_value)
                )));
    }

    @Test
    public void button_opens_color_picker() {
        onView(withId(R.id.addedit_account_color_button))
                .perform(click());

        onView(withText(R.string.account_pick_color_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void prompts_error_if_text_fields_empty_when_form_is_submitted() {
        onView(withId(R.id.addedit_account_FAB))
                .perform(click());

        onView(withId(R.id.addedit_account_name))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_name)
                )));

        onView(withId(R.id.addedit_account_initial_value))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_initial_value)
                )));
    }

    @Test
    public void FAB_finishes_activity_if_data_is_correct() {
        onView(withId(R.id.addedit_account_name_input))
                .perform(replaceText("test"));

        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(replaceText("40"))
                .perform(pressBack());

        onView(withId(R.id.addedit_account_FAB))
                .perform(click());

        try {
            onView(withId(R.id.addedit_account_FAB))
                    .perform(click());
        } catch (RuntimeException re) {
            assert true;
        }
        assert false;
    }

    @Test
    public void not_number_on_initial_value() {
        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(replaceText("ñ"));

        onView(withId(R.id.addedit_account_initial_value_input))
                .check(matches(withText("")));

        try {
            onView(withId(R.id.addedit_account_initial_value_input))
                    .perform(typeText("ñ"));
        } catch (RuntimeException re) {
            assert true;
        }

        assert false;
    }
}
