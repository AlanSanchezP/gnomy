package io.github.alansanchezp.gnomy.accounts;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.YearMonth;

import androidx.fragment.app.testing.FragmentScenario;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ui.BaseMainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AccountsFragmentInstrumentedTest {
    @Test
    public void projected_balance_label_is_correct() {
        FragmentScenario scenario = launchInContainer(AccountsFragment.class);
        onView(ViewMatchers.withId(R.id.total_projected_lable))
                .check(matches(
                        withText("Projected balance")
                ));
        scenario.onFragment(fragment -> {
            BaseMainNavigationFragment customFragment = (BaseMainNavigationFragment) fragment;
            customFragment.onMonthChanged(YearMonth.now().minusMonths(1));
        });
        onView(withId(R.id.total_projected_lable))
                .check(matches(
                        withText("Accumulated balance")
                ));
    }
    @Test
    public void archived_accounts_modal_is_shown() {
        FragmentScenario scenario = launchInContainer(AccountsFragment.class);
        onView(withId(R.id.archived_items_container))
                .check(
                        doesNotExist()
                );
        scenario.onFragment(fragment -> {
            BaseMainNavigationFragment customFragment = (BaseMainNavigationFragment) fragment;
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
            customFragment.onOptionsItemSelected(menuItem);
        });
        onView(withId(R.id.archived_items_container))
                .check(matches(
                        isDisplayed()
                ));
    }
}
