package io.github.alansanchezp.gnomy.account;

import android.content.Intent;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.ui.account.AccountHistoryActivity;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasTextColor;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AccountHistoryActivityInstrumentedTest {
    private static MonthlyBalance testBalance;
    private static final String accountTitle = "Test account";

    @BeforeClass
    public static void setup() {
        testBalance = new MonthlyBalance();
    }

    private Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), AccountHistoryActivity.class)
            .putExtra(AccountHistoryActivity.EXTRA_ID, 1)
            .putExtra(AccountHistoryActivity.EXTRA_NAME, accountTitle);

    @Rule
    public ActivityScenarioRule<AccountHistoryActivity> activityRule =
            new ActivityScenarioRule<>(intent);

    @Test
    public void title_is_shown() {
        String history_legend = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.account_balance_history_legend);
        String title = accountTitle + " " + history_legend;

        onView(withId(R.id.toolbar))
                .check(matches(hasDescendant(withText(title))));
    }

    @Test
    public void incomes_and_expenses_are_colored() {
        onView(withId(R.id.account_history_confirmed_incomes))
                .check(matches(
                        hasTextColor(R.color.colorIncomes)
                ));

        onView(withId(R.id.account_history_confirmed_expenses))
                .check(matches(
                        hasTextColor(R.color.colorExpenses)
                ));

        onView(withId(R.id.account_history_pending_incomes))
                .check(matches(
                        hasTextColor(R.color.colorIncomes)
                ));

        onView(withId(R.id.account_history_pending_expenses))
                .check(matches(
                        hasTextColor(R.color.colorExpenses)
                ));
    }

    @Test
    public void total_balances_color_is_dynamic() {
        // Totals are 0
        activityRule.getScenario().onActivity(activity ->
                activity.onBalanceChanged(testBalance));

        onView(withId(R.id.account_history_confirmed_total))
                .check(matches(
                        hasTextColor(R.color.colorText)
                ));

        onView(withId(R.id.account_history_pending_total))
                .check(matches(
                        hasTextColor(R.color.colorText)
                ));

        // Totals are > 0
        testBalance.setTotalIncomes(new BigDecimal("1"));
        testBalance.setProjectedIncomes(new BigDecimal("1"));
        activityRule.getScenario().onActivity(activity ->
                activity.onBalanceChanged(testBalance));

        onView(withId(R.id.account_history_confirmed_total))
                .check(matches(
                        hasTextColor(R.color.colorIncomes)
                ));

        onView(withId(R.id.account_history_pending_total))
                .check(matches(
                        hasTextColor(R.color.colorIncomes)
                ));

        // Totals are < 0
        testBalance.setTotalExpenses(new BigDecimal("2"));
        testBalance.setProjectedExpenses(new BigDecimal("2"));
        activityRule.getScenario().onActivity(activity ->
                activity.onBalanceChanged(testBalance));

        onView(withId(R.id.account_history_confirmed_total))
                .check(matches(
                        hasTextColor(R.color.colorExpenses)
                ));

        onView(withId(R.id.account_history_pending_total))
                .check(matches(
                        hasTextColor(R.color.colorExpenses)
                ));
    }

    @Test
    public void legends_change_on_past_months() {
        // Current month
        String pending_transactions_legend      = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.pending_transactions),
                unresolved_transactions_legend  =  InstrumentationRegistry.getInstrumentation().getTargetContext()
                        .getString(R.string.unresolved_transactions),
                not_included_legend             = InstrumentationRegistry.getInstrumentation().getTargetContext()
                        .getString(R.string.account_balance_not_included_legend);

        onView(withId(R.id.account_history_accumulated_balance_label))
                .check(matches(
                        withText(R.string.account_current_accumulated_balance)
                ));
        onView(withId(R.id.account_history_confirmed_title))
                .check(matches(
                        withText(R.string.account_confirmed_balance)
                ));
        onView(withId(R.id.account_history_pending_title))
                .check(matches(
                        withText(R.string.pending_transactions)
                ));
        onView(withId(R.id.account_history_bottom_legend))
                .check(matches(
                        withText("* " + pending_transactions_legend + " " + not_included_legend)
                ));

        // Past months
        activityRule.getScenario().onActivity(activity ->
                activity.onMonthChanged(DateUtil.now().minusMonths(1)));

        onView(withId(R.id.account_history_accumulated_balance_label))
                .check(matches(
                        withText(R.string.account_accumulated_balance)
                ));
        onView(withId(R.id.account_history_confirmed_title))
                .check(matches(
                        withText(R.string.account_balance_end_of_month)
                ));
        onView(withId(R.id.account_history_pending_title))
                .check(matches(
                        withText(R.string.unresolved_transactions)
                ));
        onView(withId(R.id.account_history_bottom_legend))
                .check(matches(
                        withText("* " + unresolved_transactions_legend + " " + not_included_legend)
                ));
    }

    @Test
    public void check_them_button_is_hidden_if_no_balance() {
        activityRule.getScenario().onActivity(activity ->
                activity.onBalanceChanged(testBalance));

        onView(withId(R.id.account_history_check_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        activityRule.getScenario().onActivity(activity ->
                activity.onBalanceChanged(null));

        onView(withId(R.id.account_history_check_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}
