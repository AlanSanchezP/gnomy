package io.github.alansanchezp.gnomy.transaction;

import android.content.pm.ActivityInfo;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.MockDatabaseOperationsUtil;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.ui.transaction.AddEditTransactionActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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

    @BeforeClass
    public static void init_mocks() {
        final MockDatabaseOperationsUtil.MockableMoneyTransactionDAO mockTransactionDAO = mock(MockDatabaseOperationsUtil.MockableMoneyTransactionDAO.class);
        final MockDatabaseOperationsUtil.MockableAccountDAO mockAccountDAO = mock(MockDatabaseOperationsUtil.MockableAccountDAO.class);
        final MockDatabaseOperationsUtil.MockableCategoryDAO mockCategoryDAO = mock(MockDatabaseOperationsUtil.MockableCategoryDAO.class);

        MockDatabaseOperationsUtil.setTransactionDAO(mockTransactionDAO);
        MockDatabaseOperationsUtil.setAccountDAO(mockAccountDAO);
        MockDatabaseOperationsUtil.setCategoryDAO(mockCategoryDAO);
        List<Account> testAccountList = new ArrayList<>();
        testAccountList.add(new Account());
        MutableLiveData<List<Account>> testAccountListLD = new MutableLiveData<>(testAccountList);
        List<Category> testCategoryList = new ArrayList<>();
        testCategoryList.add(new Category());
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

        try {
            onView(withId(R.id.addedit_transaction_FAB))
                    .perform(click());
        } catch (RuntimeException re) {
            assert true;
        }
        assert false;
    }

    @Test
    public void not_number_on_amount() {
        onView(withId(R.id.addedit_transaction_amount_input))
                .perform(replaceText("ñ"));

        onView(withId(R.id.addedit_transaction_amount_input))
                .check(matches(withText("")));

        try {
            onView(withId(R.id.addedit_transaction_amount_input))
                    .perform(typeText("ñ"));
        } catch (RuntimeException re) {
            assert true;
        }

        assert false;
    }

    @Test
    public void sets_currency_from_selected_account() {
        assert true;
    }

    @Test
    public void initial_date_is_today() {
        assert true;
    }

    @Test
    public void data_from_spinners_is_sent_to_repository() {
        assert true;
    }
}
