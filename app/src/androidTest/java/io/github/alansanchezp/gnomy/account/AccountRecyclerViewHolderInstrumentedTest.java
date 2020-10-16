package io.github.alansanchezp.gnomy.account;

import android.content.Intent;
import android.view.View;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Locale;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.mock.MockActivity;
import io.github.alansanchezp.gnomy.ui.account.AccountRecyclerViewAdapter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccountRecyclerViewHolderInstrumentedTest {
    static AccountWithBalance testAccount = new AccountWithBalance();
    Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
            MockActivity.class)
            .putExtra(
                    MockActivity.EXTRA_LAYOUT_TAG,
                    R.layout.fragment_account_card);

    @Rule
    public ActivityScenarioRule<MockActivity> activityRule =
            new ActivityScenarioRule<>(intent);


    @BeforeClass
    public static void init_test_account_and_set_locale() {
        Locale.setDefault(Locale.US);
        testAccount.account = new Account();
        testAccount.account.setName("Test account 1");
        testAccount.account.setType(Account.BANK);
        testAccount.account.setBackgroundColor(0xFF4D7C4F);
        testAccount.account.setInitialValue("0");
        testAccount.accumulatedBalance = new BigDecimal("20");
        testAccount.projectedBalance = new BigDecimal("30");
    }

    @Test
    public void dynamic_balance_labels_are_correct() {
        ActivityScenario<MockActivity> scenario = activityRule.getScenario();
        View view[] = new View[1];
        AccountRecyclerViewAdapter.ViewHolder[] holder = new AccountRecyclerViewAdapter.ViewHolder[1];
        scenario.onActivity(activity -> {
            view[0] = (View) activity.findViewById(R.id.account_card);
            holder[0] = new AccountRecyclerViewAdapter.ViewHolder(view[0]);
            holder[0].setAccountData(testAccount, YearMonth.now());
        });

        onView(withId(R.id.account_card_current_label))
                .check(matches(withText(R.string.account_current_balance)));

        onView(withId(R.id.account_card_projected_label))
                .check(matches(withText(R.string.account_projected_balance)));

        scenario.onActivity(activity -> {
            holder[0].setAccountData(testAccount, YearMonth.now().minusMonths(1));
        });

        onView(withId(R.id.account_card_current_label))
                .check(matches(withText(R.string.account_current_balance)));

        onView(withId(R.id.account_card_projected_label))
                .check(matches(withText(R.string.account_accumulated_balance)));
    }

    @Test
    public void account_information_is_displayed_in_card() {
        ActivityScenario<MockActivity> scenario = activityRule.getScenario();
        View view[] = new View[1];
        AccountRecyclerViewAdapter.ViewHolder[] holder = new AccountRecyclerViewAdapter.ViewHolder[1];
        scenario.onActivity(activity -> {
            view[0] = (View) activity.findViewById(R.id.account_card);
            holder[0] = new AccountRecyclerViewAdapter.ViewHolder(view[0]);
            holder[0].setAccountData(testAccount, YearMonth.now());
        });

        onView(withId(R.id.account_card_name))
                .check(matches(withText(testAccount.account.getName())));

        onView(withId(R.id.account_card_current))
                .check(matches(withText("$20.00")));

        onView(withId(R.id.account_card_projected))
                .check(matches(withText("$30.00")));
    }

    @Test
    public void account_icon_in_card_is_correct() {
        ActivityScenario<MockActivity> scenario = activityRule.getScenario();
        View view[] = new View[1];
        AccountRecyclerViewAdapter.ViewHolder[] holder = new AccountRecyclerViewAdapter.ViewHolder[1];
        scenario.onActivity(activity -> {
            view[0] = (View) activity.findViewById(R.id.account_card);
            holder[0] = new AccountRecyclerViewAdapter.ViewHolder(view[0]);
        });

        for (int type=Account.BANK; type <= Account.OTHER; type++) {
            testAccount.account.setType(type);
            scenario.onActivity(activity -> {
                holder[0].setAccountData(testAccount, YearMonth.now());
            });
            onView(withId(R.id.account_card_icon))
                    .check(matches(
                        withTagValue(
                            equalTo(Account.getDrawableResourceId(type))
                    )));
        }
    }
}
