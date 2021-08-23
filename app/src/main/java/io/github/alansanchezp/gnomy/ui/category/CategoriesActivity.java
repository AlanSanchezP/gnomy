package io.github.alansanchezp.gnomy.ui.category;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.androidUtil.ViewTintingUtil;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.databinding.ActivityCategoriesBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CategoriesActivity
        extends BackButtonActivity<ActivityCategoriesBinding> {

    private TabLayout mTabLayout;
    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()) {
                case 0:
                    CategoriesActivity.this.setThemeColor(getResources().getColor(R.color.colorExpenses));
                    break;
                case 1:
                    CategoriesActivity.this.setThemeColor(getResources().getColor(R.color.colorIncomes));
                    break;
                case 2:
                    CategoriesActivity.this.setThemeColor(getResources().getColor(R.color.colorPrimary));
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    public CategoriesActivity() {
        super(null, false, ActivityCategoriesBinding::inflate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.categories_title);

        ViewPager2 viewPager = $.pager;
        CategoryCollectionAdapter categoryCollectionAdapter = new CategoryCollectionAdapter(this);
        viewPager.setAdapter(categoryCollectionAdapter);

        mTabLayout = $.tabLayout;
        mTabLayout.addOnTabSelectedListener(onTabSelectedListener);
        new TabLayoutMediator(mTabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.category_type_expenses);
                        break;
                        case 1:
                            tab.setText(R.string.category_type_incomes);
                            break;
                        case 2:
                            tab.setText(R.string.category_type_both);
                            break;
                    }
                }
        ).attach();

        SingleClickViewHolder<FloatingActionButton> fab = new SingleClickViewHolder<>($.categoriesFab);
        fab.setOnClickListener(this::onFABClick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTabLayout.removeOnTabSelectedListener(onTabSelectedListener);
    }

    @Override
    protected void tintNavigationBar() {
        super.tintNavigationBar();
        mTabLayout.setBackgroundColor(mThemeColor);
        // TODO: Make not-selected options have a different color, similar to WhatsApp implementation
        mTabLayout.setTabTextColors(mThemeTextColor, mThemeTextColor);
        mTabLayout.setSelectedTabIndicatorColor(mThemeTextColor);
        ViewTintingUtil.tintFAB($.categoriesFab, mThemeColor, mThemeTextColor);
    }

    private void onFABClick(View v) {
        Intent newCategoryIntent = new Intent(this, AddEditCategoryActivity.class);
        newCategoryIntent.putExtra(AddEditCategoryActivity.EXTRA_CATEGORY_TYPE, mTabLayout.getSelectedTabPosition() + 1);
        startActivity(newCategoryIntent);
    }

    public static class CategoryCollectionAdapter extends FragmentStateAdapter {
        public CategoryCollectionAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            CategoriesFragment fragment = new CategoriesFragment();
            Bundle args = new Bundle();
            switch (position) {
                case 0:
                    args.putInt(CategoriesFragment.ARG_CATEGORY_TYPE, Category.EXPENSE_CATEGORY);
                    break;
                case 1:
                    args.putInt(CategoriesFragment.ARG_CATEGORY_TYPE, Category.INCOME_CATEGORY);
                    break;
                case 2:
                    args.putInt(CategoriesFragment.ARG_CATEGORY_TYPE, Category.SHARED_CATEGORY);
                    break;
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}