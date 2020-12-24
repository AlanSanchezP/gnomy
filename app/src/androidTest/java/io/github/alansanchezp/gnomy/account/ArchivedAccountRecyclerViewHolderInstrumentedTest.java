package io.github.alansanchezp.gnomy.account;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ViewScenarioRule;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.databinding.LayoutArchivedAccountCardBinding;
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
    public final ViewScenarioRule viewRule = new ViewScenarioRule(
            LayoutArchivedAccountCardBinding.class);

    @BeforeClass
    public static void init_test_account() {
        testAccount.setName("Test account 1");
        testAccount.setType(Account.BANK);
        testAccount.setBackgroundColor(0xFF4D7C4F);
        testAccount.setArchived(true);
    }

    @Test
    public void account_name_is_displayed_in_card() {
        ArchivedAccountsRecyclerViewAdapter.ViewHolder holder
                = new ArchivedAccountsRecyclerViewAdapter.ViewHolder(Objects.requireNonNull(viewRule.retrieveViewBinding()));
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                holder.setAccountData(testAccount));

        onView(withId(R.id.archived_account_card_name))
                .check(matches(withText(testAccount.getName())));
    }

    @Test
    public void account_icon_in_card_is_correct() {

        ArchivedAccountsRecyclerViewAdapter.ViewHolder holder
                = new ArchivedAccountsRecyclerViewAdapter.ViewHolder(Objects.requireNonNull(viewRule.retrieveViewBinding()));


        for (int type=Account.BANK; type <= Account.OTHER; type++) {
            testAccount.setType(type);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    holder.setAccountData(testAccount));
            onView(withId(R.id.archived_account_card_icon))
                    .check(matches(
                        withTagValue(
                            equalTo(Account.getDrawableResourceId(type))
                    )));
            // Couldn't find a way to test drawable tint
        }
    }
}
