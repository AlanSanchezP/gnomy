package io.github.alansanchezp.gnomy.account;

import android.content.Context;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.data.account.Account;
import io.github.alansanchezp.gnomy.data.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.databinding.LayoutAccountCardBinding;
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
    public final ViewScenarioRule viewRule = new ViewScenarioRule(
            LayoutAccountCardBinding.class);

    @BeforeClass
    public static void init_test_account_and_set_locale() {
        Locale.setDefault(Locale.US);
        testAccumulated.account = new Account(1);
        testTodayAccumulated.targetMonth = DateUtil.now();
        testTodayAccumulated.account = testAccumulated.account;
        testAccumulated.account.setName("Test account");
        testAccumulated.account.setDefaultCurrency("USD");
        when(testAccumulated.getBalanceAtEndOfMonth())
                .thenReturn(BigDecimal.ONE);
        when(testTodayAccumulated.getConfirmedAccumulatedBalanceAtMonth())
                .thenReturn(BigDecimal.TEN);
    }

    @Test
    public void dynamic_balance_labels_are_correct() {
        AccountRecyclerViewAdapter.ViewHolder holder =
                new AccountRecyclerViewAdapter.ViewHolder(Objects.requireNonNull(viewRule.retrieveViewBinding()));
        testAccumulated.targetMonth = DateUtil.now();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
            holder.setAccountData(testAccumulated, testTodayAccumulated));

        onView(withId(R.id.account_card_current_label))
                .check(matches(withText(R.string.account_current_balance)));

        onView(withId(R.id.account_card_projected_label))
                .check(matches(withText(R.string.account_projected_balance)));

        testAccumulated.targetMonth = DateUtil.now().minusMonths(1);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setAccountData(testAccumulated, testTodayAccumulated));

        onView(withId(R.id.account_card_current_label))
                .check(matches(withText(R.string.account_current_balance)));

        onView(withId(R.id.account_card_projected_label))
                .check(matches(withText(R.string.account_balance_end_of_month)));

        testAccumulated.targetMonth = DateUtil.now().plusMonths(1);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setAccountData(testAccumulated, testTodayAccumulated));

        onView(withId(R.id.account_card_current_label))
                .check(matches(withText(R.string.account_current_balance)));

        onView(withId(R.id.account_card_projected_label))
                .check(matches(withText(R.string.account_projected_balance)));
    }

    @Test
    public void account_information_is_displayed_in_card() {
        AccountRecyclerViewAdapter.ViewHolder holder =
                new AccountRecyclerViewAdapter.ViewHolder(Objects.requireNonNull(viewRule.retrieveViewBinding()));
        testAccumulated.targetMonth = DateUtil.now();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setAccountData(testAccumulated, testTodayAccumulated));

        onView(withId(R.id.account_card_name))
                .check(matches(withText(testAccumulated.account.getName())));

        onView(withId(R.id.account_card_current))
                .check(matches(withText("$10.00")));

        onView(withId(R.id.account_card_projected))
                .check(matches(withText("$1.00")));
    }

    @Test
    public void account_icon_in_card_is_correct() {
        AccountRecyclerViewAdapter.ViewHolder holder =
                new AccountRecyclerViewAdapter.ViewHolder(Objects.requireNonNull(viewRule.retrieveViewBinding()));
        testAccumulated.account.setId(1);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        for (int type=Account.BANK; type <= Account.OTHER; type++) {
            testAccumulated.account.setType(type);
            testAccumulated.targetMonth = DateUtil.now();
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    holder.setAccountData(testAccumulated, testTodayAccumulated));
            onView(withId(R.id.account_card_icon))
                    .check(matches(
                        withTagValue(
                            equalTo(context.getResources().getIdentifier(testAccumulated.account.getDrawableResourceName(), "drawable", context.getPackageName()))
                    )));

            // Couldn't find a way to test drawable tint
            // or background drawable color
        }
    }
}
