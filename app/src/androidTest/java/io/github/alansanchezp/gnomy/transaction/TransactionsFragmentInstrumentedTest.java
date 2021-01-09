package io.github.alansanchezp.gnomy.transaction;

import android.view.MenuItem;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.YearMonth;

import androidx.fragment.app.testing.FragmentScenario;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static io.github.alansanchezp.gnomy.data.MockRepositoryBuilder.initMockRepository;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.data.account.AccountRepository;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.ui.transaction.TransactionsFragment;
import io.github.alansanchezp.gnomy.util.DateUtil;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TransactionsFragmentInstrumentedTest {
    private static MenuItem mockMenuItem;
    // TODO: Not sure about how to test group total sum or that all items are being shown
    //  (pretty sure it is working right now, but already encountered that bug before)

    // TODO: How to test recyclerview click events? Is it even necessary?

    @BeforeClass
    public static void init_mocks() {
        MutableLiveData<YearMonth> mld = new MutableLiveData<>();
        mld.postValue(DateUtil.now());

        final MoneyTransactionRepository mockRepository = initMockRepository(MoneyTransactionRepository.class);
        final AccountRepository mockAccountsRepository = initMockRepository(AccountRepository.class);
        final CategoryRepository mockCategoryRepository = initMockRepository(CategoryRepository.class);

        when(mockRepository.getByFilters(any(MoneyTransactionFilters.class)))
                .thenReturn(new MutableLiveData<>());
        when(mockRepository.getBalanceFromMonth(anyInt(), any(YearMonth.class)))
                .thenReturn(new MutableLiveData<>());
        when(mockAccountsRepository.getAll())
                .thenReturn(new MutableLiveData<>());
        when(mockCategoryRepository.getByStrictCategory(anyInt()))
                .thenReturn(new MutableLiveData<>());
        when(mockCategoryRepository.getSharedAndCategory(anyInt()))
                .thenReturn(new MutableLiveData<>());

        mockMenuItem = mock(MenuItem.class);
    }

    @Test
    public void opens_filters_dialog() {
        FragmentScenario<TransactionsFragment> scenario = launchInContainer(TransactionsFragment.class,
                null, R.style.AppTheme);
        // Asserts filters fragment gets open
        when(mockMenuItem.getItemId()).thenReturn(R.id.action_filter_more);
        scenario.onFragment(f -> f.onOptionsItemSelected(mockMenuItem));

        onView(withId(R.id.filters_dialog_toolbar))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void custom_filters_banner_is_toggled() {
        FragmentScenario<TransactionsFragment> scenario = launchInContainer(TransactionsFragment.class,
                null, R.style.AppTheme);

        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        MoneyTransactionFilters customFilters = new MoneyTransactionFilters();
        customFilters.setMonth(YearMonth.now());
        scenario.onFragment(f -> f.applyFilters(customFilters));
        // Still a simple filter
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        customFilters.setAccountId(2); // Not simple filter anymore
        scenario.onFragment(f -> f.applyFilters(customFilters));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.custom_filter_banner_clear))
                .perform(click()); // Clears filters and banner disappears
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        // Testing that quick filters on menu options reset filters to a "simple" state

        scenario.onFragment(f -> f.applyFilters(customFilters));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        when(mockMenuItem.getItemId()).thenReturn(R.id.action_filter_all);
        scenario.onFragment(f -> f.onOptionsItemSelected(mockMenuItem));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        scenario.onFragment(f -> f.applyFilters(customFilters));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        when(mockMenuItem.getItemId()).thenReturn(R.id.action_filter_incomes);
        scenario.onFragment(f -> f.onOptionsItemSelected(mockMenuItem));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        scenario.onFragment(f -> f.applyFilters(customFilters));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        when(mockMenuItem.getItemId()).thenReturn(R.id.action_filter_expenses);
        scenario.onFragment(f -> f.onOptionsItemSelected(mockMenuItem));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        scenario.onFragment(f -> f.applyFilters(customFilters));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        when(mockMenuItem.getItemId()).thenReturn(R.id.action_filter_transfers);
        scenario.onFragment(f -> f.onOptionsItemSelected(mockMenuItem));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        // Testing that "edit them" button opens dialog
        scenario.onFragment(f -> f.applyFilters(customFilters));
        onView(withId(R.id.custom_filter_banner))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.custom_filter_banner_edit))
                .perform(click());
        onView(withId(R.id.filters_dialog_toolbar))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
