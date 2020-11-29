package io.github.alansanchezp.gnomy.transaction;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.Checkable;

import org.hamcrest.BaseMatcher;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.MockDatabaseOperationsUtil;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.ui.transaction.AddEditTransactionActivity;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.ErrorUtil.assertThrows;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AddTransactionInstrumentedTest {
    @Rule
    public final ActivityScenarioRule<AddEditTransactionActivity> activityRule =
            new ActivityScenarioRule<>(AddEditTransactionActivity.class);
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private static final MockDatabaseOperationsUtil.MockableMoneyTransactionDAO mockTransactionDAO = mock(MockDatabaseOperationsUtil.MockableMoneyTransactionDAO.class);
    private static MutableLiveData<List<Account>> testAccountListLD;
    private static List<Account> testAccountList = new ArrayList<>();
    private static Account testAccountA, testAccountB;
    private static Category testCategory;

    @BeforeClass
    public static void init_mocks() {
        final MockDatabaseOperationsUtil.MockableAccountDAO mockAccountDAO = mock(MockDatabaseOperationsUtil.MockableAccountDAO.class);
        final MockDatabaseOperationsUtil.MockableCategoryDAO mockCategoryDAO = mock(MockDatabaseOperationsUtil.MockableCategoryDAO.class);

        MockDatabaseOperationsUtil.setTransactionDAO(mockTransactionDAO);
        MockDatabaseOperationsUtil.setAccountDAO(mockAccountDAO);
        MockDatabaseOperationsUtil.setCategoryDAO(mockCategoryDAO);

        testAccountA = new Account(1);
        testAccountA.setName("Test account A");
        testAccountA.setDefaultCurrency("MXN");
        testAccountB = new Account(2);
        testAccountB.setName("Test account B");
        testAccountB.setDefaultCurrency("EUR");
        testAccountList.add(testAccountA);
        testAccountList.add(testAccountB);

        testAccountListLD = new MutableLiveData<>(testAccountList);

        testCategory = new Category();
        testCategory.setName("Test category");
        testCategory.setId(2);
        Category anotherCategory = new Category();
        anotherCategory.setName("Another category");
        anotherCategory.setId(1);

        List<Category> testCategoryList = new ArrayList<>();
        testCategoryList.add(anotherCategory);
        testCategoryList.add(testCategory);
        MutableLiveData<List<Category>> testCategoryListLD = new MutableLiveData<>(testCategoryList);

        when(mockTransactionDAO._insert(any(MoneyTransaction.class)))
                .thenReturn(1L);
        when(mockAccountDAO.getAll())
                .thenReturn(testAccountListLD);
        when(mockCategoryDAO.getAll())
                .thenReturn(testCategoryListLD);
    }

    @Test
    public void text_fields_trigger_error_if_empty() {
        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(typeText("40"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_transaction_amount))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_error_amount)
                )));

        onView(withId(R.id.addedit_transaction_concept_input))
                .perform(typeText("Test"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_transaction_concept))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_error_concept)
                )));
    }


    @Test
    public void text_fields_do_not_trigger_error_on_rotation_if_pristine() {
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        onView(withId(R.id.addedit_transaction_concept))
                .check(matches(not(hasDescendant(
                        withText(R.string.transaction_error_concept)
                ))));

        onView(withId(R.id.addedit_transaction_amount))
                .check(matches(not(hasDescendant(
                        withText(R.string.transaction_error_amount)
                ))));
    }

    @Test
    public void text_fields_keep_errors_on_rotation_if_not_pristine() {
        onView(withId(R.id.addedit_transaction_concept_input))
                .perform(replaceText("test"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(replaceText("40"))
                .perform(replaceText(""));

        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        onView(withId(R.id.addedit_transaction_concept))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_error_concept)
                )));

        onView(withId(R.id.addedit_transaction_amount))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_error_amount)
                )));
    }

    @Test
    public void prompts_error_if_text_fields_empty_when_form_is_submitted() {
        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());

        onView(withId(R.id.addedit_transaction_concept))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_error_concept)
                )));

        onView(withId(R.id.addedit_transaction_amount))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_error_amount)
                )));
        // TODO: Reject too if accounts or categories are empty lists
    }

    @Test
    public void FAB_finishes_activity_if_data_is_correct() {
        onView(withId(R.id.addedit_transaction_concept_input))
                .perform(replaceText("test"));

        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(replaceText("40"));

        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());

        assertThrows(RuntimeException.class,
                () -> onView(withId(R.id.addedit_transaction_FAB))
                        .perform(click()));
    }

    @Test
    public void not_number_on_amount() {
        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(replaceText("ñ"));

        onView(withId(R.id.addedit_transaction_amount_input))
                .check(matches(withText("")));

        assertThrows(RuntimeException.class,
                () -> onView(withId(R.id.addedit_transaction_amount_input))
                        .perform(typeText("ñ")));
    }

    @Test
    public void not_crashing_if_empty_accounts_list() {
        // Not testing empty categories list since that should NEVER happen
        //  and it's the repository's duty to guarantee that
        // Try using both empty list and null list
        testAccountList.clear();
        testAccountListLD.postValue(testAccountList);
        testAccountListLD.postValue(null);
        // Reset for other tests
        testAccountList.add(testAccountA);
        testAccountList.add(testAccountB);
        testAccountListLD.postValue(testAccountList);
    }

    @Test
    public void sets_currency_from_selected_account() throws GnomyCurrencyException {
        // Set test values to test list
        onView(withId(R.id.addedit_transaction_from_account))
                .perform(click());
        // Using testAccount2 since by default, index 0 is used on spinner and therefore
        //  not on spinner options
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountB)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_currency))
                .check(matches(
                        withText(CurrencyUtil.getDisplayName(
                                testAccountB.getDefaultCurrency()))));
        onView(withId(R.id.addedit_transaction_from_account))
                .perform(click());

        // Check the other way around
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountA)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_currency))
                .check(matches(
                        withText(CurrencyUtil.getDisplayName(
                                testAccountA.getDefaultCurrency()))));
    }

    @Test
    public void initial_date_is_today() {
        onView(withId(R.id.addedit_transaction_date_input))
                .check(matches(withText(DateUtil.OffsetDateTimeNow().format(DateTimeFormatter.ISO_LOCAL_DATE))));
    }

    @Test
    public void data_is_sent_to_repository() throws GnomyCurrencyException {
        when(mockTransactionDAO._insert(any(MoneyTransaction.class)))
                .then(invocation -> {
                    // I don't like this, but it was the only way I found to test this
                    MoneyTransaction sentByForm = invocation.getArgument(0);
                    if (sentByForm.getAccount() != testAccountB.getId()) throw new RuntimeException();
                    if (sentByForm.getCategory() != testCategory.getId()) throw new RuntimeException();
                    if (!sentByForm.getCurrency().equals("USD")) throw new RuntimeException();
                    if (!sentByForm.getOriginalValue().equals(
                            BigDecimalUtil.fromString("40"))) throw new RuntimeException();
                    if (!sentByForm.getConcept().equals("Test concept")) throw new RuntimeException();
                    if (sentByForm.isConfirmed()) throw new RuntimeException();
                    if (!sentByForm.getNotes().equals("Test notes")) throw new RuntimeException();
                    return 1L;
                });
        onView(withId(R.id.addedit_transaction_from_account))
                .perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountB)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_currency))
                .perform(click());
        onData(allOf(is(instanceOf(String.class)), is(
                CurrencyUtil.getDisplayName("USD"))))
                .perform(click());
        onView(withId(R.id.addedit_transaction_category))
                .perform(click());
        onData(allOf(is(instanceOf(Category.class)), is(
                testCategory)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(typeText("40"));
        onView(withId(R.id.addedit_transaction_concept_input))
                .perform(typeText("Test sds"))// Setting wrong value to trigger RuntimeException
                .perform(closeSoftKeyboard());

        onView(withId(R.id.addedit_transaction_notes_input))
                .perform(swipeUp(), click()) // In order to avoid coordinates errors
                .perform(typeText("Test notes"))
                .perform(closeSoftKeyboard());

        onView(withId(R.id.addedit_transaction_mark_as_done)) // In order to avoid coordinates errors
                .perform(swipeUp(), setChecked(false));

        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());

        // These calls should only be possible if _insert threw a RuntimeException
        onView(withId(R.id.addedit_transaction_concept_input))
                .perform(swipeDown(), click())
                .perform(replaceText("Test concept"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());

        // As data now matches the one sent by form, activity is expected to be finished
        //  and therefore trigger NoMatchingViewException
        assertThrows(NoMatchingViewException.class,
                () -> onView(withId(R.id.addedit_account_FAB))
                        .perform(click()));
        // return to default state
        when(mockTransactionDAO._insert(any(MoneyTransaction.class)))
                .thenReturn(1L);
    }

    // TODO: Implement these features on Activity
    @Test
    public void account_selection_changes_transaction_date_when_needed() {
        assert true;
    }

    @Test
    public void cannot_select_date_older_than_account() {
        assert true;
    }

    @Test
    public void displays_error_if_dynamic_accounts_spinner_is_empty() {
        assert true;
    }

    // TODO: Update methods logic
    //  https://xebia.com/blog/android-intent-extras-espresso-rules/

    // TODO: Move into separate class
    public static ViewAction setChecked(final boolean checked) {
        return new ViewAction() {
            @Override
            public BaseMatcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public void describeTo(org.hamcrest.Description description) {
                    }

                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);
            }
        };
    }
}
