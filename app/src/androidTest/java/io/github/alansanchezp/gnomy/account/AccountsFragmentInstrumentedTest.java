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

import java.time.YearMonth;

import androidx.fragment.app.testing.FragmentScenario;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;

import androidx.lifecycle.MutableLiveData;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.MockDatabaseOperationsUtil;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.util.DateUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
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
public class AccountsFragmentInstrumentedTest {
    // Needed so that ViewModel instance doesn't crash
    @BeforeClass
    public static void init_mocks() {
        final MockDatabaseOperationsUtil.MockableAccountDAO mockAccountDAO = mock(MockDatabaseOperationsUtil.MockableAccountDAO.class);
        final MockDatabaseOperationsUtil.MockableMonthlyBalanceDAO mockBalanceDAO = mock(MockDatabaseOperationsUtil.MockableMonthlyBalanceDAO.class);
        MockDatabaseOperationsUtil.setAccountDAO(mockAccountDAO);
        MockDatabaseOperationsUtil.setBalanceDAO(mockBalanceDAO);
        when(mockAccountDAO.getArchivedAccounts())
                .thenReturn(new MutableLiveData<>());
        when(mockAccountDAO.find(anyInt()))
                .thenReturn(new MutableLiveData<>());
        when(mockBalanceDAO.getAccumulatedFromMonth(anyInt(), any(YearMonth.class)))
                .thenReturn(new MutableLiveData<>());
    }

    @Test
    public void dynamic_balance_labels_are_correct() {
        FragmentScenario<AccountsFragment> scenario = launchInContainer(AccountsFragment.class,
                null, R.style.AppTheme, null);

        scenario.onFragment(fragment ->
                fragment.onMonthChanged(DateUtil.now()));

        onView(withId(R.id.total_projected_label))
                .check(matches(
                        withText(R.string.account_projected_balance)
                ));

        scenario.onFragment(fragment ->
                fragment.onMonthChanged(DateUtil.now().minusMonths(1)));

        onView(ViewMatchers.withId(R.id.total_projected_label))
                .check(matches(
                        withText(R.string.account_balance_end_of_month)
                ));

        scenario.onFragment(fragment ->
                fragment.onMonthChanged(DateUtil.now().plusMonths(1)));

        onView(ViewMatchers.withId(R.id.total_projected_label))
                .check(matches(
                        withText(R.string.account_projected_balance)
                ));
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
        scenario.onFragment(fragment ->
                fragment.onFABClick(null));
        onView(withId(R.id.custom_appbar))
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
        onView(withId(R.id.custom_appbar))
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
        onView(withId(R.id.custom_appbar))
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
        onView(withId(R.id.custom_appbar))
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
}
