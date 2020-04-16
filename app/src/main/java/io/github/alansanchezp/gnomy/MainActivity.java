package io.github.alansanchezp.gnomy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.Menu;
import android.view.View;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.ui.account.NewAccountActivity;

public class MainActivity extends AppCompatActivity implements AccountsFragment.OnListFragmentInteractionListener {
    private static final String FRAGMENT_TAG = "GNOMY_MAIN_FRAGMENT";
    private Menu menu;
    private String currentTitle;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    currentTitle = getString(R.string.title_home);
                    break;
                case R.id.navigation_transactions:
                    currentTitle = getString(R.string.title_transactions);
                    break;
                case R.id.navigation_accounts:
                    currentTitle = getString(R.string.title_accounts);
                    break;
                case R.id.navigation_notifications:
                    currentTitle = getString(R.string.title_notifications);
                    break;
                default:
                    currentTitle = "";
                    return false;
            }
            return switchFragment();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentTitle = getResources().getString(R.string.title_home);
        setTitle();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_activity_toolbar, menu);
        tintMenuItems();
        toggleTransactionActions();
        return true;
    }

    private void toggleTransactionActions() {
        if (currentTitle.equals(getString(R.string.title_transactions))) {
            menu.findItem(R.id.action_search).setVisible(true);
            menu.findItem(R.id.action_filter).setVisible(true);
        } else {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_filter).setVisible(false);
        }
    }

    private void tintMenuItems() {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem filterItem = menu.findItem(R.id.action_filter);

        Drawable normalDrawable = searchItem.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, this.getResources().getColor(R.color.colorTextInverse));

        normalDrawable = filterItem.getIcon();
        wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, this.getResources().getColor(R.color.colorTextInverse));
    }

    public boolean switchFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment;

        // TODO: Move this two lines right before return statement when all fragments are handled
        setTitle();
        toggleTransactionActions();

        if (currentTitle.equals(getResources().getString(R.string.title_accounts))) {
            fragment = AccountsFragment.newInstance(1);
        } else {
            return true;
        }

        manager.beginTransaction()
                .replace(R.id.main_container, fragment, FRAGMENT_TAG)
                .commit();

        return true;
    }

    private void setTitle() {
        getSupportActionBar().setTitle(currentTitle);
    }

    public void onFABClick(View v) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (currentFragment == null) return;

        if (currentFragment instanceof AccountsFragment) {
            Intent newAccountIntent = new Intent(MainActivity.this, NewAccountActivity.class);
            MainActivity.this.startActivity(newAccountIntent);
        }
    }


    public void onListFragmentInteraction(Account account) {
    }

    public boolean onListFragmentMenuItemInteraction(Account account, MenuItem menuItem) {
        return true;
    }
}
