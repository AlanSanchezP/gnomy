package io.github.alansanchezp.gnomy;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.Menu;
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
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.util.android.ViewTintingUtil;
import io.github.alansanchezp.gnomy.viewmodel.MainActivityViewModel;

// TODO: Add Javadoc comments to project
// TODO: Write README.md contents
// TODO: Handle dark mode
// TODO: (Wishlist) implement tooltips for non-menu buttons
// TODO: Animations
// TODO: Replace findById with viewbinding
// TODO: Improve Layout naming
// These TODOs are placed here just because MainActivity acts as a "root" file
// even if they are not related to the class
public class MainActivity
        extends GnomyActivity<ActivityMainBinding> {

    private SingleClickViewHolder<FloatingActionButton> mFABVH;
    private MainActivityViewModel mViewModel;

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
        $.monthtoolbar.setViewModel(mViewModel);
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
    private void onFABClick(View v) {
        mViewModel.notifyFABClick((FloatingActionButton) v);
    }

    private void tintNavigationElements(int themeColor) {
        setThemeColor(themeColor);
        int darkVariant =  ColorUtil.getDarkVariant(themeColor);

        if ($.monthtoolbar.isVisible()) $.monthtoolbar.tintElements(themeColor);
        mFABVH.onView(this, v -> {
            if (v.getVisibility() == View.VISIBLE) {
                ViewTintingUtil.tintFAB(v, darkVariant, mThemeTextColor);
            }
        });
    }

    private void toggleOptionalNavigationElements(boolean showOptionalElements) {
        $.monthtoolbar.toggleVisibility(showOptionalElements);
        if (showOptionalElements) {
            mFABVH.onView(this, v -> v.setVisibility(View.VISIBLE));
        } else {
            mFABVH.onView(this, v -> v.setVisibility(View.INVISIBLE));
        }
    }
}
