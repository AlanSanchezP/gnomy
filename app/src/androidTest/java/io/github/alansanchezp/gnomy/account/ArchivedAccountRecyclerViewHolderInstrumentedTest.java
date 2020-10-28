package io.github.alansanchezp.gnomy.account;

import android.view.View;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.dummy.DummyActivity;
import io.github.alansanchezp.gnomy.ui.account.ArchivedAccountsRecyclerViewAdapter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class ArchivedAccountRecyclerViewHolderInstrumentedTest {
    static Account testAccount = new Account();

    @Rule
    public ViewScenarioRule viewRule =
            new ViewScenarioRule(R.layout.fragment_archived_account_card);

    @BeforeClass
    public static void init_test_account() {
        Locale.setDefault(Locale.US);
        testAccount.setName("Test account 1");
        testAccount.setType(Account.BANK);
        testAccount.setBackgroundColor(0xFF4D7C4F);
        testAccount.setArchived(true);
    }

    @Test
    public void account_name_is_displayed_in_card() {
        ActivityScenario<DummyActivity> scenario = viewRule.getScenario();
        View view[] = new View[1];
        ArchivedAccountsRecyclerViewAdapter.ViewHolder[] holder
                = new ArchivedAccountsRecyclerViewAdapter.ViewHolder[1];
        scenario.onActivity(activity -> {
            view[0] = (View) activity.findViewById(R.id.archived_account_card);
            holder[0] = new ArchivedAccountsRecyclerViewAdapter.ViewHolder(view[0]);
            holder[0].setAccountData(testAccount);
        });

        onView(withId(R.id.archived_account_card_name))
                .check(matches(withText(testAccount.getName())));
    }

    @Test
    public void account_icon_in_card_is_correct() {
        ActivityScenario<DummyActivity> scenario = viewRule.getScenario();
        View view[] = new View[1];
        ArchivedAccountsRecyclerViewAdapter.ViewHolder[] holder
                = new ArchivedAccountsRecyclerViewAdapter.ViewHolder[1];
        scenario.onActivity(activity -> {
            view[0] = (View) activity.findViewById(R.id.archived_account_card);
            holder[0] = new ArchivedAccountsRecyclerViewAdapter.ViewHolder(view[0]);
        });

        for (int type=Account.BANK; type <= Account.OTHER; type++) {
            testAccount.setType(type);
            scenario.onActivity(activity ->
                    holder[0].setAccountData(testAccount));
            onView(withId(R.id.archived_account_card_icon))
                    .check(matches(
                        withTagValue(
                            equalTo(Account.getDrawableResourceId(type))
                    )));
            // Couldn't find a way to test drawable tint
        }
    }
}
