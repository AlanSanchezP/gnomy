package io.github.alansanchezp.gnomy;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.YearMonth;

import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.database.MockDatabaseOperationsUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
public class MainNavigationInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Needed so that ViewModel instance doesn't crash
    @BeforeClass
    public static void init_mocks() {
        final MockDatabaseOperationsUtil.MockableAccountDAO mockAccountDAO = mock(MockDatabaseOperationsUtil.MockableAccountDAO.class);
        MockDatabaseOperationsUtil.setAccountDAO(mockAccountDAO);

        when(mockAccountDAO.getArchivedAccounts())
                .thenReturn(new MutableLiveData<>());
        when(mockAccountDAO.getTodayAccumulatesList())
                .thenReturn(new MutableLiveData<>());
        when(mockAccountDAO.getAccumulatesListAtMonth(any(YearMonth.class)))
                .thenReturn(new MutableLiveData<>());
        when(mockAccountDAO.find(anyInt()))
                .thenReturn(new MutableLiveData<>());
    }

    @Test
    public void switches_to_accounts() {
        onView(withId(R.id.navigation_accounts))
                .perform(click());

        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.title_accounts)
                )));

        onView(withId(R.id.total_balance_label))
                .check(matches(isDisplayed()));
    }

    @Test
    public void switches_to_transactions() {
        onView(withId(R.id.navigation_transactions))
                .perform(click());

        onView(withId(R.id.custom_appbar))
                .check(matches(hasDescendant(
                        withText(R.string.title_transactions)
                )));
    }
}
