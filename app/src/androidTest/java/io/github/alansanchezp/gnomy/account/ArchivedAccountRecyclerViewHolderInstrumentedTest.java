package io.github.alansanchezp.gnomy.account;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.databinding.FragmentArchivedAccountCardBinding;
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
    static final Account testAccount = new Account();

    @Rule
    public final ViewScenarioRule viewRule =
            new ViewScenarioRule(R.layout.fragment_archived_account_card);

    @BeforeClass
    public static void init_test_account() {
        testAccount.setName("Test account 1");
        testAccount.setType(Account.BANK);
        testAccount.setBackgroundColor(0xFF4D7C4F);
        testAccount.setArchived(true);
    }

    @Test
    public void account_name_is_displayed_in_card() {
        ActivityScenario<DummyActivity> scenario = viewRule.getScenario();
        ArchivedAccountsRecyclerViewAdapter.ViewHolder[] holder
                = new ArchivedAccountsRecyclerViewAdapter.ViewHolder[1];
        scenario.onActivity(activity -> {
            FragmentArchivedAccountCardBinding viewBinding = FragmentArchivedAccountCardBinding.inflate(
                    activity.getLayoutInflater(), activity.findViewById(R.id.dummy_activity_root),true);
            holder[0] = new ArchivedAccountsRecyclerViewAdapter.ViewHolder(viewBinding);
            holder[0].setAccountData(testAccount);
        });

        onView(withId(R.id.archived_account_card_name))
                .check(matches(withText(testAccount.getName())));
    }

    @Test
    public void account_icon_in_card_is_correct() {
        ActivityScenario<DummyActivity> scenario = viewRule.getScenario();
        ArchivedAccountsRecyclerViewAdapter.ViewHolder[] holder
                = new ArchivedAccountsRecyclerViewAdapter.ViewHolder[1];
        scenario.onActivity(activity -> {
            FragmentArchivedAccountCardBinding viewBinding = FragmentArchivedAccountCardBinding.inflate(
                    activity.getLayoutInflater(), activity.findViewById(R.id.dummy_activity_root),true);
            holder[0] = new ArchivedAccountsRecyclerViewAdapter.ViewHolder(viewBinding);
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
