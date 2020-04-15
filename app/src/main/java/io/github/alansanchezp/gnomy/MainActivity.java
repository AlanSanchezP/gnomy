package io.github.alansanchezp.gnomy;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.ui.account.NewAccountActivity;

public class MainActivity extends AppCompatActivity implements AccountsFragment.OnListFragmentInteractionListener {
    private static final String FRAGMENT_TAG = "GNOMY_MAIN_FRAGMENT";

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            String title = "";
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    title = getString(R.string.title_home);
                    return true;
                case R.id.navigation_transactions:
                    title = getString(R.string.title_transactions);
                    return true;
                case R.id.navigation_accounts:
                    title = getString(R.string.title_accounts);
                    fragment = AccountsFragment.newInstance(1);
                    break;
                case R.id.navigation_notifications:
                    title = getString(R.string.title_notifications);
                    return true;
                default:
                    return false;
            }
            return switchToFragment(fragment, title);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remove default title text
        setTitle(getString(R.string.title_home));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public boolean switchToFragment(Fragment fragment, String title) {
        if (fragment == null) return false;

        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .replace(R.id.main_container, fragment, FRAGMENT_TAG)
                .commit();
        setTitle(title);

        return true;
    }

    public void setTitle(String title) {
        getSupportActionBar().setTitle(title);
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
}
