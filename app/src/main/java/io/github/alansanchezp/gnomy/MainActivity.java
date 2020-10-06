package io.github.alansanchezp.gnomy;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.threeten.bp.YearMonth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.ui.BaseMainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.MonthToolbarView;
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
    private MonthToolbarView mMonthBar;
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

        mMonthBar = (MonthToolbarView) findViewById(R.id.monthtoolbar);
        mMonthBar.setListeners(new MonthToolbarView.MonthToolbarClickListener() {
            @Override
            public void onPreviousMonthClick() {
                mViewModel.setMonth(mViewModel.getMonth().minusMonths(1));
            }

            @Override
            public void onNextMonthClick() {
                mViewModel.setMonth(mViewModel.getMonth().plusMonths(1));
            }

            @Override
            public void onReturnToCurrentMonthClick() {
                mViewModel.setMonth(YearMonth.now());
            }

            @Override
            public YearMonth onGetYearMonth() {
                return mViewModel.getMonth();
            }

            @Override
            public void onDateSet(int year, int monthOfYear) {
                mViewModel.setMonth(YearMonth.of(year, monthOfYear+1));
            }
        });

        mViewModel.getPublicMonthFilter().observe(this, new Observer<YearMonth>() {
            @Override
            public void onChanged(@Nullable final YearMonth month) {
                mMonthBar.setMonth(month);
            }
        });

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
                return false;
        }

        // TODO: Is there a better way to prevent animation lag?
        // TODO: Can this delay cause any undesired behavior?
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.beginTransaction()
                        .replace(R.id.main_container, fragment, FRAGMENT_TAG)
                        .commit();
            }
        }, 230);

        return true;
    }

    public void onFABClick(View v) {
        BaseMainNavigationFragment currentFragment = (BaseMainNavigationFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (currentFragment == null) return;

        currentFragment.onFABClick(v);
        mFAB.setEnabled(false);
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

        mMonthBar.setToolbarVisibility(showSecondaryToolbar, mainColor);

        mFAB.setBackgroundTintList(ColorStateList.valueOf(darkVariant));
        mFAB.getDrawable().mutate().setTint(textColor);
    }
}
