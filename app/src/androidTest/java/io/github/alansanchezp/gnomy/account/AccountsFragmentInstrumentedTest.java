package io.github.alansanchezp.gnomy.account;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.testing.FragmentScenario;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static io.github.alansanchezp.gnomy.RecyclerViewMatcher.withRecyclerView;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AccountsFragmentInstrumentedTest {
    /*
    This class tests AccountRecyclerViewAdapter too, as
    RecyclerViewAdapters cannot be tested in isolation
    like Activities and Fragments.
    */
    private static AccountWithBalance[] accountsWithBalance = new AccountWithBalance[6];

    @BeforeClass
    public static void init_accounts_list_and_set_locale() {
        Locale.setDefault(Locale.US);
        accountsWithBalance[0] = new AccountWithBalance();
        accountsWithBalance[1] = new AccountWithBalance();
        accountsWithBalance[2] = new AccountWithBalance();
        accountsWithBalance[3] = new AccountWithBalance();
        accountsWithBalance[4] = new AccountWithBalance();
        accountsWithBalance[5] = new AccountWithBalance();

        accountsWithBalance[0].account = new Account();
        accountsWithBalance[0].account.setName("Test account 1");
        accountsWithBalance[0].account.setType(Account.BANK);
        accountsWithBalance[0].account.setInitialValue("0");
        accountsWithBalance[0].accumulatedBalance = new BigDecimal("20");
        accountsWithBalance[0].projectedBalance = new BigDecimal("30");

        accountsWithBalance[1].account = new Account();
        accountsWithBalance[1].account.setName("Test account 2");
        accountsWithBalance[1].account.setType(Account.INFORMAL);
        accountsWithBalance[1].account.setInitialValue("0");
        accountsWithBalance[1].accumulatedBalance = new BigDecimal("40");
        accountsWithBalance[1].projectedBalance = new BigDecimal("50");

        accountsWithBalance[2].account = new Account();
        accountsWithBalance[2].account.setName("Test account 3");
        accountsWithBalance[2].account.setType(Account.OTHER);
        accountsWithBalance[2].account.setInitialValue("0");
        accountsWithBalance[2].accumulatedBalance = new BigDecimal("20");
        accountsWithBalance[2].projectedBalance = new BigDecimal("30");

        accountsWithBalance[3].account = new Account();
        accountsWithBalance[3].account.setName("Test account 4");
        accountsWithBalance[3].account.setType(Account.CREDIT_CARD);
        accountsWithBalance[3].account.setInitialValue("0");
        accountsWithBalance[3].accumulatedBalance = new BigDecimal("40");
        accountsWithBalance[3].projectedBalance = new BigDecimal("50");

        accountsWithBalance[4].account = new Account();
        accountsWithBalance[4].account.setName("Test account 5");
        accountsWithBalance[4].account.setType(Account.SAVINGS);
        accountsWithBalance[4].account.setInitialValue("0");
        accountsWithBalance[4].accumulatedBalance = new BigDecimal("20");
        accountsWithBalance[4].projectedBalance = new BigDecimal("30");

        accountsWithBalance[5].account = new Account();
        accountsWithBalance[5].account.setName("Test account 6");
        accountsWithBalance[5].account.setType(Account.INVERSIONS);
        accountsWithBalance[5].account.setInitialValue("0");
        accountsWithBalance[5].accumulatedBalance = new BigDecimal("40");
        accountsWithBalance[5].projectedBalance = new BigDecimal("50");
    }

    @Test
    public void dynamic_balance_labels_are_correct() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme, null);
        List<AccountWithBalance> accounts = new ArrayList<>();
        accounts.add(accountsWithBalance[4]);

        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(ViewMatchers.withId(R.id.total_projected_label))
                .check(matches(
                        withText(R.string.account_projected_balance)
                ));
        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_projected_label)
        ).check(matches(withText(R.string.account_projected_balance)));

        scenario.onFragment(fragment -> {
            fragment.onMonthChanged(YearMonth.now().minusMonths(1));
            fragment.onAccountsListChanged(accounts);
        });

        onView(ViewMatchers.withId(R.id.total_projected_label))
                .check(matches(
                        withText(R.string.account_accumulated_balance)
                ));
        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_projected_label)
        ).check(matches(withText(R.string.account_accumulated_balance)));
    }

    @Test
    public void archived_accounts_modal_is_shown() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class);
        onView(withId(R.id.archived_items_container))
                .check(
                        doesNotExist()
                );
        scenario.onFragment(fragment -> {
            MenuItem menuItem = new MenuItem() {
                @Override
                public int getItemId() {
                    return R.id.action_show_archived;
                }

                @Override
                public int getGroupId() {
                    return 0;
                }

                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public MenuItem setTitle(CharSequence title) {
                    return null;
                }

                @Override
                public MenuItem setTitle(int title) {
                    return null;
                }

                @Override
                public CharSequence getTitle() {
                    return null;
                }

                @Override
                public MenuItem setTitleCondensed(CharSequence title) {
                    return null;
                }

                @Override
                public CharSequence getTitleCondensed() {
                    return null;
                }

                @Override
                public MenuItem setIcon(Drawable icon) {
                    return null;
                }

                @Override
                public MenuItem setIcon(int iconRes) {
                    return null;
                }

                @Override
                public Drawable getIcon() {
                    return null;
                }

                @Override
                public MenuItem setIntent(Intent intent) {
                    return null;
                }

                @Override
                public Intent getIntent() {
                    return null;
                }

                @Override
                public MenuItem setShortcut(char numericChar, char alphaChar) {
                    return null;
                }

                @Override
                public MenuItem setNumericShortcut(char numericChar) {
                    return null;
                }

                @Override
                public char getNumericShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setAlphabeticShortcut(char alphaChar) {
                    return null;
                }

                @Override
                public char getAlphabeticShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setCheckable(boolean checkable) {
                    return null;
                }

                @Override
                public boolean isCheckable() {
                    return false;
                }

                @Override
                public MenuItem setChecked(boolean checked) {
                    return null;
                }

                @Override
                public boolean isChecked() {
                    return false;
                }

                @Override
                public MenuItem setVisible(boolean visible) {
                    return null;
                }

                @Override
                public boolean isVisible() {
                    return false;
                }

                @Override
                public MenuItem setEnabled(boolean enabled) {
                    return null;
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public boolean hasSubMenu() {
                    return false;
                }

                @Override
                public SubMenu getSubMenu() {
                    return null;
                }

                @Override
                public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
                    return null;
                }

                @Override
                public ContextMenu.ContextMenuInfo getMenuInfo() {
                    return null;
                }

                @Override
                public void setShowAsAction(int actionEnum) {

                }

                @Override
                public MenuItem setShowAsActionFlags(int actionEnum) {
                    return null;
                }

                @Override
                public MenuItem setActionView(View view) {
                    return null;
                }

                @Override
                public MenuItem setActionView(int resId) {
                    return null;
                }

                @Override
                public View getActionView() {
                    return null;
                }

                @Override
                public MenuItem setActionProvider(ActionProvider actionProvider) {
                    return null;
                }

                @Override
                public ActionProvider getActionProvider() {
                    return null;
                }

                @Override
                public boolean expandActionView() {
                    return false;
                }

                @Override
                public boolean collapseActionView() {
                    return false;
                }

                @Override
                public boolean isActionViewExpanded() {
                    return false;
                }

                @Override
                public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
                    return null;
                }
            };
            fragment.onOptionsItemSelected(menuItem);
        });
        onView(withId(R.id.archived_items_container))
                .check(matches(
                        isDisplayed()
                ));
    }

    @Test
    public void opens_new_account_activity() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class);
        scenario.onFragment(fragment -> {
            fragment.onFABClick(null);
        });
        onView(withId(R.id.toolbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_new))
                ));
    }

    @Test
    public void interface_click_method_opens_account_details() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class);
        scenario.onFragment(fragment -> {
            Account account = new Account();
            account.setId(1);
            fragment.onItemInteraction(account);
        });
        onView(withId(R.id.toolbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_details))
                ));
    }

    @Test
    public void interface_menu_method_opens_account_details() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class);
        scenario.onFragment(fragment -> {
            MenuItem menuItem = new MenuItem() {
                @Override
                public int getItemId() {
                    return R.id.account_card_details;
                }

                @Override
                public int getGroupId() {
                    return 0;
                }

                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public MenuItem setTitle(CharSequence title) {
                    return null;
                }

                @Override
                public MenuItem setTitle(int title) {
                    return null;
                }

                @Override
                public CharSequence getTitle() {
                    return null;
                }

                @Override
                public MenuItem setTitleCondensed(CharSequence title) {
                    return null;
                }

                @Override
                public CharSequence getTitleCondensed() {
                    return null;
                }

                @Override
                public MenuItem setIcon(Drawable icon) {
                    return null;
                }

                @Override
                public MenuItem setIcon(int iconRes) {
                    return null;
                }

                @Override
                public Drawable getIcon() {
                    return null;
                }

                @Override
                public MenuItem setIntent(Intent intent) {
                    return null;
                }

                @Override
                public Intent getIntent() {
                    return null;
                }

                @Override
                public MenuItem setShortcut(char numericChar, char alphaChar) {
                    return null;
                }

                @Override
                public MenuItem setNumericShortcut(char numericChar) {
                    return null;
                }

                @Override
                public char getNumericShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setAlphabeticShortcut(char alphaChar) {
                    return null;
                }

                @Override
                public char getAlphabeticShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setCheckable(boolean checkable) {
                    return null;
                }

                @Override
                public boolean isCheckable() {
                    return false;
                }

                @Override
                public MenuItem setChecked(boolean checked) {
                    return null;
                }

                @Override
                public boolean isChecked() {
                    return false;
                }

                @Override
                public MenuItem setVisible(boolean visible) {
                    return null;
                }

                @Override
                public boolean isVisible() {
                    return false;
                }

                @Override
                public MenuItem setEnabled(boolean enabled) {
                    return null;
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public boolean hasSubMenu() {
                    return false;
                }

                @Override
                public SubMenu getSubMenu() {
                    return null;
                }

                @Override
                public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
                    return null;
                }

                @Override
                public ContextMenu.ContextMenuInfo getMenuInfo() {
                    return null;
                }

                @Override
                public void setShowAsAction(int actionEnum) {

                }

                @Override
                public MenuItem setShowAsActionFlags(int actionEnum) {
                    return null;
                }

                @Override
                public MenuItem setActionView(View view) {
                    return null;
                }

                @Override
                public MenuItem setActionView(int resId) {
                    return null;
                }

                @Override
                public View getActionView() {
                    return null;
                }

                @Override
                public MenuItem setActionProvider(ActionProvider actionProvider) {
                    return null;
                }

                @Override
                public ActionProvider getActionProvider() {
                    return null;
                }

                @Override
                public boolean expandActionView() {
                    return false;
                }

                @Override
                public boolean collapseActionView() {
                    return false;
                }

                @Override
                public boolean isActionViewExpanded() {
                    return false;
                }

                @Override
                public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
                    return null;
                }
            };
            Account account = new Account();
            account.setId(1);
            fragment.onItemMenuItemInteraction(account, menuItem);
        });
        onView(withId(R.id.toolbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_details))
                ));
    }

    @Test
    public void interface_menu_method_opens_account_modify() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class);
        scenario.onFragment(fragment -> {
            MenuItem menuItem = new MenuItem() {
                @Override
                public int getItemId() {
                    return R.id.account_card_modify;
                }

                @Override
                public int getGroupId() {
                    return 0;
                }

                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public MenuItem setTitle(CharSequence title) {
                    return null;
                }

                @Override
                public MenuItem setTitle(int title) {
                    return null;
                }

                @Override
                public CharSequence getTitle() {
                    return null;
                }

                @Override
                public MenuItem setTitleCondensed(CharSequence title) {
                    return null;
                }

                @Override
                public CharSequence getTitleCondensed() {
                    return null;
                }

                @Override
                public MenuItem setIcon(Drawable icon) {
                    return null;
                }

                @Override
                public MenuItem setIcon(int iconRes) {
                    return null;
                }

                @Override
                public Drawable getIcon() {
                    return null;
                }

                @Override
                public MenuItem setIntent(Intent intent) {
                    return null;
                }

                @Override
                public Intent getIntent() {
                    return null;
                }

                @Override
                public MenuItem setShortcut(char numericChar, char alphaChar) {
                    return null;
                }

                @Override
                public MenuItem setNumericShortcut(char numericChar) {
                    return null;
                }

                @Override
                public char getNumericShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setAlphabeticShortcut(char alphaChar) {
                    return null;
                }

                @Override
                public char getAlphabeticShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setCheckable(boolean checkable) {
                    return null;
                }

                @Override
                public boolean isCheckable() {
                    return false;
                }

                @Override
                public MenuItem setChecked(boolean checked) {
                    return null;
                }

                @Override
                public boolean isChecked() {
                    return false;
                }

                @Override
                public MenuItem setVisible(boolean visible) {
                    return null;
                }

                @Override
                public boolean isVisible() {
                    return false;
                }

                @Override
                public MenuItem setEnabled(boolean enabled) {
                    return null;
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public boolean hasSubMenu() {
                    return false;
                }

                @Override
                public SubMenu getSubMenu() {
                    return null;
                }

                @Override
                public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
                    return null;
                }

                @Override
                public ContextMenu.ContextMenuInfo getMenuInfo() {
                    return null;
                }

                @Override
                public void setShowAsAction(int actionEnum) {

                }

                @Override
                public MenuItem setShowAsActionFlags(int actionEnum) {
                    return null;
                }

                @Override
                public MenuItem setActionView(View view) {
                    return null;
                }

                @Override
                public MenuItem setActionView(int resId) {
                    return null;
                }

                @Override
                public View getActionView() {
                    return null;
                }

                @Override
                public MenuItem setActionProvider(ActionProvider actionProvider) {
                    return null;
                }

                @Override
                public ActionProvider getActionProvider() {
                    return null;
                }

                @Override
                public boolean expandActionView() {
                    return false;
                }

                @Override
                public boolean collapseActionView() {
                    return false;
                }

                @Override
                public boolean isActionViewExpanded() {
                    return false;
                }

                @Override
                public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
                    return null;
                }
            };
            Account account = new Account();
            account.setId(1);
            fragment.onItemMenuItemInteraction(account, menuItem);
        });
        onView(withId(R.id.toolbar))
                .check(matches(hasDescendant(
                        withText(R.string.account_card_modify))
                ));
    }

    @Test
    public void interface_menu_method_opens_account_transactions() {
        // TODO: Implement when Transactions module is done
        assert(true);
    }

    @Test
    public void interface_menu_method_opens_archive_dialog() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme, null);
        scenario.onFragment(fragment -> {
            MenuItem menuItem = new MenuItem() {
                @Override
                public int getItemId() {
                    return R.id.account_card_archive;
                }

                @Override
                public int getGroupId() {
                    return 0;
                }

                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public MenuItem setTitle(CharSequence title) {
                    return null;
                }

                @Override
                public MenuItem setTitle(int title) {
                    return null;
                }

                @Override
                public CharSequence getTitle() {
                    return null;
                }

                @Override
                public MenuItem setTitleCondensed(CharSequence title) {
                    return null;
                }

                @Override
                public CharSequence getTitleCondensed() {
                    return null;
                }

                @Override
                public MenuItem setIcon(Drawable icon) {
                    return null;
                }

                @Override
                public MenuItem setIcon(int iconRes) {
                    return null;
                }

                @Override
                public Drawable getIcon() {
                    return null;
                }

                @Override
                public MenuItem setIntent(Intent intent) {
                    return null;
                }

                @Override
                public Intent getIntent() {
                    return null;
                }

                @Override
                public MenuItem setShortcut(char numericChar, char alphaChar) {
                    return null;
                }

                @Override
                public MenuItem setNumericShortcut(char numericChar) {
                    return null;
                }

                @Override
                public char getNumericShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setAlphabeticShortcut(char alphaChar) {
                    return null;
                }

                @Override
                public char getAlphabeticShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setCheckable(boolean checkable) {
                    return null;
                }

                @Override
                public boolean isCheckable() {
                    return false;
                }

                @Override
                public MenuItem setChecked(boolean checked) {
                    return null;
                }

                @Override
                public boolean isChecked() {
                    return false;
                }

                @Override
                public MenuItem setVisible(boolean visible) {
                    return null;
                }

                @Override
                public boolean isVisible() {
                    return false;
                }

                @Override
                public MenuItem setEnabled(boolean enabled) {
                    return null;
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public boolean hasSubMenu() {
                    return false;
                }

                @Override
                public SubMenu getSubMenu() {
                    return null;
                }

                @Override
                public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
                    return null;
                }

                @Override
                public ContextMenu.ContextMenuInfo getMenuInfo() {
                    return null;
                }

                @Override
                public void setShowAsAction(int actionEnum) {

                }

                @Override
                public MenuItem setShowAsActionFlags(int actionEnum) {
                    return null;
                }

                @Override
                public MenuItem setActionView(View view) {
                    return null;
                }

                @Override
                public MenuItem setActionView(int resId) {
                    return null;
                }

                @Override
                public View getActionView() {
                    return null;
                }

                @Override
                public MenuItem setActionProvider(ActionProvider actionProvider) {
                    return null;
                }

                @Override
                public ActionProvider getActionProvider() {
                    return null;
                }

                @Override
                public boolean expandActionView() {
                    return false;
                }

                @Override
                public boolean collapseActionView() {
                    return false;
                }

                @Override
                public boolean isActionViewExpanded() {
                    return false;
                }

                @Override
                public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
                    return null;
                }
            };
            fragment.onItemMenuItemInteraction(new Account(), menuItem);
        });
        onView(withText(R.string.account_card_archive))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.account_card_archive_info))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void interface_menu_method_opens_delete_dialog() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme, null);
        scenario.onFragment(fragment -> {
            MenuItem menuItem = new MenuItem() {
                @Override
                public int getItemId() {
                    return R.id.account_card_delete;
                }

                @Override
                public int getGroupId() {
                    return 0;
                }

                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public MenuItem setTitle(CharSequence title) {
                    return null;
                }

                @Override
                public MenuItem setTitle(int title) {
                    return null;
                }

                @Override
                public CharSequence getTitle() {
                    return null;
                }

                @Override
                public MenuItem setTitleCondensed(CharSequence title) {
                    return null;
                }

                @Override
                public CharSequence getTitleCondensed() {
                    return null;
                }

                @Override
                public MenuItem setIcon(Drawable icon) {
                    return null;
                }

                @Override
                public MenuItem setIcon(int iconRes) {
                    return null;
                }

                @Override
                public Drawable getIcon() {
                    return null;
                }

                @Override
                public MenuItem setIntent(Intent intent) {
                    return null;
                }

                @Override
                public Intent getIntent() {
                    return null;
                }

                @Override
                public MenuItem setShortcut(char numericChar, char alphaChar) {
                    return null;
                }

                @Override
                public MenuItem setNumericShortcut(char numericChar) {
                    return null;
                }

                @Override
                public char getNumericShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setAlphabeticShortcut(char alphaChar) {
                    return null;
                }

                @Override
                public char getAlphabeticShortcut() {
                    return 0;
                }

                @Override
                public MenuItem setCheckable(boolean checkable) {
                    return null;
                }

                @Override
                public boolean isCheckable() {
                    return false;
                }

                @Override
                public MenuItem setChecked(boolean checked) {
                    return null;
                }

                @Override
                public boolean isChecked() {
                    return false;
                }

                @Override
                public MenuItem setVisible(boolean visible) {
                    return null;
                }

                @Override
                public boolean isVisible() {
                    return false;
                }

                @Override
                public MenuItem setEnabled(boolean enabled) {
                    return null;
                }

                @Override
                public boolean isEnabled() {
                    return false;
                }

                @Override
                public boolean hasSubMenu() {
                    return false;
                }

                @Override
                public SubMenu getSubMenu() {
                    return null;
                }

                @Override
                public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
                    return null;
                }

                @Override
                public ContextMenu.ContextMenuInfo getMenuInfo() {
                    return null;
                }

                @Override
                public void setShowAsAction(int actionEnum) {

                }

                @Override
                public MenuItem setShowAsActionFlags(int actionEnum) {
                    return null;
                }

                @Override
                public MenuItem setActionView(View view) {
                    return null;
                }

                @Override
                public MenuItem setActionView(int resId) {
                    return null;
                }

                @Override
                public View getActionView() {
                    return null;
                }

                @Override
                public MenuItem setActionProvider(ActionProvider actionProvider) {
                    return null;
                }

                @Override
                public ActionProvider getActionProvider() {
                    return null;
                }

                @Override
                public boolean expandActionView() {
                    return false;
                }

                @Override
                public boolean collapseActionView() {
                    return false;
                }

                @Override
                public boolean isActionViewExpanded() {
                    return false;
                }

                @Override
                public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
                    return null;
                }
            };
            fragment.onItemMenuItemInteraction(new Account(), menuItem);
        });
        onView(withText(R.string.account_card_delete))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.account_card_delete_warning))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void total_balance_is_shown() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class, null, R.style.AppTheme, null);
        List<AccountWithBalance> accounts = new ArrayList<>();

        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(withId(R.id.total_balance))
                .check(matches(
                        withText("$0.00"))
                );
        onView(withId(R.id.total_projected))
                .check(matches(
                        withText("---"))
                );

        accounts.add(accountsWithBalance[0]);
        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(withId(R.id.total_balance))
                .check(matches(
                        withText("$20.00"))
                );
        onView(withId(R.id.total_projected))
                .check(matches(
                        withText("$30.00"))
                );

        accounts.add(accountsWithBalance[1]);
        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(withId(R.id.total_balance))
                .check(matches(
                        withText("$60.00"))
                );
        onView(withId(R.id.total_projected))
                .check(matches(
                        withText("$80.00"))
                );
    }

    @Test
    public void account_information_is_displayed_in_card() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(
                AccountsFragment.class, null, R.style.AppTheme, null);
        List<AccountWithBalance> accounts = new ArrayList<>();
        accounts.add(accountsWithBalance[0]);

        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_name)
        ).check(matches(withText("Test account 1")));
        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_current)
        ).check(matches(withText("$20.00")));
        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_projected)
        ).check(matches(withText("$30.00")));

        accounts.clear();
        accounts.add(accountsWithBalance[1]);
        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_name)
        ).check(matches(withText("Test account 2")));
        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_current)
        ).check(matches(withText("$40.00")));
        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_projected)
        ).check(matches(withText("$50.00")));
    }

    @Test
    public void account_icon_in_card_is_correct() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(
                AccountsFragment.class, null, R.style.AppTheme, null);
        List<AccountWithBalance> accounts = new ArrayList<>();
        accounts.add(accountsWithBalance[0]);

        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_icon)
        ).check(matches(
                withTagValue(
                        equalTo(R.drawable.ic_account_balance_black_24dp)
                )));

        accounts.clear();
        accounts.add(accountsWithBalance[1]);
        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });
        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_icon)
        ).check(matches(
                withTagValue(
                        equalTo(R.drawable.ic_account_balance_piggy_black_24dp)
                )));

        accounts.clear();
        accounts.add(accountsWithBalance[2]);
        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_icon)
        ).check(matches(
                withTagValue(
                        equalTo(R.drawable.ic_account_balance_wallet_black_24dp)
                )));

        accounts.clear();
        accounts.add(accountsWithBalance[3]);
        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_icon)
        ).check(matches(
                withTagValue(
                        equalTo(R.drawable.ic_account_balance_credit_card_black_24dp)
                )));

        accounts.clear();
        accounts.add(accountsWithBalance[4]);
        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_icon)
        ).check(matches(
                withTagValue(
                        equalTo(R.drawable.ic_account_balance_savings_black_24dp)
                )));

        accounts.clear();
        accounts.add(accountsWithBalance[5]);
        scenario.onFragment(fragment -> {
            fragment.onAccountsListChanged(accounts);
        });

        onView(
                withRecyclerView(R.id.items_list)
                        .atPositionOnView(0, R.id.account_card_icon)
        ).check(matches(
                withTagValue(
                        equalTo(R.drawable.ic_account_balance_inversion_black_24dp)
                )));
    }
}
