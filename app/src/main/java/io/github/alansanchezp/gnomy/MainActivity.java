package io.github.alansanchezp.gnomy;

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
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.ui.GnomyActivity;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.customView.MonthToolbarView;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.util.android.ViewTintingUtil;
import io.github.alansanchezp.gnomy.viewmodel.customView.MonthToolbarViewModel;

// TODO: Add Javadoc comments to project
// TODO: Write README.md contents
// TODO: Handle dark mode
// These TODOs are placed here just because MainActivity acts as a "root" file
// even if they are not related to the class
public class MainActivity
        extends GnomyActivity
        implements MainNavigationFragment.MainNavigationInteractionInterface {
    private static final String TAG_ACTIVE_FRAGMENT = "MainActivity.ActiveFragment";
    private static final int
            SUMMARY_FRAGMENT_INDEX = 1,
            TRANSACTIONS_FRAGMENT_INDEX = 2,
            ACCOUNTS_FRAGMENT_INDEX = 3,
            NOTIFICATIONS_FRAGMENT_INDEX = 4;
    private int mCurrentFragmentIndex = 0;

    private MonthToolbarView mMonthBar;
    private SingleClickViewHolder<FloatingActionButton> mFABVH;
    private MonthToolbarViewModel mViewModel;

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

        mMonthBar = findViewById(R.id.monthtoolbar);
        mFABVH = new SingleClickViewHolder<>(findViewById(R.id.main_floating_action_button));
        mFABVH.setOnClickListener(this::onFABClick);

        mViewModel = new ViewModelProvider(
                this,
                new SavedStateViewModelFactory(
                        getApplication(),
                        this
                )).get(MonthToolbarViewModel.class);
        mMonthBar.setViewModel(mViewModel);

        BottomNavigationView navigation = findViewById(R.id.navigation);
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
                .replace(R.id.main_container, fragment, TAG_ACTIVE_FRAGMENT)
                .commit(), 230);

        return true;
    }

    public void onFABClick(View v) {
        MainNavigationFragment currentFragment = (MainNavigationFragment) getSupportFragmentManager().findFragmentByTag(TAG_ACTIVE_FRAGMENT);
        if (currentFragment == null) return;

        currentFragment.onFABClick(v);
    }

    public void onFragmentChanged(int index) {
        mCurrentFragmentIndex = index;
    }

    public LiveData<YearMonth> getActiveMonth() {
        return mViewModel.activeMonth;
    }

    public void tintNavigationElements(int themeColor) {
        setThemeColor(themeColor);
        int darkVariant =  ColorUtil.getDarkVariant(themeColor);

        if (mMonthBar.isVisible()) mMonthBar.tintElements(themeColor);
        mFABVH.onView(v -> {
            if (v.getVisibility() == View.VISIBLE) {
                ViewTintingUtil.tintFAB(v, darkVariant, mThemeTextColor);
            }
        });
    }

    public void toggleOptionalNavigationElements(boolean showOptionalElements) {
        mMonthBar.toggleVisibility(showOptionalElements);
        if (showOptionalElements) {
            mFABVH.onView(v -> v.setVisibility(View.VISIBLE));
        } else {
            mFABVH.onView(v -> v.setVisibility(View.INVISIBLE));
        }
    }
}
