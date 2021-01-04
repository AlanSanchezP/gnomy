package io.github.alansanchezp.gnomy.account;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.androidUtil.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.ui.account.ArchivedAccountsDialogFragment;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static io.github.alansanchezp.gnomy.EspressoTestUtil.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ArchivedAccountsDialogFragmentInstrumentedTest {
    private static final Account[] accounts = new Account[2];
    private static final MutableLiveData<List<Account>> mutableAccountsList = new MutableLiveData<>();
    private static GnomyFragmentFactory factory;

    @BeforeClass
    public static void init_accounts_list() {
        ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface _interface =
                mock(ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface.class);
        when(_interface.getArchivedAccounts()).thenReturn(mutableAccountsList);
        factory = new GnomyFragmentFactory()
                .addMapElement(ArchivedAccountsDialogFragment.class, _interface);

        accounts[0] = new Account();
        accounts[0].setName("Test account 1");
        accounts[0].setId(1);

        accounts[1] = new Account();
        accounts[1].setName("Test account 2");
        accounts[1].setId(2);
    }

    @Test
    public void dynamically_shown_ui_elements_according_to_list_size() {
        launchInContainer(
                ArchivedAccountsDialogFragment.class, null, R.style.AppTheme, factory);
        List<Account> accountsList = new ArrayList<>();
        mutableAccountsList.postValue(accountsList);

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
        mutableAccountsList.postValue(accountsList);

        onView(withId(R.id.archived_items_empty))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        onView(withId(R.id.restore_all_accounts_button))
                .check(matches(
                        withEffectiveVisibility(ViewMatchers.Visibility.GONE)
                ));

        accountsList.add(accounts[1]);
        mutableAccountsList.postValue(accountsList);

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
        mutableAccountsList.postValue(accountsList);

        assertThrows(NoMatchingViewException.class,
                () -> onView(withId(R.id.archived_items_container))
                            .check(matches(isDisplayed())));
    }
}

