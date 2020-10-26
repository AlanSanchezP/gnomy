package io.github.alansanchezp.gnomy;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.YearMonth;
import java.util.Objects;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.ui.GnomyActivity;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.customView.MonthToolbarView;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.util.ColorUtil;

// TODO: Add Javadoc comments to project
// TODO: Write README.md contents
// TODO: Use Lamda expressions and remove casting on findViewById()
//  Turns out we don't have to worry about minSDK issues as those are handled by compileSDK
// These TODOs are placed here just because MainActivity acts as a "root" file
// even if they are not related to the class
public class MainActivity
        extends GnomyActivity
        implements MainNavigationFragment.MainNavigationInteractionInterface {
    private static final String FRAGMENT_TAG = "GNOMY_MAIN_FRAGMENT";
    private static final int
            SUMMARY_FRAGMENT_INDEX = 1,
            TRANSACTIONS_FRAGMENT_INDEX = 2,
            ACCOUNTS_FRAGMENT_INDEX = 3,
            NOTIFICATIONS_FRAGMENT_INDEX = 4;
    private int mCurrentFragmentIndex = 0;

    private MonthToolbarView mMonthBar;
    private FloatingActionButton mFAB;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
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
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMonthBar = (MonthToolbarView) findViewById(R.id.monthtoolbar);
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

    @Override
    protected void tintMenuItems() {
        super.tintMenuItems();
        try {
            Objects.requireNonNull(mAppbar.getOverflowIcon())
                    .setTint(mThemeTextColor);
        } catch (NullPointerException npe) {
            Log.e("MainActivity", "tintNavigationElements: Why is menu not collapsed?", npe);
        }
    }

    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    public boolean switchFragment(int newIndex) {
        final FragmentManager manager = getSupportFragmentManager();
        final MainNavigationFragment fragment;

        if (newIndex == mCurrentFragmentIndex) return true;

        //noinspection SwitchStatementWithTooFewBranches
        switch (newIndex) {
            case ACCOUNTS_FRAGMENT_INDEX:
                fragment = AccountsFragment.newInstance(1, newIndex);
                break;
            default:
                return false;
        }

        // TODO: Is there a better way to prevent animation lag?
        //  Can this delay cause any undesired behavior?
        new Handler().postDelayed(() -> manager.beginTransaction()
                .replace(R.id.main_container, fragment, FRAGMENT_TAG)
                .commit(), 230);

        return true;
    }

    public void onFABClick(View v) {
        MainNavigationFragment currentFragment = (MainNavigationFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (currentFragment == null) return;

        currentFragment.onFABClick(v);
        mFAB.setEnabled(false);
    }

    public void onFragmentChanged(int index) {
        mCurrentFragmentIndex = index;
    }

    public LiveData<YearMonth> getActiveMonth() {
        return mMonthBar.getActiveMonth();
    }

    public void tintNavigationElements(int themeColor) {
        setThemeColor(themeColor);
        int darkVariant =  ColorUtil.getDarkVariant(themeColor);

        if (mMonthBar.isVisible()) mMonthBar.tintElements(themeColor);
        if (mFAB.getVisibility() == View.VISIBLE) {
            mFAB.setBackgroundTintList(ColorStateList.valueOf(darkVariant));
            mFAB.getDrawable().mutate().setTint(mThemeTextColor);
        }
    }

    public void toggleOptionalNavigationElements(boolean showOptionalElements) {
        mMonthBar.toggleVisibility(showOptionalElements);
        if (showOptionalElements) {
            mFAB.setVisibility(View.VISIBLE);
        } else {
            mFAB.setVisibility(View.INVISIBLE);
        }
    }
}
