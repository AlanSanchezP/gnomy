package io.github.alansanchezp.gnomy;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;

import io.github.alansanchezp.gnomy.ui.BaseMainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.util.ColorUtil;

public class MainActivity extends AppCompatActivity
        implements BaseMainNavigationFragment.MainNavigationInteractionInterface {
    private static final String FRAGMENT_TAG = "GNOMY_MAIN_FRAGMENT";
    private static final int
            SUMMARY_FRAGMENT_INDEX = 1,
            TRANSACTIONS_FRAGMENT_INDEX = 2,
            ACCOUNTS_FRAGMENT_INDEX = 3,
            NOTIFICATIONS_FRAGMENT_INDEX = 4;
    private int mCurrentFragmentIndex = 0;
    private YearMonth mCurrentMonth;

    private Toolbar mMainBar;
    private Toolbar mSecondaryBar;
    private FloatingActionButton mFAB;

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
        updateMonth(YearMonth.now());

        mMainBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mMainBar);

        mSecondaryBar = (Toolbar) findViewById(R.id.toolbar2);
        mFAB = (FloatingActionButton) findViewById(R.id.main_floating_action_button);

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

    public void onPreviousMonthClick(View v) {
        updateMonth(mCurrentMonth.minusMonths(1));
    }

    public void onNextMonthClick(View v) {
        updateMonth(mCurrentMonth.plusMonths(1));
    }

    public void onCalendarClick(View v) {
        updateMonth(null);
    }

    private void updateMonth(YearMonth month) {
        if (month == null) return;

        TextView monthTextView = (TextView) findViewById(R.id.month_name_view);
        ImageButton nextMonthBtn = (ImageButton) findViewById(R.id.next_month_btn);
        String formatterPattern;
        String monthString;

        if (month.getYear() == YearMonth.now().getYear()) {
            formatterPattern = "MMMM";
        } else {
            formatterPattern = "MMMM yyyy";
        }

        monthString = month.format(DateTimeFormatter.ofPattern(formatterPattern));
        /* This is needed as spanish localization (and possibly others too)
           returns first character as lowercase */
        monthString = monthString.substring(0, 1).toUpperCase()
                + monthString.substring(1);

        /* Temporal limitation
           TODO: Handle projected balances for future months (as there is no MonthlyBalance instance for those) */
        if (month.equals(YearMonth.now())) {
            nextMonthBtn.setVisibility(View.INVISIBLE);
        } else {
            nextMonthBtn.setVisibility(View.VISIBLE);
        }

        monthTextView.setText(monthString);
        mCurrentMonth = month;

        BaseMainNavigationFragment currentFragment = (BaseMainNavigationFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (currentFragment == null) return;

        currentFragment.onMonthChanged(mCurrentMonth);
    }

    public void onFragmentChanged(int index) {
        mCurrentFragmentIndex = index;
    }

    public void tintAppbars(int mainColor, boolean showSecondaryToolbar) {
        int textColor = ColorUtil.getTextColor(mainColor);
        int darkVariant =  ColorUtil.getDarkVariant(mainColor);

        getWindow().setStatusBarColor(darkVariant);

        mMainBar.setBackgroundColor(mainColor);
        mMainBar.setTitleTextColor(textColor);
        mMainBar.getOverflowIcon().setTint(textColor);

        mSecondaryBar.setBackgroundColor(mainColor);

        mFAB.setBackgroundTintList(ColorStateList.valueOf(darkVariant));
        mFAB.getDrawable().mutate().setTint(textColor);
    }
}
