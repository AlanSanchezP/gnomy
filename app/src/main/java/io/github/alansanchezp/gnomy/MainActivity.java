package io.github.alansanchezp.gnomy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.BaseMainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.ui.account.AddEditAccountActivity;

public class MainActivity extends AppCompatActivity
        implements BaseMainNavigationFragment.MainNavigationInteractionInterface,
            AccountsFragment.OnListFragmentInteractionListener {
    private static final String FRAGMENT_TAG = "GNOMY_MAIN_FRAGMENT";
    private static final int
            SUMMARY_FRAGMENT_INDEX = 1,
            TRANSACTIONS_FRAGMENT_INDEX = 2,
            ACCOUNTS_FRAGMENT_INDEX = 3,
            NOTIFICATIONS_FRAGMENT_INDEX = 4;
    private int mCurrentFragmentIndex = 0;

    private Toolbar mMainBar;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return switchFragment(SUMMARY_FRAGMENT_INDEX);
                case R.id.navigation_transactions:
                    return switchFragment(TRANSACTIONS_FRAGMENT_INDEX);
                case R.id.navigation_accounts:
                    return switchFragment(ACCOUNTS_FRAGMENT_INDEX);
                case R.id.navigation_notifications:
                    return switchFragment(NOTIFICATIONS_FRAGMENT_INDEX);
                default:
                    return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);

        setContentView(R.layout.activity_main);
        mMainBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mMainBar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public boolean switchFragment(int newIndex) {
        final FragmentManager manager = getSupportFragmentManager();
        final BaseMainNavigationFragment fragment;

        if (newIndex == mCurrentFragmentIndex) return true;

        switch (newIndex) {
            case ACCOUNTS_FRAGMENT_INDEX:
                fragment = AccountsFragment.newInstance(1, newIndex);
                break;
            default:
                return true;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.beginTransaction()
                        .replace(R.id.main_container, fragment, FRAGMENT_TAG)
                        .commit();
            }
        }, 210);

        return true;
    }

    public void onFABClick(View v) {
        BaseMainNavigationFragment currentFragment = (BaseMainNavigationFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (currentFragment == null) return;

        currentFragment.onFABClick(v);
    }

    public void onListFragmentInteraction(Account account) {
    }

    public boolean onListFragmentMenuItemInteraction(final Account account, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.account_card_details:
                break;
            case R.id.account_card_modify:
                Intent modifyAccountIntent = new Intent(MainActivity.this, AddEditAccountActivity.class);
                modifyAccountIntent.putExtra("accountId", account.getId());
                modifyAccountIntent.putExtra("accountBgColor", account.getBackgroundColor());
                modifyAccountIntent.putExtra("accountName", account.getName());
                modifyAccountIntent.putExtra("accountInitialValue", account.getInitialValue().toPlainString());
                modifyAccountIntent.putExtra("accountIncludedInSum", account.isShowInDashboard());
                modifyAccountIntent.putExtra("accountCurrency", account.getDefaultCurrency());
                modifyAccountIntent.putExtra("accountType", account.getType());

                MainActivity.this.startActivity(modifyAccountIntent);
                break;
            case R.id.account_card_transactions:
                break;
            case R.id.account_card_archive:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.account_card_archive))
                        .setMessage(getString(R.string.account_card_archive_info))
                        .setPositiveButton(getString(R.string.confirmation_dialog_yes), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AccountsFragment fragment = (AccountsFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
                                fragment.archiveAccount(account);
                            }
                        })
                        .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                        .show();
                break;
            case R.id.account_card_delete:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.account_card_delete))
                        .setMessage(getString(R.string.account_card_delete_warning))
                        .setPositiveButton(getString(R.string.confirmation_dialog_yes), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AccountsFragment fragment = (AccountsFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
                                fragment.deleteAccount(account);
                            }
                        })
                        .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                        .show();
                break;
            default:
                return false;
        }

        return true;
    }

    public void onFragmentChanged(int index) {
        mCurrentFragmentIndex = index;
    }

    public void tintAppbars(int bgColor, boolean showSecondaryToolbar) {
        mMainBar.getBackground().setTint(bgColor);
    }
}
