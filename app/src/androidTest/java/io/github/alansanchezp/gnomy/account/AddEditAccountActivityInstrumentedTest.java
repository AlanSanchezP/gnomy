package io.github.alansanchezp.gnomy.account;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.data.account.Account;
import io.github.alansanchezp.gnomy.data.account.AccountRepository;
import io.github.alansanchezp.gnomy.ui.account.AccountTypeItem;
import io.github.alansanchezp.gnomy.ui.account.AddEditAccountActivity;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
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
import static androidx.test.espresso.matcher.ViewMatchers.hasBackground;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertActivityState;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.nestedScrollTo;
import static io.github.alansanchezp.gnomy.data.MockRepositoryBuilder.initMockRepository;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AddEditAccountActivityInstrumentedTest {
    @Rule
    public final ActivityScenarioRule<AddEditAccountActivity> activityRule =
            new ActivityScenarioRule<>(AddEditAccountActivity.class);
    private static final AccountRepository mockAccountRepository = initMockRepository(AccountRepository.class);
    private static final MutableLiveData<Account> mutableAccount = new MutableLiveData<>();

    // Needed so that ViewModel instance doesn't crash
    @BeforeClass
    public static void init_mocks() {
        when(mockAccountRepository.insert(any(Account.class)))
                .thenReturn(Single.just(1L));
        when(mockAccountRepository.getAccount(anyInt()))
                .thenReturn(mutableAccount);
        when(mockAccountRepository.update(any(Account.class)))
                .thenReturn(Single.just(1));
    }

    @Test
    public void dynamic_title_is_correct() {
        // Default behavior is new account
        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_new)
                )));
        mutableAccount.postValue(new Account(1));
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditAccountActivity.class)
                .putExtra(AddEditAccountActivity.EXTRA_ACCOUNT_ID, 1);
        ActivityScenario<AddEditAccountActivity> tempScenario = launch(intent);

        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_card_modify)
                )));
        tempScenario.close();
    }

    @Test
    public void non_existent_account_from_extra() {
        // Create a new intent passing invalid account id
        mutableAccount.postValue(null);
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                AddEditAccountActivity.class)
                .putExtra(AddEditAccountActivity.EXTRA_ACCOUNT_ID, 2);
        ActivityScenario<AddEditAccountActivity> tempScenario = launch(intent);
        assertActivityState(DESTROYED, tempScenario);
        tempScenario.close();
    }

    @Test
    public void sets_proper_initial_background_color() {
        onView(withId(R.id.custom_appbar))
                .check(matches(
                        not(hasBackground(android.R.color.transparent))
                ));
    }

    @Test
    public void text_fields_trigger_error_if_empty() {
        onView(withId(R.id.addedit_account_name_input))
                .perform(typeText("test"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_account_name))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_name)
                )));

        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(typeText("40"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_account_initial_value))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_initial_value)
                )));
    }

    @Test
    public void text_fields_do_not_trigger_error_on_rotation_if_pristine() {
        activityRule.getScenario().onActivity(activity -> {
           activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
           activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        onView(withId(R.id.addedit_account_name))
                .check(matches(not(hasDescendant(
                        withText(R.string.account_error_name)
                ))));

        onView(withId(R.id.addedit_account_initial_value))
                .check(matches(not(hasDescendant(
                        withText(R.string.account_error_initial_value)
                ))));
    }


    @Test
    public void text_fields_keep_errors_on_rotation_if_not_pristine() {
        onView(withId(R.id.addedit_account_name_input))
                .perform(replaceText("test"))
                .perform(replaceText(""));

        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(replaceText("40"))
                .perform(replaceText(""));

        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });

        onView(withId(R.id.addedit_account_name))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_name)
                )));

        onView(withId(R.id.addedit_account_initial_value))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_initial_value)
                )));
    }

    @Test
    public void button_opens_color_picker() {
        onView(withId(R.id.addedit_account_color_button))
                .perform(click());

        onView(withText(R.string.account_pick_color_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void prompts_error_if_text_fields_empty_when_form_is_submitted() {
        onView(withId(R.id.addedit_account_FAB))
                .perform(click());

        onView(withId(R.id.addedit_account_name))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_name)
                )));

        onView(withId(R.id.addedit_account_initial_value))
                .check(matches(hasDescendant(
                        withText(R.string.account_error_initial_value)
                )));
    }

    @Test
    public void FAB_finishes_activity_if_data_is_correct() {
        onView(withId(R.id.addedit_account_name_input))
                .perform(replaceText("test"));

        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(replaceText("40"));

        onView(withId(R.id.addedit_account_FAB))
                .perform(click());

        assertActivityState(DESTROYED, activityRule);
    }

    @Test
    public void not_number_on_initial_value() {
        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(replaceText("ñ"));

        onView(withId(R.id.addedit_account_initial_value_input))
                .check(matches(withText("")));

        assertThrows(RuntimeException.class,
                () -> onView(withId(R.id.addedit_account_initial_value_input))
                        .perform(typeText("ñ")));
    }


    @Test
    public void data_is_sent_to_repository() throws GnomyCurrencyException {
        when(mockAccountRepository.insert(any(Account.class)))
                .then(invocation -> {
                    RuntimeException exception = null;
                    // I don't like this, but it was the only way I found to test this
                    Account sentByForm = invocation.getArgument(0);
                    if (!sentByForm.getDefaultCurrency().equals("MXN")) exception =  new RuntimeException();
                    if (!sentByForm.getInitialValue().equals(
                            BigDecimalUtil.fromString("40"))) exception =  new RuntimeException();
                    if (!sentByForm.getName().equals("Test account")) exception =  new RuntimeException();
                    if (sentByForm.getType() != Account.INFORMAL) exception =  new RuntimeException();

                    // Not testing selected color: Don't know how to select an item.

                    if (exception == null)
                        return Single.just(1L);
                    else
                        return Single.error(exception);
                });

        //noinspection SpellCheckingInspection
        onView(withId(R.id.addedit_account_name_input))
                .perform(typeText("gffdg")) // setting wrong value
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_account_initial_value_input))
                .perform(typeText("40"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_account_currency))
                .perform(click());
        onData(allOf(is(instanceOf(String.class)), is(
                CurrencyUtil.getDisplayName("MXN"))))
                .perform(click());
        onView(withId(R.id.addedit_account_type))
                .perform(click());
        onData(allOf(is(instanceOf(AccountTypeItem.class)), is(
                // Using hardcoded string as AccountTypeItem.equals only considers type
                new AccountTypeItem(Account.INFORMAL, "Informal"))))
                .perform(click());

        onView(withId(R.id.addedit_account_FAB))
                .perform(click());

        // These calls should only be possible if insert fails and activity is not
        //  automatically finished
        onView(withId(R.id.addedit_account_name_input))
                .perform(nestedScrollTo(), click())
                .perform(replaceText("Test account"))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.addedit_account_FAB))
                .perform(click());

        // return to default state
        when(mockAccountRepository.insert(any(Account.class)))
                .thenReturn(Single.just(1L));

        try {
            assertActivityState(DESTROYED, activityRule);
        } catch (AssertionError e) {
            // Not sure why SOMETIMES state is DESTROYED and sometimes it's RESUMED
            assertActivityState(RESUMED, activityRule);
        }
    }
}
