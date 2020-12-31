package io.github.alansanchezp.gnomy.transaction;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.testing.FragmentScenario;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.END_ICON;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.clickIcon;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.setChecked;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.ui.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.ui.transaction.TransactionFiltersDialogFragment;
import io.github.alansanchezp.gnomy.ui.transaction.TransactionFiltersDialogFragment.TransactionFiltersDialogInterface;
import io.github.alansanchezp.gnomy.util.DateUtil;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TransactionFiltersDialogFragmentInstrumentedTest {
    // TODO: Include destination account filter when transfers are selected
    private static final GnomyFragmentFactory factory = new GnomyFragmentFactory();
    private static final TransactionFiltersDialogInterface mockInterface =
            mock(TransactionFiltersDialogInterface.class);

    @BeforeClass
    public static void init() {
        factory.addMapElement(TransactionFiltersDialogFragment.class, mockInterface);

        MutableLiveData<List<Account>> testAccountsListLD = new MutableLiveData<>();
        MutableLiveData<List<Category>> testCategoriesList = new MutableLiveData<>();

        when(mockInterface.getAccountsLiveData()).thenReturn(testAccountsListLD);
        when(mockInterface.getCategoriesLiveData()).thenReturn(testCategoriesList);

        List<Account> accountsList = new ArrayList<>();
        Account acc = new Account(2);
        acc.setName("ACCOUNT A");
        accountsList.add(acc);
        acc = new Account(4);
        acc.setName("ACCOUNT B");
        accountsList.add(acc);

        List<Category> categoriesList = new ArrayList<>();
        Category cat = new Category();
        cat.setId(1);
        cat.setName("CATEGORY A");
        cat.setIconResName("ic_calculate_24");
        categoriesList.add(cat);
        cat = new Category();
        cat.setId(2);
        cat.setName("CATEGORY B");
        cat.setIconResName("ic_baseline_notes_24");
        categoriesList.add(cat);

        testAccountsListLD.postValue(accountsList);
        testCategoriesList.postValue(categoriesList);
    }

    @Test
    public void shows_initial_data() {
        MoneyTransactionFilters initialFilters = new MoneyTransactionFilters();
        OffsetDateTime startDate = DateUtil.OffsetDateTimeNow().minusMonths(3);
        OffsetDateTime endDate = DateUtil.OffsetDateTimeNow();

        when(mockInterface.getInitialFilters()).thenReturn(initialFilters);

        initialFilters.setTransactionType(MoneyTransaction.EXPENSE);
        initialFilters.setAccountId(4);
        initialFilters.setCategoryId(2);
        initialFilters.setSortingMethod(MoneyTransactionFilters.LEAST_RECENT);
        initialFilters.setTransactionStatus(MoneyTransactionFilters.CONFIRMED_STATUS);
        initialFilters.setStartDate(startDate);
        initialFilters.setEndDate(endDate);
        initialFilters.setMinAmount("10");
        initialFilters.setMaxAmount("30");

        // JUST NOW WE START THE ACTUAL TEST

        FragmentScenario<TransactionFiltersDialogFragment> scenario = launchInContainer(TransactionFiltersDialogFragment.class,
                       null, R.style.AppTheme, factory);
        onView(withId(R.id.filters_dialog_type_spinner))
                .check(matches(
                        withSpinnerText(R.string.action_filter_expenses)
                ));
        onView(withId(R.id.filters_dialog_category_spinner))
                .check(matches(
                        hasDescendant(withText("CATEGORY B"))
                ));
        onView(withId(R.id.filters_dialog_account_spinner))
                .check(matches(
                        hasDescendant(withText("ACCOUNT B"))
                ));
        onView(withId(R.id.filters_dialog_sorting_spinner))
                .check(matches(
                        hasDescendant(withText(R.string.transaction_filters_sort_least_recent))
                ));
        onView(withId(R.id.filters_dialog_status_radio_confirmed))
                .check(matches(
                        isChecked()
                ));
        onView(withId(R.id.filters_dialog_period_switch))
                .check(matches(
                        isChecked()
                ));
        onView(withId(R.id.filters_dialog_period_from_input))
                .check(matches(
                        withText(DateUtil.getOffsetDateTimeString(startDate, true))
                ));
        onView(withId(R.id.filters_dialog_period_to_input))
                .check(matches(
                        withText(DateUtil.getOffsetDateTimeString(endDate, true))
                ));
        onView(withId(R.id.filters_dialog_amount_switch))
                .check(matches(
                        isChecked()
                ));
        onView(withId(R.id.filters_dialog_amount_min_input))
                .check(matches(
                        withText("10")
                ));
        onView(withId(R.id.filters_dialog_amount_max_input))
                .check(matches(
                        withText("30")
                ));
    }

    @Test
    public void dynamic_toggle_of_date_and_amount_filters() {
        when(mockInterface.getInitialFilters()).thenReturn(new MoneyTransactionFilters());
        FragmentScenario<TransactionFiltersDialogFragment> scenario = launchInContainer(TransactionFiltersDialogFragment.class,
                null, R.style.AppTheme, factory);
        // Default value has both switches off
        onView(withId(R.id.filters_dialog_period_switch))
                .check(matches(
                        not(isChecked())
                ));
        onView(withId(R.id.filters_dialog_period_from))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));
        onView(withId(R.id.filters_dialog_period_to))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));
        onView(withId(R.id.filters_dialog_amount_switch))
                .check(matches(
                        not(isChecked())
                ));
        onView(withId(R.id.filters_dialog_amount_group))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        onView(withId(R.id.filters_dialog_period_switch))
                .perform(setChecked(true));
        onView(withId(R.id.filters_dialog_period_from))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                ));
        onView(withId(R.id.filters_dialog_period_to))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                ));
        onView(withId(R.id.filters_dialog_amount_switch))
                .perform(setChecked(true));
        onView(withId(R.id.filters_dialog_amount_group))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                ));


        onView(withId(R.id.filters_dialog_period_switch))
                .perform(setChecked(false));
        onView(withId(R.id.filters_dialog_period_from))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));
        onView(withId(R.id.filters_dialog_period_to))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));
        onView(withId(R.id.filters_dialog_amount_switch))
                .perform(setChecked(false));
        onView(withId(R.id.filters_dialog_amount_group))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));
    }

    @Test
    public void amount_range_errors() {
        when(mockInterface.getInitialFilters()).thenReturn(new MoneyTransactionFilters());
        FragmentScenario<TransactionFiltersDialogFragment> scenario = launchInContainer(TransactionFiltersDialogFragment.class,
                null, R.style.AppTheme, factory);

        onView(withId(R.id.filters_dialog_amount_switch))
                .perform(setChecked(true));

        assertThrows(RuntimeException.class,
                () -> onView(withId(R.id.filters_dialog_amount_min_input))
                        .perform(typeText("ñ")));

        onView(withId(R.id.filters_dialog_amount_min_input))
                .perform(typeText("30"))
                .perform(closeSoftKeyboard());

        assertThrows(RuntimeException.class,
                () -> onView(withId(R.id.filters_dialog_amount_max_input))
                        .perform(typeText("ñ")));

        onView(withId(R.id.filters_dialog_amount_max_input))
                .perform(typeText("30"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.filters_dialog_amount_max))
                .check(matches(
                        hasDescendant(withText(R.string.transaction_filters_max_amount_error))
                ));
        onView(withId(R.id.filters_dialog_apply_btn))
                .check(matches(
                        not(isEnabled())
                ));
        onView(withId(R.id.filters_dialog_amount_max_input))
                .perform(replaceText("40"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.filters_dialog_amount_max))
                .check(matches(
                        not(hasDescendant(withText(R.string.transaction_filters_max_amount_error)))
                ));
        onView(withId(R.id.filters_dialog_apply_btn))
                .check(matches(
                        isEnabled()
                ));
        onView(withId(R.id.filters_dialog_amount_min_input))
                .perform(replaceText("40"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.filters_dialog_amount_min))
                .check(matches(
                        hasDescendant(withText(R.string.transaction_filters_min_amount_error))
                ));
        onView(withId(R.id.filters_dialog_apply_btn))
                .check(matches(
                        not(isEnabled())
                ));
        onView(withId(R.id.filters_dialog_amount_min_input))
                .perform(replaceText("20"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.filters_dialog_amount_min))
                .check(matches(
                        not(hasDescendant(withText(R.string.transaction_filters_min_amount_error)))
                ));
        onView(withId(R.id.filters_dialog_apply_btn))
                .check(matches(
                        isEnabled()
                ));
        onView(withId(R.id.filters_dialog_amount_min_input))
                .perform(replaceText("50"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.filters_dialog_amount_min))
                .check(matches(
                        hasDescendant(withText(R.string.transaction_filters_min_amount_error))
                ));
        onView(withId(R.id.filters_dialog_amount_max_input))
                .perform(replaceText(""))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.filters_dialog_amount_min))
                .check(matches(
                        not(hasDescendant(withText(R.string.transaction_filters_min_amount_error)))
                ));
        onView(withId(R.id.filters_dialog_apply_btn))
                .check(matches(
                        isEnabled()
                ));
        onView(withId(R.id.filters_dialog_amount_max_input))
                .perform(replaceText("50"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.filters_dialog_apply_btn))
                .check(matches(
                        not(isEnabled())
                ));

        onView(withId(R.id.filters_dialog_amount_switch))
                .perform(setChecked(false));
        // Espresso is activating account spinner for some very weird reason X.X
        // TODO: WHAT IS GOING ON HERE?
        boolean keepTying = true;
        while(keepTying) {
            try {
                onView(withId(R.id.filters_dialog_apply_btn))
                        .check(matches(
                                isEnabled()
                        ));
                keepTying = false;
            } catch (NoMatchingViewException e) {
                Espresso.pressBack();
            }
        }
    }
    @Test
    public void opens_datetime_pickers() {
        when(mockInterface.getInitialFilters()).thenReturn(new MoneyTransactionFilters());
        FragmentScenario<TransactionFiltersDialogFragment> scenario = launchInContainer(TransactionFiltersDialogFragment.class,
                null, R.style.AppTheme, factory);

        onView(withId(R.id.filters_dialog_period_switch))
                .perform(setChecked(true));
        onView(withId(R.id.filters_dialog_period_from))
                .perform(clickIcon(END_ICON));
        // Using resource ids from library
        onView(withId(R.id.mdtp_animator))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withId(R.id.mdtp_ok))
                .inRoot(isDialog())
                .perform(click());
        onView(withId(R.id.mdtp_time_picker))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.mdtp_ok))
                .inRoot(isDialog())
                .perform(click());
        onView(withId(R.id.filters_dialog_period_to))
                .perform(clickIcon(END_ICON));
        // Using resource ids from library
        onView(withId(R.id.mdtp_animator))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withId(R.id.mdtp_ok))
                .inRoot(isDialog())
                .perform(click());
        onView(withId(R.id.mdtp_time_picker))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withId(R.id.mdtp_ok))
                .inRoot(isDialog())
                .perform(click());

        // Same date should display an error. Cannot test other date error scenarios
        //  without digging too much into date and time pickers internal resources
        onView(withId(R.id.filters_dialog_period_to))
                .check(matches(
                        hasDescendant(withText(R.string.transaction_filters_end_date_error))
                ));
        onView(withId(R.id.filters_dialog_apply_btn))
                .check(matches(
                        not(isEnabled())
                ));
    }
}
