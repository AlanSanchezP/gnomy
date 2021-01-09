package io.github.alansanchezp.gnomy.account;

import android.view.MenuItem;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.testing.FragmentScenario;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;

import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.category.CategoryRepository;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.database.MockRepositoryBuilder.initMockRepository;
import static io.github.alansanchezp.gnomy.util.DateUtil.OffsetDateTimeNow;
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
public class AccountsFragmentInstrumentedTest {
    private static MenuItem mockMenuItem;
    // Needed so that ViewModel instance doesn't crash
    @BeforeClass
    public static void init_mocks() {
        MutableLiveData<YearMonth> mld = new MutableLiveData<>();
        mld.postValue(DateUtil.now());

        final AccountRepository mockAccountRepository = initMockRepository(AccountRepository.class);
        final MoneyTransactionRepository mockTransactionRepository = initMockRepository(MoneyTransactionRepository.class);
        final CategoryRepository mockCategoryRepository = initMockRepository(CategoryRepository.class);

        when(mockCategoryRepository.getSharedAndCategory(anyInt()))
                .thenReturn(new MutableLiveData<>());
        when(mockAccountRepository.getArchivedAccounts())
                .thenReturn(new MutableLiveData<>());
        when(mockAccountRepository.getAll())
                .thenReturn(new MutableLiveData<>());
        when(mockAccountRepository.getAccount(anyInt()))
                .thenReturn(new MutableLiveData<>());

        when(mockAccountRepository.getTodayAccumulatesList())
                .thenReturn(new MutableLiveData<>());
        when(mockAccountRepository.getAccumulatesListAtMonth(any(YearMonth.class)))
                .thenReturn(new MutableLiveData<>());
        when(mockAccountRepository.getAccumulatedAtMonth(anyInt(), any(YearMonth.class)))
                .thenReturn(new MutableLiveData<>());
        List<TransactionDisplayData> testTransactionsList = new ArrayList<>();
        TransactionDisplayData testItem = new TransactionDisplayData();
        testItem.transaction = new MoneyTransaction();
        testItem.transaction.setDate(OffsetDateTimeNow());
        testItem.categoryResourceName = "ic_add_black_24dp";
        testItem.accountName = "test";
        testTransactionsList.add(testItem);
        when(mockTransactionRepository.getByFilters(any(MoneyTransactionFilters.class)))
                .thenReturn(new MutableLiveData<>(testTransactionsList));
        when(mockTransactionRepository.find(anyInt()))
                .thenReturn(new MutableLiveData<>(testItem.transaction));

        mockMenuItem = mock(MenuItem.class);
    }

    @Test
    public void dynamic_balance_labels_are_correct() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);

        scenario.onFragment(fragment ->
                fragment.onMonthChanged(DateUtil.now()));

        onView(withId(R.id.total_projected_label))
                .check(matches(
                        withText(R.string.account_projected_balance)
                ));

        scenario.onFragment(fragment ->
                fragment.onMonthChanged(DateUtil.now().minusMonths(1)));

        onView(ViewMatchers.withId(R.id.total_projected_label))
                .check(matches(
                        withText(R.string.account_balance_end_of_month)
                ));

        scenario.onFragment(fragment ->
                fragment.onMonthChanged(DateUtil.now().plusMonths(1)));

        onView(ViewMatchers.withId(R.id.total_projected_label))
                .check(matches(
                        withText(R.string.account_projected_balance)
                ));
    }

    @Test
    public void archived_accounts_modal_is_shown() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        onView(withId(R.id.archived_items_container))
                .check(
                        doesNotExist()
                );
        when(mockMenuItem.getItemId()).thenReturn(R.id.action_show_archived);
        scenario.onFragment(fragment -> fragment.onOptionsItemSelected(mockMenuItem));
        onView(withId(R.id.archived_items_container))
                .check(matches(
                        isDisplayed()
                ));
    }

    @Test
    public void opens_new_account_activity() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        scenario.onFragment(fragment ->
                fragment.onFABClick(null));
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_new))
                ));
    }

    @Test
    public void interface_click_method_opens_account_details() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        scenario.onFragment(fragment -> {
            Account account = new Account();
            account.setId(1);
            fragment.onItemInteraction(account);
        });
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_details))
                ));
    }

    @Test
    public void interface_menu_method_opens_account_details() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        when(mockMenuItem.getItemId()).thenReturn(R.id.account_card_details);
        scenario.onFragment(fragment -> {
            Account account = new Account();
            account.setId(1);
            fragment.onItemMenuItemInteraction(account, mockMenuItem);
        });
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_details))
                ));
    }

    @Test
    public void interface_menu_method_opens_account_modify() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        when(mockMenuItem.getItemId()).thenReturn(R.id.account_card_modify);
        scenario.onFragment(fragment -> {
            Account account = new Account();
            account.setId(1);
            fragment.onItemMenuItemInteraction(account, mockMenuItem);
        });
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_card_modify))
                ));
    }

    @Test
    public void interface_menu_method_opens_account_transactions() {
        // TODO: Not sure if this can be tested here. Try to do it anyways.
        assert(true);
    }

    @Test
    public void interface_menu_method_opens_archive_dialog() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        when(mockMenuItem.getItemId()).thenReturn(R.id.account_card_archive);
        scenario.onFragment(fragment ->
                fragment.onItemMenuItemInteraction(new Account(), mockMenuItem));
        onView(withText(R.string.account_card_archive))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.account_card_archive_info))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void interface_menu_method_opens_delete_dialog() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        when(mockMenuItem.getItemId()).thenReturn(R.id.account_card_delete);
        scenario.onFragment(fragment ->
                fragment.onItemMenuItemInteraction(new Account(), mockMenuItem));
        onView(withText(R.string.account_card_delete))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.account_card_delete_warning))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void interface_method_opens_unresolved_transactions() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        scenario.onFragment(fragment ->
                fragment.onUnresolvedTransactions(new Account(1)));
        onView(withText(R.string.unresolved_transactions))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void interface_method_opens_unresolved_transaction_item() {
        MoneyTransaction testItem = new MoneyTransaction();
        testItem.setId(1);
        testItem.setType(MoneyTransaction.EXPENSE);
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme);
        scenario.onFragment(fragment ->
                fragment.onUnresolvedTransactionClick(testItem));

        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_modify_expense))
                ));
    }
}
