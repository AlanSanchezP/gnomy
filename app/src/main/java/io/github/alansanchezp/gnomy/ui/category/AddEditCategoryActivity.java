package io.github.alansanchezp.gnomy.ui.category;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.androidUtil.GnomySpinnerAdapter;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.androidUtil.ViewTintingUtil;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.databinding.ActivityAddEditCategoryBinding;
import io.github.alansanchezp.gnomy.ui.BackButtonActivity;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.viewmodel.category.AddEditCategoryViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.tiper.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.alansanchezp.gnomy.androidUtil.SimpleTextWatcherWrapper.onlyOnTextChanged;
import static io.github.alansanchezp.gnomy.ui.category.CategoryIconsRecyclerViewAdapter.DEFAULT_RES_NAME;

public class AddEditCategoryActivity
        extends BackButtonActivity<ActivityAddEditCategoryBinding>
        implements CategoryIconsRecyclerViewAdapter.OnIconSelectedListener {

    public static final String EXTRA_CATEGORY_ID = "AddEditCategoryActivity.CategoryId";
    private static final String TAG_PICKER_DIALOG = "AddEditCategoryActivity.ColorPickerDialog";
    private AddEditCategoryViewModel mAddEditCategoryViewModel;
    private CategoryIconsRecyclerViewAdapter mAdapter;
    private Category mCategory;
    private SingleClickViewHolder<ImageButton> mColorPickerBtnVH;
    private SingleClickViewHolder<FloatingActionButton> mFABVH;

    public AddEditCategoryActivity() {
        super(null, true, ActivityAddEditCategoryBinding::inflate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddEditCategoryViewModel = new ViewModelProvider(this,
                new SavedStateViewModelFactory(
                        this.getApplication(), this))
                .get(AddEditCategoryViewModel.class);

        // Prevent potential noticeable blink in color
        mAppbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        // TODO: Either dynamically show/hide FAB on landscape, block landscape altogether
        //  or find a better UI behavior for it.
        //  It makes UX unpleasant on landscape because it overlays with other elements
        mFABVH = new SingleClickViewHolder<>($.addeditCategoryFAB, true);
        mFABVH.setOnClickListener(this::processData);
        mColorPickerBtnVH = new SingleClickViewHolder<>($.addeditCategoryColorButton);
        mColorPickerBtnVH.setOnClickListener(this::showColorPicker);

        mAddEditCategoryViewModel.categoryColor.observe(this, this::onCategoryColorChanged);

        $.addeditCategoryType.setVisibility(View.GONE);

        Intent intent = getIntent();
        int categoryId = intent.getIntExtra(EXTRA_CATEGORY_ID, 0);
        LiveData<Category> categoryLD = mAddEditCategoryViewModel.getCategory(categoryId);

        String activityTitle;
        if (categoryLD != null) {
            activityTitle = getString(R.string.category_modify);
            categoryLD.observe(this, this::onCategoryChanged);
        } else {
            // TODO: Should allow to modify categories' type?
            initSpinners();
            $.addeditCategoryType.setVisibility(View.VISIBLE);
            activityTitle = getString(R.string.category_new);
            mCategory = new Category();
            // Only generate new color if viewModel doesn't have one stored already
            if (Objects.requireNonNull(mAddEditCategoryViewModel.categoryColor.getValue()) == 1) {
                mAddEditCategoryViewModel.setCategoryColor(ColorUtil.getRandomColor());
            }
        }

        setTitle(activityTitle);

        $.addeditCategoryNameInput.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onCategoryNameEditTextChanges(s.toString())));

        // TODO: How to test the icon picker?
        mAdapter = new CategoryIconsRecyclerViewAdapter(this);
        RecyclerView recyclerView = $.iconsGrid;
        recyclerView.setLayoutManager(new GridLayoutManager($.getRoot().getContext(), 6));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        if (categoryLD == null) {
            mAdapter.setInitialIcon(DEFAULT_RES_NAME);
            onIconSelected(DEFAULT_RES_NAME);
        }
    }

    @Override
    protected void disableActions() {
        super.disableActions();
        mAdapter.disableClicks();
        mFABVH.blockClicks();
    }

    @Override
    protected void enableActions() {
        super.enableActions();
        mAdapter.enableClicks();
        mFABVH.allowClicks();
    }

    private void onCategoryChanged(Category category) {
        if (category == null) {
            Log.e("AddEditCategory", "onCategoryChanged: Category not found. Closing activity.");
            finish();
            return;
        }
        mCategory = category;
        mAdapter.setInitialIcon(mCategory.getIconResName());
        mAddEditCategoryViewModel.setCategoryColor(category.getBackgroundColor());
        $.addeditCategoryNameInput.setText(category.getName());
        $.addeditCategoryType.setSelection(mCategory.getType() - 1);

        // Restore hint animation for the fields that will likely use it
        $.addeditCategoryName.setHintAnimationEnabled(true);
    }

    private void onCategoryColorChanged(@ColorInt int color) {
        // 1 is an invalid HEX number, no point in trying to apply color to elements
        if (color == 1) return;
        // If account is null (null != empty) then no data has been received from Room.
        // That means that we will receive a second color pretty soon and therefore
        // this first coloring can be skipped.
        if (mCategory == null) return;

        mCategory.setBackgroundColor(color);
        mAdapter.alterColor(color);
        setThemeColor(color);

        int fabBgColor = ColorUtil.getVariantByFactor(color, 0.86f);
        int fabTextColor = ColorUtil.getTextColor(fabBgColor);

        $.addeditCategoryContainer.setBackgroundColor(color);

        mFABVH.onView(this, v -> ViewTintingUtil.tintFAB(v, fabBgColor, fabTextColor));
        ViewTintingUtil
                .monotintTextInputLayout($.addeditCategoryName, mThemeTextColor);
        // TODO: Lighter colors make the hint barely readable
        //  Possible solutions:
        //      A) Use a darker variant of the selected color
        //      B) Change UI of this element (any ideas?)
        //      C) Is it possible to add a shadow or border to the hint text?
        ViewTintingUtil
                .tintSpinner($.addeditCategoryType, mThemeColor);

        // TODO: (Wish-list, not a big deal) How can we unify the ripple color with the one from FAB?
        mColorPickerBtnVH.onView(this, v -> {
            $.addeditCategoryColorButton.setBackgroundTintList(ColorStateList.valueOf(mThemeColor));
            $.addeditCategoryColorButton.getDrawable().mutate().setTint(mThemeTextColor);
        });
    }

    private void initSpinners() {
            String[] categoryTypeNames = getResources().getStringArray(R.array.category_types);
            List<CategoryTypeItem> categoryTypes = new ArrayList<>();
            for(int i = 0; i < categoryTypeNames.length; i++) {
                categoryTypes.add(new CategoryTypeItem(i+1, categoryTypeNames[i]));
            }
            GnomySpinnerAdapter<CategoryTypeItem> typesAdapter = new GnomySpinnerAdapter<>(this, categoryTypes);

            $.addeditCategoryType.setAdapter(typesAdapter);
            $.addeditCategoryType.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(@NonNull MaterialSpinner parent, @Nullable View view, int position, long id) {
                    $.addeditCategoryType.setStartIconDrawable(typesAdapter.getItemDrawable(position));
                }

                @Override
                public void onNothingSelected(@NonNull MaterialSpinner parent) {
                }
            });
            if ($.addeditCategoryType.getSelectedItem() == null)
                $.addeditCategoryType.setSelection(0);

    }

    public void showColorPicker(View v) {
        new SpectrumDialog.Builder(this)
                .setColors(ColorUtil.getColors())
                .setSelectedColor(Objects.requireNonNull(mAddEditCategoryViewModel.categoryColor.getValue()))
                .setDismissOnColorSelected(true)
                .setOutlineWidth(0)
                .setTitle(R.string.account_pick_color_title)
                .setFixedColumnCount(5)
                .setOnColorSelectedListener((positiveResult, color) -> {
                    if (positiveResult) {
                        mAddEditCategoryViewModel.setCategoryColor(color);
                    }
                }).build().show(getSupportFragmentManager(), TAG_PICKER_DIALOG);
    }

    public void processData(View v) {
        boolean texFieldsAreValid = validateTextFields();
        mAdapter.disableClicks();

        if (texFieldsAreValid) {
            int categoryType;
            if (mCategory.getId() == 0)
                categoryType = (int) $.addeditCategoryType.getSelectedItemId();
            else
                categoryType = mCategory.getType();
            saveData(categoryType);
        } else {
            Toast.makeText(this, getString(R.string.form_error), Toast.LENGTH_LONG).show();
            mFABVH.notifyOnAsyncOperationFinished();
            mAdapter.enableClicks();
        }
    }

    private void onCategoryNameEditTextChanges(String value) {
        if (mCategory != null) mCategory.setName(value);

        if (value.trim().length() == 0) {
            if (mAddEditCategoryViewModel.categoryNameIsPristine()) return;
            $.addeditCategoryName.setError(getString(R.string.category_error_name));
        } else {
            $.addeditCategoryName.setErrorEnabled(false);
        }

        mAddEditCategoryViewModel.notifyCategoryNameChanged();
    }

    private boolean validateTextFields() {
        mAddEditCategoryViewModel.notifyCategoryNameChanged();

        String categoryName = Objects.requireNonNull($.addeditCategoryNameInput.getText()).toString();
        onCategoryNameEditTextChanges(categoryName);

        return categoryName.length() > 0;
    }

    private void saveData(int categoryType) {
        Disposable disposable;

        mCategory.setType(categoryType);

        if (mCategory.getId() == 0) {
            disposable = mAddEditCategoryViewModel.insert(mCategory)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            longs -> {
                                Toast.makeText(this, R.string.category_message_saved, Toast.LENGTH_LONG).show();
                                finish();
                            },
                            throwable -> {
                                Toast.makeText(this, R.string.generic_data_error, Toast.LENGTH_LONG).show();
                                mFABVH.notifyOnAsyncOperationFinished();
                            });

        } else {
            disposable = mAddEditCategoryViewModel.update(mCategory)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            integer -> {
                                Toast.makeText(this, R.string.category_message_updated, Toast.LENGTH_LONG).show();
                                finish();
                            },
                            throwable -> {
                                Toast.makeText(this, R.string.generic_data_error, Toast.LENGTH_LONG).show();
                                mFABVH.notifyOnAsyncOperationFinished();
                            });
        }
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onIconSelected(String iconResName) {
        mCategory.setIconResName(iconResName);
    }
}