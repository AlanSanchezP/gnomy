package io.github.alansanchezp.gnomy;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.annotation.Nullable;
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

import java.util.Calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.ui.BaseMainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity
        implements BaseMainNavigationFragment.MainNavigationInteractionInterface {
    private static final String FRAGMENT_TAG = "GNOMY_MAIN_FRAGMENT";
    private static final int
            SUMMARY_FRAGMENT_INDEX = 1,
            TRANSACTIONS_FRAGMENT_INDEX = 2,
            ACCOUNTS_FRAGMENT_INDEX = 3,
            NOTIFICATIONS_FRAGMENT_INDEX = 4;
    private int mCurrentFragmentIndex = 0;
    private MainActivityViewModel mViewModel;

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

        mMainBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mMainBar);

        mViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(MainActivityViewModel.class);
        mViewModel.getPublicMonthFilter().observe(this, new Observer<YearMonth>() {
            @Override
            public void onChanged(@Nullable final YearMonth month) {
                updateMonth(month);
            }
        });

        mSecondaryBar = (Toolbar) findViewById(R.id.toolbar2);
        mFAB = (FloatingActionButton) findViewById(R.id.main_floating_action_button);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFAB.setEnabled(true);
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
                fragment = AccountsFragment.newInstance(1, newIndex, mViewModel.getMonth());
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
        mFAB.setEnabled(false);
    }

    public void onPreviousMonthClick(View v) {
        mViewModel.setMonth(mViewModel.getMonth().minusMonths(1));
    }

    public void onNextMonthClick(View v) {
        mViewModel.setMonth(mViewModel.getMonth().plusMonths(1));
    }

    public void onMonthPickerClick(View v) {
        // TODO: Implement (when possible) a better looking calendar
        // Current limitation is that open source libraries
        // implementing material design do not support
        // a range limit, causing conflicts with
        // gnomy's inability to handle future balances
        Calendar calendar = Calendar.getInstance();
        int yearSelected = mViewModel.getMonth().getYear();
        int monthSelected = mViewModel.getMonth().getMonthValue();

        // Month representation here ranges from 0 to 11,
        // thus requiring +1 and -1 operations
        calendar.clear();
        calendar.set(YearMonth.now().getYear(), YearMonth.now().getMonthValue()-1, 1);
        long maxDate = calendar.getTimeInMillis();

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected-1, yearSelected, 0, maxDate);

        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                mViewModel.setMonth(YearMonth.of(year, monthOfYear+1));
            }
        });

        dialogFragment.show(getSupportFragmentManager(), null);
    }

    public void onReturnToCurrentMonthClick(View v) {
        mViewModel.setMonth(YearMonth.now());
    }

    private void updateMonth(YearMonth month) {
        TextView monthTextView = (TextView) findViewById(R.id.month_name_view);
        ImageButton nextMonthBtn = (ImageButton) findViewById(R.id.next_month_btn);
        ImageButton calendarBtn =  (ImageButton) findViewById(R.id.select_total_month);
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
            calendarBtn.setVisibility(View.GONE);
        } else {
            nextMonthBtn.setVisibility(View.VISIBLE);
            calendarBtn.setVisibility(View.VISIBLE);
        }

        monthTextView.setText(monthString);
    }

    public void onFragmentChanged(int index) {
        mCurrentFragmentIndex = index;
    }

    public LiveData<YearMonth> getMonthFilter() {
        return mViewModel.getPublicMonthFilter();
    }

    public void tintAppbars(int mainColor, boolean showSecondaryToolbar) {
        int textColor = ColorUtil.getTextColor(mainColor);
        int darkVariant =  ColorUtil.getDarkVariant(mainColor);

        getWindow().setStatusBarColor(darkVariant);

        mMainBar.setBackgroundColor(mainColor);
        mMainBar.setTitleTextColor(textColor);
        mMainBar.getOverflowIcon().setTint(textColor);

        mSecondaryBar.setVisibility(View.GONE);

        if (showSecondaryToolbar) {
            mSecondaryBar.setVisibility(View.VISIBLE);
            mSecondaryBar.setBackgroundColor(mainColor);
        }

        mFAB.setBackgroundTintList(ColorStateList.valueOf(darkVariant));
        mFAB.getDrawable().mutate().setTint(textColor);
    }
}
