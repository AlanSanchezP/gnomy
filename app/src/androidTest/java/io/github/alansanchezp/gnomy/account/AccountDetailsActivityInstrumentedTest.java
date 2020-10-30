package io.github.alansanchezp.gnomy.account;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.account.AccountDetailsActivity;
import io.github.alansanchezp.gnomy.util.ColorUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AccountDetailsActivityInstrumentedTest {
    private static Account testAccount;

    @BeforeClass
    public static void setup() {
        testAccount = new Account();
        testAccount.setId(1);
        testAccount.setBackgroundColor(ColorUtil.getRandomColor());
    }

    @Rule
    public final ActivityScenarioRule<AccountDetailsActivity> activityRule =
            new ActivityScenarioRule<>(AccountDetailsActivity.class);

    @Test
    public void menu_items_get_enabled_dynamically() {
        onView(withId(R.id.action_archive_account))
                .check(matches(not(isEnabled())));

        onView(withId(R.id.action_account_actions))
                .check(matches(not(isEnabled())));

        activityRule.getScenario().onActivity(activity ->
                activity.onAccountChanged(testAccount));

        onView(withId(R.id.action_archive_account))
                .check(matches(isEnabled()));

        onView(withId(R.id.action_account_actions))
                .check(matches(isEnabled()));
    }

    // TODO: Implement other actions when Transactions module is ready
    @Test
    public void archived_menu_item_opens_dialog() {
        activityRule.getScenario().onActivity(activity ->
                activity.onAccountChanged(testAccount));

        onView(withId(R.id.action_archive_account))
                .perform(click());

        try {
            onView(withId(R.id.action_archive_account))
                    .check(matches(not(isEnabled())));

            onView(withId(R.id.account_floating_action_button))
                    .check(matches(not(isEnabled())));

            onView(withId(R.id.account_see_more_button))
                    .check(matches(not(isEnabled())));
        } catch (NoMatchingViewException nmve) {
            assert(true);
        }

        onView(withText(R.string.account_card_archive))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void FAB_opens_addedit_activity() {
        activityRule.getScenario().onActivity(activity ->
                activity.onAccountChanged(testAccount));

        onView(withId(R.id.account_floating_action_button))
                .perform(click());

        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_card_modify))
                ));
    }

    @Test
    public void button_opens_history_activity() {
        String legend_string = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.account_balance_history_legend);

        activityRule.getScenario().onActivity(activity ->
                activity.onAccountChanged(testAccount));

        onView(withId(R.id.account_see_more_button))
                .perform(click());

        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withSubstring(legend_string))
                ));
    }

    @Test
    public void shows_icon_and_label_for_showInDashboard_field() {
        activityRule.getScenario().onActivity(activity ->
                activity.onAccountChanged(testAccount));

        onView(withId(R.id.account_included_in_sum_text))
                .check(matches(
                        withText(R.string.account_is_included_in_sum)
                ));

        onView(withId(R.id.account_included_in_sum_icon))
                .check(matches(withTagValue(
                        equalTo(R.drawable.ic_check_black_24dp))
                ));

        testAccount.setShowInDashboard(false);
        activityRule.getScenario().onActivity(activity ->
                activity.onAccountChanged(testAccount));

        onView(withId(R.id.account_included_in_sum_text))
                .check(matches(
                        withText(R.string.account_is_not_included_in_sum)
                ));

        onView(withId(R.id.account_included_in_sum_icon))
                .check(matches(withTagValue(
                        equalTo(R.drawable.ic_close_black_24dp))
                ));
    }
}
