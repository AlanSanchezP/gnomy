package io.github.alansanchezp.gnomy.account;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Locale;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.databinding.FragmentAccountCardBinding;
import io.github.alansanchezp.gnomy.dummy.DummyActivity;
import io.github.alansanchezp.gnomy.ui.account.AccountRecyclerViewAdapter;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccountRecyclerViewHolderInstrumentedTest {
    static final AccountWithAccumulated testAccumulated = mock(AccountWithAccumulated.class);
    static final AccountWithAccumulated testTodayAccumulated = mock(AccountWithAccumulated.class);

    @Rule
    public final ViewScenarioRule viewRule =
            new ViewScenarioRule(R.layout.fragment_account_card);

    @BeforeClass
    public static void init_test_account_and_set_locale() {
        Locale.setDefault(Locale.US);
        testAccumulated.account = mock(Account.class);
        testTodayAccumulated.targetMonth = DateUtil.now();
        testTodayAccumulated.account = testAccumulated.account;
        when(testAccumulated.account.getName())
                .thenReturn("Test account");
        when(testAccumulated.account.getDefaultCurrency())
                .thenReturn("USD");
        when(testAccumulated.getBalanceAtEndOfMonth())
                .thenReturn(BigDecimal.ONE);
        when(testTodayAccumulated.getConfirmedAccumulatedBalanceAtMonth())
                .thenReturn(BigDecimal.TEN);
    }

    @Test
    public void dynamic_balance_labels_are_correct() {
        ActivityScenario<DummyActivity> scenario = viewRule.getScenario();
        AccountRecyclerViewAdapter.ViewHolder[] holder = new AccountRecyclerViewAdapter.ViewHolder[1];
        testAccumulated.targetMonth = DateUtil.now();
        scenario.onActivity(activity -> {
            FragmentAccountCardBinding viewBinding = FragmentAccountCardBinding.inflate(
                    activity.getLayoutInflater(), activity.findViewById(R.id.dummy_activity_root),true);
            holder[0] = new AccountRecyclerViewAdapter.ViewHolder(viewBinding);
            holder[0].setAccountData(testAccumulated, testTodayAccumulated);
        });

        onView(withId(R.id.account_card_current_label))
                .check(matches(withText(R.string.account_current_balance)));

        onView(withId(R.id.account_card_projected_label))
                .check(matches(withText(R.string.account_projected_balance)));

        testAccumulated.targetMonth = DateUtil.now().minusMonths(1);
        scenario.onActivity(activity ->
                holder[0].setAccountData(testAccumulated, testTodayAccumulated));

        onView(withId(R.id.account_card_current_label))
                .check(matches(withText(R.string.account_current_balance)));

        onView(withId(R.id.account_card_projected_label))
                .check(matches(withText(R.string.account_balance_end_of_month)));

        testAccumulated.targetMonth = DateUtil.now().plusMonths(1);
        scenario.onActivity(activity ->
                holder[0].setAccountData(testAccumulated, testTodayAccumulated));

        onView(withId(R.id.account_card_current_label))
                .check(matches(withText(R.string.account_current_balance)));

        onView(withId(R.id.account_card_projected_label))
                .check(matches(withText(R.string.account_projected_balance)));
    }

    @Test
    public void account_information_is_displayed_in_card() {
        ActivityScenario<DummyActivity> scenario = viewRule.getScenario();
        AccountRecyclerViewAdapter.ViewHolder[] holder = new AccountRecyclerViewAdapter.ViewHolder[1];
        testAccumulated.targetMonth = DateUtil.now();
        scenario.onActivity(activity -> {
            FragmentAccountCardBinding viewBinding = FragmentAccountCardBinding.inflate(
                    activity.getLayoutInflater(), activity.findViewById(R.id.dummy_activity_root),true);
            holder[0] = new AccountRecyclerViewAdapter.ViewHolder(viewBinding);
            holder[0].setAccountData(testAccumulated, testTodayAccumulated);
        });

        onView(withId(R.id.account_card_name))
                .check(matches(withText(testAccumulated.account.getName())));

        onView(withId(R.id.account_card_current))
                .check(matches(withText("$10.00")));

        onView(withId(R.id.account_card_projected))
                .check(matches(withText("$1.00")));
    }

    @Test
    public void account_icon_in_card_is_correct() {
        ActivityScenario<DummyActivity> scenario = viewRule.getScenario();
        AccountRecyclerViewAdapter.ViewHolder[] holder = new AccountRecyclerViewAdapter.ViewHolder[1];
        scenario.onActivity(activity -> {
            FragmentAccountCardBinding viewBinding = FragmentAccountCardBinding.inflate(
                    activity.getLayoutInflater(), activity.findViewById(R.id.dummy_activity_root),true);
            holder[0] = new AccountRecyclerViewAdapter.ViewHolder(viewBinding);
        });

        for (int type=Account.BANK; type <= Account.OTHER; type++) {
            when(testAccumulated.account.getType()).thenReturn(type);
            testAccumulated.targetMonth = DateUtil.now();
            scenario.onActivity(activity ->
                    holder[0].setAccountData(testAccumulated, testTodayAccumulated));
            onView(withId(R.id.account_card_icon))
                    .check(matches(
                        withTagValue(
                            equalTo(Account.getDrawableResourceId(type))
                    )));

            // Couldn't find a way to test drawable tint
            // or background drawable color
        }
    }
}
