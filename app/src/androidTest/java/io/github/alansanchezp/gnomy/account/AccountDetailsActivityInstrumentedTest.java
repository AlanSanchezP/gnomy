package io.github.alansanchezp.gnomy.account;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.YearMonth;

import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.database.category.CategoryRepository;
import io.github.alansanchezp.gnomy.ui.account.AccountDetailsActivity;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

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

import static io.github.alansanchezp.gnomy.database.MockRepositoryBuilder.initMockRepository;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
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
public class AccountDetailsActivityInstrumentedTest {
    private static AccountWithAccumulated testAWA;
    private static final MutableLiveData<AccountWithAccumulated> mutableAWA = new MutableLiveData<>();

    @BeforeClass
    public static void init_mocks() {
        final AccountRepository mockAccountRepository = initMockRepository(AccountRepository.class);
        final CategoryRepository mockCategoryRepository = initMockRepository(CategoryRepository.class);
        testAWA = mock(AccountWithAccumulated.class);
        testAWA.account = mock(Account.class);

        when(mockAccountRepository.getAccumulatedAtMonth(anyInt(), any(YearMonth.class)))
                .thenReturn(mutableAWA);

        // Needed dummy elements so that AccountDetailsActivity, AccountBalanceHistoryActivity
        //  and AddEditAccountActivity don't crash
        testAWA.targetMonth = DateUtil.now();
        when(testAWA.account.getBackgroundColor()).thenReturn(ColorUtil.getRandomColor());
        when(testAWA.account.getId()).thenReturn(1);
        when(testAWA.account.getName()).thenReturn("Test name");
        when(testAWA.account.getDrawableResourceName()).thenReturn("ic_account_balance_bank_black_24dp");
        when(testAWA.account.getTypeNameResourceName()).thenReturn("account_type_bank");
        when(testAWA.account.getCreatedAt()).thenReturn(DateUtil.OffsetDateTimeNow());
        when(testAWA.account.getDefaultCurrency()).thenReturn("USD");
        when(testAWA.getConfirmedExpensesAtMonth()).thenReturn(BigDecimal.ZERO);
        when(testAWA.getConfirmedIncomesAtMonth()).thenReturn(BigDecimal.ZERO);
        when(testAWA.getPendingExpensesAtMonth()).thenReturn(BigDecimal.ZERO);
        when(testAWA.getPendingIncomesAtMonth()).thenReturn(BigDecimal.ZERO);
        when(mockAccountRepository.getAccount(anyInt())).thenReturn(new MutableLiveData<>());
        when(mockAccountRepository.getAll())
                .thenReturn(new MutableLiveData<>());
        when(mockCategoryRepository.getSharedAndCategory(anyInt()))
                .thenReturn(new MutableLiveData<>());
    }

    @Rule
    public final ActivityScenarioRule<AccountDetailsActivity> activityRule =
            new ActivityScenarioRule<>(AccountDetailsActivity.class);

    @Test
    public void menu_items_get_enabled_dynamically() {
        // These asserts will only be true if the test is the first one being executed
        // and we shouldn't force a postValue(null) call as that will force finish
        // the activity
        if (mutableAWA.getValue() == null) {
            onView(withId(R.id.action_archive_account))
                    .check(matches(not(isEnabled())));

            onView(withId(R.id.action_account_actions))
                    .check(matches(not(isEnabled())));
        }

        mutableAWA.postValue(testAWA);

        onView(withId(R.id.action_archive_account))
                .check(matches(isEnabled()));

        onView(withId(R.id.action_account_actions))
                .check(matches(isEnabled()));
    }

    @Test
    public void menu_items_work() {
        mutableAWA.postValue(testAWA);

        onView(withId(R.id.action_archive_account))
                .perform(click());

        onView(withText(R.string.account_card_archive))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        Espresso.pressBack();
        onView(withId(R.id.action_account_actions))
                .perform(click());

        onView(withText(R.string.action_new_expense))
                .perform(click());
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_new_expense)
                )));
        Espresso.pressBack();
        onView(withId(R.id.confirmation_dialog_yes))
                .perform(click());
        onView(withId(R.id.action_account_actions))
                .perform(click());

        onView(withText(R.string.action_new_income))
                .perform(click());
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_new_income)
                )));
        Espresso.pressBack();
        onView(withId(R.id.confirmation_dialog_yes))
                .perform(click());
        onView(withId(R.id.action_account_actions))
                .perform(click());

        onView(withText(R.string.action_new_incoming_transfer))
                .perform(click());
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_new_transfer)
                )));
        Espresso.pressBack();
        onView(withId(R.id.confirmation_dialog_yes))
                .perform(click());
        onView(withId(R.id.action_account_actions))
                .perform(click());

        onView(withText(R.string.action_new_outgoing_transfer))
                .perform(click());
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_new_transfer)
                )));
    }

    @Test
    public void FAB_opens_addedit_activity() {
        mutableAWA.postValue(testAWA);

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

        mutableAWA.postValue(testAWA);

        onView(withId(R.id.account_see_more_button))
                .perform(click());

        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withSubstring(legend_string))
                ));
    }

    @Test
    public void shows_icon_and_label_for_showInDashboard_field() {
        when(testAWA.account.isShowInDashboard()).thenReturn(true);
        mutableAWA.postValue(testAWA);

        onView(withId(R.id.account_included_in_sum_text))
                .check(matches(
                        withText(R.string.account_is_included_in_sum)
                ));

        onView(withId(R.id.account_included_in_sum_icon))
                .check(matches(withTagValue(
                        equalTo(R.drawable.ic_check_black_24dp))
                ));

        when(testAWA.account.isShowInDashboard()).thenReturn(false);
        mutableAWA.postValue(testAWA);

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
