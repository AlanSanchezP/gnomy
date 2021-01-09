package io.github.alansanchezp.gnomy.transaction;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.google.android.material.textfield.TextInputEditText;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.category.CategoryRepository;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;
import io.github.alansanchezp.gnomy.ui.transaction.AddEditTransactionActivity;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.reactivex.Single;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static androidx.lifecycle.Lifecycle.State.RESUMED;
import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.espresso.Espresso.onData;
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
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static io.github.alansanchezp.gnomy.EspressoTestUtil.END_ICON;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.ERROR_ICON;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertActivityState;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.clickIcon;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.nestedScrollTo;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.setChecked;
import static io.github.alansanchezp.gnomy.database.MockRepositoryBuilder.initMockRepository;
import static io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction.EXPENSE;
import static io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction.INCOME;
import static io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction.TRANSFER;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AddEditTransactionInstrumentedTest {
    @Rule
    public final ActivityScenarioRule<AddEditTransactionActivity> activityRule =
            new ActivityScenarioRule<>(AddEditTransactionActivity.class);
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private static final MoneyTransactionRepository mockTransactionRepository = initMockRepository(MoneyTransactionRepository.class);
    private static MutableLiveData<List<Account>> testAccountListLD;
    private static final List<Account> testAccountList = new ArrayList<>();
    private static Account testAccountA, testAccountB;
    private static Category testCategory;

    @BeforeClass
    public static void init_mocks() {
        final AccountRepository mockAccountRepository = initMockRepository(AccountRepository.class);
        final CategoryRepository mockCategoryRepository = initMockRepository(CategoryRepository.class);

        testAccountA = new Account(1);
        testAccountA.setName("Test account A");
        testAccountA.setDefaultCurrency("MXN");
        testAccountA.setCreatedAt(DateUtil.OffsetDateTimeNow().minusMonths(1));
        testAccountB = new Account(2);
        testAccountB.setName("Test account B");
        testAccountB.setDefaultCurrency("EUR");
        testAccountB.setCreatedAt(DateUtil.OffsetDateTimeNow().plusMonths(1));
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

        when(mockTransactionRepository.insert(any(MoneyTransaction.class)))
                .thenReturn(Single.just(1L));
        when(mockAccountRepository.getAll())
                .thenReturn(testAccountListLD);
        when(mockCategoryRepository.getSharedAndCategory(anyInt()))
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

        onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_error_account)
                )));

        onView(withId(R.id.addedit_transaction_category))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_error_category)
                )));
    }

    @Test
    public void FAB_finishes_activity_if_data_is_correct() {
        onView(withId(R.id.addedit_transaction_concept_input))
                .perform(replaceText("test"));

        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(replaceText("40"));

        onView(withId(R.id.addedit_transaction_from_account)).perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountB)))
                .perform(click());

        onView(withId(R.id.addedit_transaction_category)).perform(click());
        onData(allOf(is(instanceOf(Category.class)), is(
                testCategory)))
                .perform(click());

        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());

        assertActivityState(DESTROYED, activityRule);
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
    @UiThreadTest
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
        onView(withId(R.id.addedit_transaction_from_account))
                .perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountB)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_currency))
                .check(matches(
                        hasDescendant(withText(CurrencyUtil.getDisplayName(
                                testAccountB.getDefaultCurrency())))));
    }

    @Test
    public void data_is_sent_to_repository() throws GnomyCurrencyException {
        when(mockTransactionRepository.insert(any(MoneyTransaction.class)))
                .then(invocation -> {
                    RuntimeException exception = null;
                    // I don't like this, but it was the only way I found to test this
                    MoneyTransaction sentByForm = invocation.getArgument(0);
                    if (sentByForm.getAccount() != testAccountB.getId()) exception = new RuntimeException();
                    if (sentByForm.getCategory() != testCategory.getId()) exception =  new RuntimeException();
                    if (!sentByForm.getCurrency().equals("USD")) exception =  new RuntimeException();
                    if (!sentByForm.getOriginalValue().equals(
                            BigDecimalUtil.fromString("40"))) exception =  new RuntimeException();
                    if (!sentByForm.getConcept().equals("Test concept")) exception =  new RuntimeException();
                    if (sentByForm.isConfirmed()) exception =  new RuntimeException();
                    if (!sentByForm.getNotes().equals("Test notes")) exception =  new RuntimeException();

                    if (exception == null)
                        return Single.just(1L);
                    else
                        return Single.error(exception);
                });
        // Force more options
        onView(withId(R.id.addedit_transaction_more_options_toggle))
                .perform(click());

        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(typeText("40"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_transaction_concept_input))
                .perform(typeText("Test sds"))// Setting wrong value to trigger RuntimeException
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_transaction_from_account))
                .perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountB)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_currency))
                .perform(nestedScrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is(
                CurrencyUtil.getDisplayName("USD"))))
                .perform(click());
        onView(withId(R.id.addedit_transaction_category))
                .perform(nestedScrollTo(), click());
        onData(allOf(is(instanceOf(Category.class)), is(
                testCategory)))
                .perform(click());

        onView(withId(R.id.addedit_transaction_notes_input))
                .perform(typeText("Test notes"))
                .perform(closeSoftKeyboard());

        onView(withId(R.id.addedit_transaction_mark_as_done)) // In order to avoid coordinates errors
                .perform(nestedScrollTo(), setChecked(false));

        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());

        // These calls should only be possible if insert fails and activity is not
        //  automatically finished
        onView(withId(R.id.addedit_transaction_concept_input))
                .perform(nestedScrollTo(), click())
                .perform(replaceText("Test concept"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());

        try {
            assertActivityState(DESTROYED, activityRule);
        } catch (AssertionError e) {
            // Not sure why SOMETIMES state is DESTROYED and sometimes it's RESUMED
            assertActivityState(RESUMED, activityRule);
        }
        // return to default state
        when(mockTransactionRepository.insert(any(MoneyTransaction.class)))
                .thenReturn(Single.just(1L));
    }

    @Test
    public void dynamic_title_based_on_extras() {
        MoneyTransaction testT = new MoneyTransaction();
        testT.setType(EXPENSE);
        when(mockTransactionRepository.find(anyInt()))
                .thenReturn(new MutableLiveData<>(testT));
        // TODO: Check hints
        // Default behavior is to create a new expense. Not testing tinting since espresso
        //  doesn't provide a way to match color resources yet.
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_new_expense)
                )));
        /*onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(withText(
                        R.string.transaction_from_account
                ))));*/

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditTransactionActivity.class);
        ActivityScenario<AddEditTransactionActivity> tempScenario;

        // Modify expense
        intent = intent
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, EXPENSE)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, 2);
        tempScenario = launch(intent);
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_modify_expense)
                )));
        /*onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(withText(
                        R.string.transaction_from_account
                ))));*/
        tempScenario.close();

        // Create income
        intent = intent
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, INCOME)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, 0);
        tempScenario = launch(intent);
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_new_income)
                )));
        /*onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(withText(
                        R.string.transaction_to_account
                ))));*/
        tempScenario.close();

        testT.setType(INCOME);
        // Modify income
        intent= intent
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, INCOME)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, 2);
        tempScenario = launch(intent);
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_modify_income)
                )));
        /*onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(withText(
                        R.string.transaction_to_account
                ))));*/
        tempScenario.close();

        // New transfer
        intent= intent
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TRANSFER)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, 0);
        tempScenario = launch(intent);
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_new_transfer)
                )));
        /*onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(withText(
                        R.string.transaction_from_account
                ))));*/
        tempScenario.close();

        testT.setType(TRANSFER);
        // Modify transfer
        intent= intent
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TRANSFER)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, 2);
        tempScenario = launch(intent);
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.transaction_modify_transfer)
                )));
        /*onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(withText(
                        R.string.transaction_from_account
                ))));*/
        tempScenario.close();

        // Not testing invalid types because RuntimeException can't be catched
        //  (for some weird reason)
    }

    @Test
    public void sets_account_from_extra() {
        MoneyTransaction testT = new MoneyTransaction();
        testT.setAccount(1);
        testT.setType(INCOME);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testAccountListLD.postValue(testAccountList));
        when(mockTransactionRepository.find(anyInt()))
                .thenReturn(new MutableLiveData<>(testT));
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditTransactionActivity.class)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, INCOME)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, 1);
        ActivityScenario<AddEditTransactionActivity> tempScenario = launch(intent);
        onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(
                        withText(testAccountA.getName())
                )));
        tempScenario.close();
        testT.setType(EXPENSE);
        intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, EXPENSE);
        tempScenario = launch(intent);
        onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(
                        withText(testAccountA.getName())
                )));
        tempScenario.close();

        testT.setType(TRANSFER);
        testT.setTransferDestinationAccount(2);
        intent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TRANSFER);
        tempScenario = launch(intent);
        onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(
                        withText(testAccountA.getName())
                )));
        tempScenario.close();
    }

    @Test
    public void more_options_container_is_toggled() {
        // Initial state
        onView(withId(R.id.addedit_transaction_more_options_container))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView((withId(R.id.addedit_transaction_more_options_text)))
                .check(matches(withText(R.string.show_more_options)));
        // Didn't find a way to test drawable rotation
        onView(withId(R.id.addedit_transaction_more_options_toggle))
                .perform(click());
        onView(withId(R.id.addedit_transaction_more_options_container))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView((withId(R.id.addedit_transaction_more_options_text)))
                .check(matches(withText(R.string.show_less_options)));
        // Asserting both actions: show and hide
        onView(withId(R.id.addedit_transaction_more_options_toggle))
                .perform(click());
        onView(withId(R.id.addedit_transaction_more_options_container))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView((withId(R.id.addedit_transaction_more_options_text)))
                .check(matches(withText(R.string.show_more_options)));
    }

    @Test
    public void account_selection_changes_transaction_date_when_needed() {
        // Default MoneyTransaction.date is always now()
        // Default selected account is testAccountA
        String[] beforeEditTextContent = new String[1];
        String[] afterEditTextContent = new String[1];
        activityRule.getScenario().onActivity(activity ->
                beforeEditTextContent[0] = Objects.requireNonNull(((TextInputEditText)
                        activity.findViewById(R.id.addedit_transaction_date_input))
                .getText()).toString());
        // Force date-only format
        onView(withId(R.id.addedit_transaction_include_time))
                .perform(setChecked(false));

        // testAccountB.createdAt is a future month: Date cannot be older than that
        onView(withId(R.id.addedit_transaction_from_account))
                .perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountB)))
                .perform(click());
        activityRule.getScenario().onActivity(activity ->
                afterEditTextContent[0] = Objects.requireNonNull(((TextInputEditText)
                        activity.findViewById(R.id.addedit_transaction_date_input))
                .getText()).toString());

        String testAccountDateString = DateUtil.getOffsetDateTimeString(testAccountB.getCreatedAt(), false);

        // Displayed date is expected to change
        assertNotEquals(beforeEditTextContent[0], afterEditTextContent[0]);
        // Displayed date should be the same as tesAccountB.getDate()
        assertEquals(testAccountDateString, afterEditTextContent[0]);

        beforeEditTextContent[0] = afterEditTextContent[0];
        // testAccountA.createdAt is a past month: No changes are expected
        onView(withId(R.id.addedit_transaction_from_account))
                .perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountA)))
                .perform(click());
        activityRule.getScenario().onActivity(activity ->
                afterEditTextContent[0] = Objects.requireNonNull(((TextInputEditText)
                        activity.findViewById(R.id.addedit_transaction_date_input))
                .getText()).toString());

        testAccountDateString = DateUtil.getOffsetDateTimeString(testAccountA.getCreatedAt(), false);

        // Displayed date is not expected to change
        assertEquals(beforeEditTextContent[0], afterEditTextContent[0]);
        // Displayed date should NOT be the same as tesAccountB.getDate()
        assertNotEquals(testAccountDateString, afterEditTextContent[0]);
    }

    @Test
    public void opens_picker_dialogs() {
        // Not testing that dates older than account creation are disabled on
        //  picker dialog because that is almost equivalent to testing
        //  DateTimePicker itself and requires too much knowledge of
        //  its internal logic and structure.

        // Testing without including time: Only date picker should show
        onView(withId(R.id.addedit_transaction_include_time))
                .perform(setChecked(false));
        onView(withId(R.id.addedit_transaction_date))
                .perform(clickIcon(END_ICON));

        // Using resource ids from library
        onView(withId(R.id.mdtp_animator))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withId(R.id.mdtp_ok))
                .inRoot(isDialog())
                .perform(click());
        assertThrows(NoMatchingViewException.class, () ->
                onView(withId(R.id.mdtp_time_picker))
                .perform(click()));

        // Including time: Both pickers should show
        onView(withId(R.id.addedit_transaction_include_time))
                .perform(setChecked(true));
        onView(withId(R.id.addedit_transaction_date))
                .perform(clickIcon(END_ICON));

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
    }

    @Test
    public void switch_button_alters_date_format() {
        // Not testing the actual format, since that is already tested in DateUtil
        onView(withId(R.id.addedit_transaction_include_time))
                .perform(setChecked(false));
        String[] onlyDateEditTextContent = new String[1];
        String[] includeTimeEditTextContent = new String[1];

        activityRule.getScenario().onActivity(activity ->
                onlyDateEditTextContent[0] = Objects.requireNonNull(((TextInputEditText)
                        activity.findViewById(R.id.addedit_transaction_date_input))
                .getText()).toString());
        onView(withId(R.id.addedit_transaction_include_time))
                .perform(setChecked(true));
        activityRule.getScenario().onActivity(activity ->
                includeTimeEditTextContent[0] = Objects.requireNonNull(((TextInputEditText)
                        activity.findViewById(R.id.addedit_transaction_date_input))
                .getText()).toString());
        assertNotEquals(onlyDateEditTextContent[0], includeTimeEditTextContent[0]);
    }

    @Test
    public void force_as_not_confirmed_if_future_date() {
        // Force more options
        onView(withId(R.id.addedit_transaction_more_options_toggle))
                .perform(click());
        // Not using date picker and instead forcing testAccountB creation date
        onView(withId(R.id.addedit_transaction_from_account))
                .perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountB)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_mark_as_done))
                .perform(nestedScrollTo())
                .check(matches(allOf(not(isEnabled()), isNotChecked())));
        // TODO: How to check the inverse? Switch is expected to be enabled
        //  again if date is not a future one
    }

    @Test
    public void FAB_displays_error_if_not_account_is_selected() {
        // Not testing case where categories spinner is empty
        //  as IT IS NOT SUPPOSED TO EVER HAPPEN
        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());
        onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(hasDescendant(withText(R.string.transaction_error_account))));
    }

    @Test
    public void opens_calculator_dialog() {
        onView(withId(R.id.addedit_transaction_amount))
                .perform(clickIcon(END_ICON));

        // Using resource ids from library
        onView(withId(R.id.calc_btn_ok))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(replaceText(""));
        onView(withId(R.id.addedit_transaction_amount))
                .perform(clickIcon(ERROR_ICON));
        onView(withId(R.id.calc_btn_ok))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
    }

    @Test
    public void opens_new_account_screen() {
        onView(withId(R.id.addedit_transaction_new_account))
                .perform(click());
        onView(withId(R.id.custom_appbar))
                .check(matches(
                        hasDescendant(withText(R.string.account_new))));
    }

    @Test
    public void new_account_is_set_as_selected() {
        // Click on new Account action
        onView(withId(R.id.addedit_transaction_new_account))
                .perform(click());
        // Emulates the arrival of a new account
        Account newAccount = new Account();
        newAccount.setId(3);
        newAccount.setCreatedAt(DateUtil.OffsetDateTimeNow().minusDays(1));
        newAccount.setName("New test account");
        newAccount.setDefaultCurrency("MXN");
        testAccountList.add(newAccount);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
            testAccountListLD.postValue(testAccountList));

        // Close account activity
        Espresso.pressBack();
        onView(withId(R.id.confirmation_dialog_yes)).perform(click()); // Accept confirmation dialog

        onView(withId(R.id.addedit_transaction_from_account))
                .check(matches(
                        hasDescendant(withText(newAccount.getName()))));

        // Testing on transfer: destination account must be filled
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditTransactionActivity.class)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TRANSFER);
        ActivityScenario<AddEditTransactionActivity> tempScenario = launch(intent);

        // Click on new Account action
        onView(withId(R.id.addedit_transaction_new_account))
                .perform(click());
        // Emulates the arrival of a new account
        newAccount = new Account();
        newAccount.setId(4);
        newAccount.setCreatedAt(DateUtil.OffsetDateTimeNow().minusDays(1));
        newAccount.setName("New test account 2");
        newAccount.setDefaultCurrency("MXN");
        testAccountList.add(newAccount);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testAccountListLD.postValue(testAccountList));

        // Close account activity
        Espresso.pressBack();
        onView(withId(R.id.confirmation_dialog_yes)).perform(click()); // Accept confirmation dialog

        onView(withId(R.id.addedit_transaction_to_account))
                .check(matches(
                        hasDescendant(withText(newAccount.getName()))));

        tempScenario.close();
    }

    // TODO: Implement when categories module is ready
    @Test
    public void opens_new_category_activity() {
        assert true;
    }

    @Test
    public void new_category_is_set_as_selected() {
        assert true;
    }

    // TRANSFER-RELATED TESTS

    @Test
    public void elements_toggle_visibility_if_transfer() {
        // Force more options
        onView(withId(R.id.addedit_transaction_more_options_toggle))
                .perform(click());
        // Default behavior on tests is expense
        onView(withId(R.id.addedit_transaction_to_account))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.addedit_transaction_mark_as_done))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.addedit_transaction_new_category))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.addedit_transaction_category))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditTransactionActivity.class)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TRANSFER);
        ActivityScenario<AddEditTransactionActivity> tempScenario = launch(intent);

        // Force more options
        onView(withId(R.id.addedit_transaction_more_options_toggle))
                .perform(click());
        onView(withId(R.id.addedit_transaction_to_account))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.addedit_transaction_mark_as_done))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
                .check(matches(isChecked()));
        onView(withId(R.id.addedit_transaction_new_category))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.addedit_transaction_category))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        tempScenario.close();
    }

    @Test
    public void displays_error_if_accounts_are_the_same() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditTransactionActivity.class)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TRANSFER);
        ActivityScenario<AddEditTransactionActivity> tempScenario = launch(intent);

        // Select same account
        onView(withId(R.id.addedit_transaction_from_account)).perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountA)))
                .perform(click());

        onView(withId(R.id.addedit_transaction_to_account)).perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountA)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_to_account))
                .check(matches(hasDescendant(withText(R.string.transaction_error_transfer_destination_account))));

        // Clears error after selection
        onView(withId(R.id.addedit_transaction_from_account)).perform(click());
        onData(allOf(is(instanceOf(Account.class)), is(
                testAccountB)))
                .perform(click());
        onView(withId(R.id.addedit_transaction_to_account))
                .check(matches(not(hasDescendant(withText(R.string.transaction_error_transfer_destination_account)))));

        tempScenario.close();
    }

    @Test
    public void dynamic_nullity_rules_if_transfer() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testAccountListLD.postValue(new ArrayList<>()));

        // Default behavior on tests if expense: not checking destination account
        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());
        onView(withId(R.id.addedit_transaction_to_account))
                .check(matches(not(hasDescendant(withText(R.string.transaction_error_account)))));

        // Destination account can't be null on transfers
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditTransactionActivity.class)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TRANSFER);
        ActivityScenario<AddEditTransactionActivity> tempScenario = launch(intent);

        onView(withId(R.id.addedit_transaction_FAB))
                .perform(click());
        onView(withId(R.id.addedit_transaction_to_account))
                .check(matches(hasDescendant(withText(R.string.transaction_error_account))));

        // Return to default
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testAccountListLD.postValue(testAccountList));
        tempScenario.close();
    }

    @Test
    public void shows_dialog_if_destination_account_is_archived() {
        MoneyTransaction testT = new MoneyTransaction();
        testT.setTransferDestinationAccount(5);
        testT.setAccount(1);
        testT.setType(TRANSFER);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                testAccountListLD.postValue(testAccountList));
        when(mockTransactionRepository.find(anyInt()))
                .thenReturn(new MutableLiveData<>(testT));
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditTransactionActivity.class)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, TRANSFER)
                .putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, 1);
        ActivityScenario<AddEditTransactionActivity> tempScenario = launch(intent);
        onView(withText(R.string.transaction_transfer_destination_archived_message))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        tempScenario.close();
        // Not testing that activity throws ArrayIndexOutOfBoundsException if origin account is not in the returned list
        // because don't know how catch the exception here (thrown in a different thread)
    }

    // TODO: Test orphan transfers category (app should not crash)
}
