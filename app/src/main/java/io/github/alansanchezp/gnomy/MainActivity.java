package io.github.alansanchezp.gnomy;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.widget.TextView;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.github.alansanchezp.gnomy.database.GnomyDatabase;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_transactions:
                    return true;
                case R.id.navigation_accounts:
                    fragment = new AccountsFragment();
                    break;
                case R.id.navigation_notifications:
                    return true;
                default:
                    return false;
            }
            return switchToFragment(fragment);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);

        // Empty passphrase for development until
        // I can figure out how to properly store
        // and handle it
        // TODO Handle Passphrase
        GnomyDatabase db = GnomyDatabase
                .getInstance(this, "");

        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public boolean switchToFragment(Fragment fragment) {
        if (fragment == null) return false;

        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();

        return true;
    }
}
