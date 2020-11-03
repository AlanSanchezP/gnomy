package io.github.alansanchezp.gnomy.account;

import android.content.Intent;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.YearMonth;

import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.MockDatabaseOperationsUtil;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.ui.account.AccountBalanceHistoryActivity;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasTextColor;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AccountBalanceHistoryActivityInstrumentedTest {
    private static MonthlyBalance testBalance = new MonthlyBalance();
    private static final String accountTitle = "Test account";
    private static final MutableLiveData<MonthlyBalance> mutableMonthlyBalance = new MutableLiveData<>();
    private static final MutableLiveData<BigDecimal> mutableBigDecimal = new MutableLiveData<>();

    private final Intent intent = new Intent(
            ApplicationProvider.getApplicationContext(), AccountBalanceHistoryActivity.class)
            .putExtra(AccountBalanceHistoryActivity.EXTRA_ACCOUNT_ID, 1)
            .putExtra(AccountBalanceHistoryActivity.EXTRA_NAME, accountTitle);

    @Rule
    public final ActivityScenarioRule<AccountBalanceHistoryActivity> activityRule =
            new ActivityScenarioRule<>(intent);

    @BeforeClass
    public static void init_mocks() {
        // Needed so that ViewModel instance doesn't crash
        final MockDatabaseOperationsUtil.MockableAccountDAO mockAccountDAO = mock(MockDatabaseOperationsUtil.MockableAccountDAO.class);
        final MockDatabaseOperationsUtil.MockableMonthlyBalanceDAO mockBalanceDAO = mock(MockDatabaseOperationsUtil.MockableMonthlyBalanceDAO.class);
        MockDatabaseOperationsUtil.setAccountDAO(mockAccountDAO);
        MockDatabaseOperationsUtil.setBalanceDAO(mockBalanceDAO);

        when(mockBalanceDAO.getAccumulatedFromMonth(anyInt(), any(YearMonth.class)))
                .thenReturn(mutableBigDecimal);
        when(mockBalanceDAO.find(anyInt(), any(YearMonth.class)))
                .thenReturn(mutableMonthlyBalance);
    }

    @Test
    public void title_is_shown() {
        String history_legend = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getString(R.string.account_balance_history_legend);
        String title = accountTitle + " " + history_legend;

        onView(withId(R.id.custom_appbar))
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
        mutableMonthlyBalance.postValue(testBalance);
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
        mutableMonthlyBalance.postValue(testBalance);

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
        mutableMonthlyBalance.postValue(testBalance);

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
        mutableMonthlyBalance.postValue(testBalance);
        onView(withId(R.id.account_history_check_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        mutableMonthlyBalance.postValue(null);
        onView(withId(R.id.account_history_check_btn))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}
