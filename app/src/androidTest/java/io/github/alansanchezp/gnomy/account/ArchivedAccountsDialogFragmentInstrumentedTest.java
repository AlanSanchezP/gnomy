package io.github.alansanchezp.gnomy.account;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.account.ArchivedAccountsDialogFragment;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ArchivedAccountsDialogFragmentInstrumentedTest {
    private static final Account[] accounts = new Account[2];

    @BeforeClass
    public static void init_accounts_list() {
        accounts[0] = new Account();
        accounts[1] = new Account();

        accounts[0] = new Account();
        accounts[0].setName("Test account 1");

        accounts[1] = new Account();
        accounts[1].setName("Test account 2");
    }

    @Test
    public void dynamically_shown_ui_elements_according_to_list_size() {
        FragmentScenario<ArchivedAccountsDialogFragment> scenario = launchInContainer(
                ArchivedAccountsDialogFragment.class, null, R.style.AppTheme, null);
        List<Account> accountsList = new ArrayList<>();

        // List size is 0 as initial state or LiveData hasn't returned any results
        onView(withId(R.id.archived_items_empty))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                ));

        onView(withId(R.id.restore_all_accounts_button))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        // List grows
        accountsList.add(accounts[0]);
        scenario.onFragment(fragment ->
                fragment.onAccountsListChanged(accountsList));

        onView(withId(R.id.archived_items_empty))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        onView(withId(R.id.restore_all_accounts_button))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        accountsList.add(accounts[1]);
        scenario.onFragment(fragment ->
                fragment.onAccountsListChanged(accountsList));

        onView(withId(R.id.archived_items_empty))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        onView(withId(R.id.restore_all_accounts_button))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                ));

        // List size is 0 as a result of restoring/deleting all items
        accountsList.clear();
        scenario.onFragment(fragment ->
                fragment.onAccountsListChanged(accountsList));

        try {
            onView(withId(R.id.archived_items_container))
                    .check(matches(isDisplayed()));
            throw new RuntimeException("Modal is still present in hierarchy");
        } catch (NoMatchingViewException nmve) {
            // Modal is not in hierarchy anymore
            assert(true);
        }
    }
}

