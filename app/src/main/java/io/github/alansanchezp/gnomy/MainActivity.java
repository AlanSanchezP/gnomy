package io.github.alansanchezp.gnomy;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.View;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import androidx.lifecycle.ViewModelProvider;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import io.github.alansanchezp.gnomy.databinding.ActivityMainBinding;
import io.github.alansanchezp.gnomy.ui.GnomyActivity;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.androidUtil.ViewTintingUtil;
import io.github.alansanchezp.gnomy.viewmodel.MainActivityViewModel;

// TODO: [#44] Handle dark mode
// TODO: [#23] (Wishlist) implement tooltips for non-menu buttons
// TODO: [#47] Animations
// These TODOs are placed here just because MainActivity acts as a "root" file
// even if they are not related to the class

/**
 * Activity for main screen.
 */
public class MainActivity
        extends GnomyActivity<ActivityMainBinding> {

    private SingleClickViewHolder<FloatingActionButton> mFABVH;
    private MainActivityViewModel mViewModel;

    public MainActivity() {
        super(R.menu.main_activity_toolbar, ActivityMainBinding::inflate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFABVH = new SingleClickViewHolder<>($.mainFloatingActionButton);
        mFABVH.setOnClickListener(this::onFABClick);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_container);
        NavController navController = Objects.requireNonNull(host).getNavController();
        NavigationUI.setupWithNavController(navigation, navController);
        navigation.setOnNavigationItemReselectedListener(item -> {
            // Just do nothing when an item is reselected from the bottom navigation bar
        });

        mViewModel = new ViewModelProvider(this)
                .get(MainActivityViewModel.class);
        mViewModel.getThemeToUse().observe(this, this::tintNavigationElements);
        mViewModel.getShowOptionalNavigationElements().observe(this, this::toggleOptionalNavigationElements);
        mViewModel.getTitle().observe(this, this::setTitle);
        $.monthToolbar.setViewModel(mViewModel);
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

    @Override
    protected void tintNavigationBar() {
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    private void onFABClick(View v) {
        mViewModel.notifyFABClick((FloatingActionButton) v);
    }

    /**
     * Applies some base color to the Activity's layout, ignoring optional
     * elements (month toolbar and FAB) if they are not displayed.
     *
     * @param themeColor Color to apply.
     */
    private void tintNavigationElements(int themeColor) {
        setThemeColor(themeColor);
        int darkVariant =  ColorUtil.getDarkVariant(themeColor);

        if ($.monthToolbar.isVisible()) $.monthToolbar.tintElements(themeColor);
        mFABVH.onView(this, v -> {
            if (v.getVisibility() == View.VISIBLE) {
                ViewTintingUtil.tintFAB(v, darkVariant, mThemeTextColor);
            }
        });
    }

    /**
     * Toggles month toolbar and FAB visibility.
     *
     * @param showOptionalElements Indicates if these elements should be visible or not.
     */
    private void toggleOptionalNavigationElements(boolean showOptionalElements) {
        $.monthToolbar.toggleVisibility(showOptionalElements);
        if (showOptionalElements) {
            mFABVH.onView(this, v -> v.setVisibility(View.VISIBLE));
        } else {
            mFABVH.onView(this, v -> v.setVisibility(View.INVISIBLE));
        }
    }
}
