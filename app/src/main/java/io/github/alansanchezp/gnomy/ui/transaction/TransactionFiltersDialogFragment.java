package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tiper.MaterialSpinner;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.databinding.DialogTransactionFiltersBinding;
import io.github.alansanchezp.gnomy.ui.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.ui.GnomySpinnerAdapter;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.android.InputFilterMinMax;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.util.android.ViewTintingUtil;

import static io.github.alansanchezp.gnomy.util.ISpinnerItem.getItemIndexById;
import static io.github.alansanchezp.gnomy.util.android.SimpleTextWatcherWrapper.onlyOnTextChanged;

public class TransactionFiltersDialogFragment
        extends DialogFragment
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    // TODO: Instrumented Test
    private static final String BUNDLE_FILTER_INSTANCE = "TransactionFiltersDialogFragment.FilterInstance";
    private static final String TAG_START_DATE_DIALOG = "TransactionFiltersDialogFragment.StartDateDialog";
    private static final String TAG_END_DATE_DIALOG = "TransactionFiltersDialogFragment.EndDateDialog";
    private static final String TAG_START_TIME_DIALOG = "TransactionFiltersDialogFragment.StartTimeDialog";
    private static final String TAG_END_TIME_DIALOG = "TransactionFiltersDialogFragment.EndTimeDialog";
    // Using jQuery syntax to make code more compact
    private DialogTransactionFiltersBinding $;
    private final TransactionFiltersDialogInterface mListener;
    private MoneyTransactionFilters mFiltersInstance;
    private boolean mInitialTintingFlag = false;
    private int mThemeColor;

    public TransactionFiltersDialogFragment() {
        throw new IllegalArgumentException("This class must be provided with a TransactionFiltersDialogInterface instance.");

    }

    public TransactionFiltersDialogFragment(TransactionFiltersDialogInterface _listener) {
        mListener = _listener;
    }

    private GnomyFragmentFactory getFragmentFactory() {
        return new GnomyFragmentFactory()
                .addMapElement(DatePickerDialog.class, this)
                .addMapElement(TimePickerDialog.class, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getChildFragmentManager().setFragmentFactory(getFragmentFactory());
        super.onCreate(savedInstanceState);
        // TODO: How to keep status bar?
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen);
        if (savedInstanceState == null) return;

        MoneyTransactionFilters savedInstance = savedInstanceState.getParcelable(BUNDLE_FILTER_INSTANCE);
        if (savedInstance != null) mFiltersInstance = savedInstance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        $ = DialogTransactionFiltersBinding.inflate(inflater, container, false);
        View view = $.getRoot();
        // Calculate default appbar/toolbar size
        TypedValue tv = new TypedValue();
        requireActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true);
        final int originalAppBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());

        // Resize toolbar to avoid overlap with status bar
        $.filtersDialogToolbar.setOnApplyWindowInsetsListener((v, insets) -> {
            // HARDCODING PARENT LAYOUT TYPE: We used LinearLayout as Toolbar's parent
            LinearLayout.LayoutParams layoutParams =  (LinearLayout.LayoutParams) v.getLayoutParams();
            // Only alter view dimensions if it's the first time this inset set arrives
            if (layoutParams.height == originalAppBarHeight && insets.getSystemWindowInsetTop() != v.getPaddingTop()) {
                // Make toolbar taller (necessary due to layout_height constraints)
                layoutParams.height += insets.getSystemWindowInsetTop();
                // Add top padding to move content down
                v.setPaddingRelative(v.getPaddingStart(), insets.getSystemWindowInsetTop(), v.getPaddingEnd(), v.getPaddingBottom());
                v.setLayoutParams(layoutParams);
            }
           return insets;
        });
        SingleClickViewHolder<Button> applyFiltersBtn = new SingleClickViewHolder<>($.filtersDialogApplyBtn);
        if (mFiltersInstance == null)
            mFiltersInstance = mListener.getInitialFilters();

        // Keeping vanilla spinner since MaterialSpinner keeps hint space and it looks weird
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.transaction_types, android.R.layout.simple_spinner_dropdown_item);
        $.filtersDialogTypeSpinner.setAdapter(adapter);
        $.filtersDialogTypeSpinner.setSelection(mFiltersInstance.getTransactionType(), true);
        setTransactionSpinnerIndex(mFiltersInstance.getTransactionType());
        mInitialTintingFlag = true;

        $.filtersDialogTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setTransactionSpinnerIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        $.filtersDialogCloseBtn.setOnClickListener(v ->
                requireDialog().cancel());
        applyFiltersBtn.setOnClickListener(v -> {
            if (!$.filtersDialogPeriodSwitch.isChecked()) {
                mFiltersInstance.setStartDate(null);
                mFiltersInstance.setEndDate(null);
            }
            if (!$.filtersDialogAmountSwitch.isChecked()) {
                mFiltersInstance.setMinAmount(null);
                mFiltersInstance.setMaxAmount(null);
            }
            mListener.applyFilters(mFiltersInstance);
            requireDialog().dismiss();
        });

        mListener.getAccountsLiveData().observe(getViewLifecycleOwner(), this::onAccountsListChanged);
        mListener.getCategoriesLiveData().observe(getViewLifecycleOwner(), this::onCategoriesListChanged);
        $.filtersDialogSortingSpinner.setAdapter(
                ArrayAdapter.createFromResource(requireContext(), R.array.transaction_filters_sorting_strategies, android.R.layout.simple_spinner_dropdown_item));
        $.filtersDialogSortingSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NotNull MaterialSpinner parent, @Nullable View view, int position, long id) {
                mFiltersInstance.setSortingMethod(position);
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner materialSpinner) {
            }
        });
        $.filtersDialogSortingSpinner.setSelection(mFiltersInstance.getSortingMethod());

        switch (mFiltersInstance.getTransactionStatus()) {
            case MoneyTransactionFilters.ANY_STATUS:
                $.filtersDialogStatusRadioAny.setChecked(true);
                break;
            case MoneyTransactionFilters.CONFIRMED_STATUS:
                $.filtersDialogStatusRadioConfirmed.setChecked(true);
                break;
            case MoneyTransactionFilters.UNCONFIRMED_STATUS:
                $.filtersDialogStatusRadioUnconfirmed.setChecked(true);
                break;
        }

        $.filtersDialogStatusRadioAny.setOnClickListener(v ->
                mFiltersInstance.setTransactionStatus(MoneyTransactionFilters.ANY_STATUS));
        $.filtersDialogStatusRadioConfirmed.setOnClickListener(v ->
                mFiltersInstance.setTransactionStatus(MoneyTransactionFilters.CONFIRMED_STATUS));
        $.filtersDialogStatusRadioUnconfirmed.setOnClickListener(v ->
                mFiltersInstance.setTransactionStatus(MoneyTransactionFilters.UNCONFIRMED_STATUS));

        $.filtersDialogPeriodSwitch.setOnCheckedChangeListener((b, checked) -> {
            int visibility = checked ? View.VISIBLE : View.GONE;
            $.filtersDialogPeriodFrom.setVisibility(visibility);
            $.filtersDialogPeriodTo.setVisibility(visibility);
        });
        $.filtersDialogPeriodSwitch.setChecked(
                mFiltersInstance.getStartDate() != null && mFiltersInstance.getEndDate() != null);
        if (mFiltersInstance.getStartDate() != null) {
            updateStartDateText(mFiltersInstance.getStartDate());
        }
        if (mFiltersInstance.getEndDate() != null) {
            updateEndDateText(mFiltersInstance.getEndDate());
        }
        $.filtersDialogPeriodFrom.setEndIconOnClickListener(v -> openDatePicker(true));
        $.filtersDialogPeriodFrom.setErrorIconOnClickListener(v -> openDatePicker(true));
        $.filtersDialogPeriodTo.setEndIconOnClickListener(v -> openDatePicker(false));
        $.filtersDialogPeriodTo.setErrorIconOnClickListener(v -> openDatePicker(false));

        $.filtersDialogAmountSwitch.setOnCheckedChangeListener((b, checked) -> {
            int visibility = checked ? View.VISIBLE : View.GONE;
            $.filtersDialogAmountGroup.setVisibility(visibility);
        });
        $.filtersDialogAmountSwitch.setChecked(
                mFiltersInstance.getMinAmount() != null && mFiltersInstance.getMaxAmount() != null);
        if (mFiltersInstance.getMinAmount() != null) {
            $.filtersDialogAmountMinInput.setText(mFiltersInstance.getMinAmount().toPlainString());
        }
        if (mFiltersInstance.getMaxAmount() != null) {
            $.filtersDialogAmountMaxInput.setText(mFiltersInstance.getMaxAmount().toPlainString());
        }
        $.filtersDialogAmountMinInput.setFilters(
                new InputFilter[]{new InputFilterMinMax(MoneyTransaction.MIN_VALUE, MoneyTransaction.MAX_VALUE, BigDecimalUtil.DECIMAL_SCALE)});
        $.filtersDialogAmountMinInput.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onAmountTextChanged(s.toString(), true)));
        $.filtersDialogAmountMaxInput.setFilters(
                new InputFilter[]{new InputFilterMinMax(MoneyTransaction.MIN_VALUE, MoneyTransaction.MAX_VALUE, BigDecimalUtil.DECIMAL_SCALE)});
        $.filtersDialogAmountMaxInput.addTextChangedListener(onlyOnTextChanged((s, start, count, after) ->
                onAmountTextChanged(s.toString(), false)));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        $ = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFiltersInstance != null) outState.putParcelable(BUNDLE_FILTER_INSTANCE, mFiltersInstance);
    }

    private void onAccountsListChanged(List<Account> _accounts) {
        final List<Account> accounts = new ArrayList<>(_accounts);
        final Account emptyAccount = new Account();
        emptyAccount.setName(getResources().getString(R.string.all_accounts));
        accounts.add(0, emptyAccount);

        GnomySpinnerAdapter<Account> adapter = new GnomySpinnerAdapter<>(requireContext(), accounts);
        $.filtersDialogAccountSpinner.setAdapter(adapter);
        $.filtersDialogAccountSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NotNull MaterialSpinner parent, @Nullable View view, int position, long id) {
                mFiltersInstance.setAccountId((int) id);
                Drawable drawable = adapter.getItemDrawable(position);
                if (drawable == null) {
                    drawable = requireContext().getDrawable(R.drawable.ic_account_balance_black_24dp);
                    Objects.requireNonNull(drawable).setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP));
                }
                parent.setStartIconDrawable(drawable);
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner parent) {
            }
        });
        $.filtersDialogAccountSpinner.setSelection(
                getItemIndexById(accounts, mFiltersInstance.getAccountId()));
    }

    private void onCategoriesListChanged(List<Category> _categories) {
        final List<Category> categories = new ArrayList<>(_categories);
        final Category emptyCategory = new Category();
        emptyCategory.setName(getResources().getString(R.string.all_categories));
        categories.add(0, emptyCategory);
        GnomySpinnerAdapter<Category> adapter = new GnomySpinnerAdapter<>(requireContext(), categories);
        $.filtersDialogCategorySpinner.setAdapter(adapter);
        $.filtersDialogCategorySpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NotNull MaterialSpinner parent, @Nullable View view, int position, long id) {
                mFiltersInstance.setCategoryId((int) id);
                Drawable drawable = adapter.getItemDrawable(position);
                if (drawable == null) {
                    drawable = requireContext().getDrawable(R.drawable.ic_baseline_category_24);
                    Objects.requireNonNull(drawable).setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP));
                }
                parent.setStartIconDrawable(drawable);
            }

            @Override
            public void onNothingSelected(@NotNull MaterialSpinner parent) {
            }
        });
        $.filtersDialogCategorySpinner.setSelection(
                getItemIndexById(categories, mFiltersInstance.getCategoryId()));
    }

    private void setTransactionSpinnerIndex(int spinnerIndex) {
        if (spinnerIndex == mFiltersInstance.getTransactionType() && mInitialTintingFlag) {
            // Needed so that spinner text doesn't get reset after rotation
            TextView spinnerView = (TextView) $.filtersDialogTypeSpinner.getSelectedView();
            spinnerView.setTextColor(ColorUtil.getTextColor(mThemeColor));
            return;
        }
        mFiltersInstance.setTransactionType(spinnerIndex);
        int textColor;
        switch (spinnerIndex) {
            case MoneyTransactionFilters.ALL_TRANSACTION_TYPES:
                mThemeColor = getResources().getColor(R.color.colorPrimary);
                break;
            case MoneyTransaction.INCOME:
                mThemeColor = getResources().getColor(R.color.colorIncomes);
                break;
            case MoneyTransaction.EXPENSE:
                mThemeColor = getResources().getColor(R.color.colorExpenses);
                break;
            case MoneyTransaction.TRANSFER:
                mThemeColor = getResources().getColor(R.color.colorTransfers);
                break;
            default:
                return;
        }
        textColor = ColorUtil.getTextColor(mThemeColor);
        $.filtersDialogToolbar.setBackgroundColor(mThemeColor);
        $.filtersDialogCloseBtn.getDrawable().setTint(textColor);
        $.filtersDialogStaticTitle.setTextColor(textColor);
        $.filtersDialogApplyBtn.setTextColor(textColor);
        TextView spinnerView = (TextView) $.filtersDialogTypeSpinner.getSelectedView();
        spinnerView.setTextColor(textColor);
        $.filtersDialogStatusRadioAny.setButtonTintList(ColorStateList.valueOf(mThemeColor));
        $.filtersDialogStatusRadioConfirmed.setButtonTintList(ColorStateList.valueOf(mThemeColor));
        $.filtersDialogStatusRadioUnconfirmed.setButtonTintList(ColorStateList.valueOf(mThemeColor));
        ViewTintingUtil.tintSpinner($.filtersDialogAccountSpinner, mThemeColor);
        ViewTintingUtil.tintSpinner($.filtersDialogTransferDestinationAccountSpinner, mThemeColor);
        ViewTintingUtil.tintSpinner($.filtersDialogCategorySpinner, mThemeColor);
        ViewTintingUtil.tintSpinner($.filtersDialogSortingSpinner, mThemeColor);
        ViewTintingUtil.tintSwitch($.filtersDialogPeriodSwitch, mThemeColor);
        ViewTintingUtil.tintSwitch($.filtersDialogAmountSwitch, mThemeColor);
        ViewTintingUtil.tintTextInputLayout($.filtersDialogAmountMin, mThemeColor);
        ViewTintingUtil.tintTextInputLayout($.filtersDialogAmountMax, mThemeColor);
    }

    private void openDatePicker(boolean isStartDate) {
        Calendar originalDateCalendar = Calendar.getInstance();
        OffsetDateTime originalDate = null;
        OffsetDateTime limitDate = null;
        if (isStartDate && mFiltersInstance.getStartDate() != null) {
            originalDate = mFiltersInstance.getStartDate();
            limitDate = mFiltersInstance.getEndDate();
        }
        else if (!isStartDate && mFiltersInstance.getEndDate() != null) {
            originalDate = mFiltersInstance.getEndDate();
            limitDate = mFiltersInstance.getStartDate();
        }
        if (originalDate != null) {
            originalDateCalendar.set(originalDate.getYear(),
                    // OffsetDateTime uses month numbers from 1-12
                    // but Calendar returns numbers from 0-11
                    originalDate.getMonthValue()-1,
                    originalDate.getDayOfMonth());
        }

        DatePickerDialog dialog = DatePickerDialog.newInstance(
                this, originalDateCalendar);

        if (limitDate != null) {
            Calendar limitDateCalendar = Calendar.getInstance();
            limitDateCalendar.set(limitDate.getYear(),
                    limitDate.getMonthValue()-1,
                    limitDate.getDayOfMonth());
            if (isStartDate)
                dialog.setMaxDate(limitDateCalendar);
            else
                dialog.setMinDate(limitDateCalendar);
        }
        dialog.setAccentColor(mThemeColor);
        dialog.show(getChildFragmentManager(), isStartDate ? TAG_START_DATE_DIALOG : TAG_END_DATE_DIALOG);
    }

    private void openTimePicker(boolean isStartDate) {
        TimePickerDialog dialog = TimePickerDialog.newInstance(
                this, false);
        Timepoint limitTime;
        if (isStartDate &&
                mFiltersInstance.getEndDate() != null &&
                mFiltersInstance.getEndDate().toLocalDate().isEqual(mFiltersInstance.getStartDate().toLocalDate())) {
            limitTime = new Timepoint(mFiltersInstance.getEndDate().getHour(),
                    mFiltersInstance.getEndDate().getMinute(),
                    mFiltersInstance.getEndDate().getSecond());
            dialog.setMaxTime(limitTime);
        } else if (!isStartDate &&
                mFiltersInstance.getStartDate() != null &&
                mFiltersInstance.getStartDate().toLocalDate().isEqual(mFiltersInstance.getEndDate().toLocalDate())) {
            limitTime = new Timepoint(mFiltersInstance.getStartDate().getHour(),
                    mFiltersInstance.getStartDate().getMinute(),
                    mFiltersInstance.getStartDate().getSecond());
            dialog.setMinTime(limitTime);
        }
        dialog.setAccentColor(mThemeColor);
        dialog.show(getChildFragmentManager(), isStartDate ? TAG_START_TIME_DIALOG : TAG_END_TIME_DIALOG);
    }

    private void updateStartDateText(OffsetDateTime datetime) {
        $.filtersDialogPeriodFromInput.setText(DateUtil.getOffsetDateTimeString(datetime, true));
        $.filtersDialogPeriodTo.setErrorEnabled(false);
        if (mFiltersInstance.getEndDate() != null &&
                !datetime.isBefore(mFiltersInstance.getEndDate())) {
            $.filtersDialogPeriodFrom.setError(getResources().getString(R.string.transaction_filters_start_date_error));
        } else {
            $.filtersDialogPeriodFrom.setErrorEnabled(false);
        }
    }

    private void updateEndDateText(OffsetDateTime datetime) {
        $.filtersDialogPeriodToInput.setText(DateUtil.getOffsetDateTimeString(datetime, true));
        $.filtersDialogPeriodFrom.setErrorEnabled(false);
        if (mFiltersInstance.getStartDate() != null &&
                !datetime.isAfter(mFiltersInstance.getStartDate())) {
            $.filtersDialogPeriodTo.setError(getResources().getString(R.string.transaction_filters_end_date_error));
        } else {
            $.filtersDialogPeriodTo.setErrorEnabled(false);
        }
    }

    private void onAmountTextChanged(String value, boolean isMin) {
        BigDecimal decimal;
        try {
            decimal = BigDecimalUtil.fromString(value);
        } catch (NumberFormatException e) {
            decimal = null;
            value = null;
        }
        if (isMin) {
            BigDecimal max = mFiltersInstance.getMaxAmount();
            $.filtersDialogAmountMax.setErrorEnabled(false);
            if (decimal != null) {
                if (max != null && max.compareTo(decimal) <= 0) {
                    $.filtersDialogAmountMin.setError(getResources().getString(R.string.transaction_filters_min_amount_error));
                    $.filtersDialogApplyBtn.setEnabled(false);
                } else {
                    $.filtersDialogAmountMin.setErrorEnabled(false);
                    $.filtersDialogApplyBtn.setEnabled(true);
                }
            }
            mFiltersInstance.setMinAmount(value);
        } else {
            BigDecimal min = mFiltersInstance.getMinAmount();
            $.filtersDialogAmountMin.setErrorEnabled(false);
            if (decimal != null) {
                if (min != null && min.compareTo(decimal) >= 0) {
                    $.filtersDialogApplyBtn.setEnabled(false);
                    $.filtersDialogAmountMax.setError(getResources().getString(R.string.transaction_filters_max_amount_error));
                } else {
                    $.filtersDialogAmountMax.setErrorEnabled(false);
                    $.filtersDialogApplyBtn.setEnabled(true);
                }
            }
            mFiltersInstance.setMaxAmount(value);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        boolean isStartDate = TAG_START_DATE_DIALOG.equals(view.getTag());
        OffsetDateTime dateTime = isStartDate ? mFiltersInstance.getStartDate() : mFiltersInstance.getEndDate();
        if (dateTime == null) dateTime = DateUtil.OffsetDateTimeNow();
        dateTime = dateTime.withYear(year)
                // OffsetDateTime uses month numbers from 1-12
                // but OnDateSetListener returns numbers from 0-11
                .withMonth(monthOfYear+1)
                .withDayOfMonth(dayOfMonth);

        if (isStartDate) {
            mFiltersInstance.setStartDate(dateTime);
            updateStartDateText(dateTime);
        }
        else {
            mFiltersInstance.setEndDate(dateTime);
            updateEndDateText(dateTime);
        }

        openTimePicker(isStartDate);
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        boolean isStartDate = TAG_START_TIME_DIALOG.equals(view.getTag());
        // As time dialog always comes after date dialog, dateTime should never be null
        OffsetDateTime dateTime = isStartDate ? mFiltersInstance.getStartDate() : mFiltersInstance.getEndDate();
        dateTime = dateTime.withHour(hourOfDay)
                .withMinute(minute)
                .withSecond(second);

        if (isStartDate) {
            mFiltersInstance.setStartDate(dateTime);
            updateStartDateText(dateTime);
        }
        else {
            mFiltersInstance.setEndDate(dateTime);
            updateEndDateText(dateTime);
        }
    }

    public interface TransactionFiltersDialogInterface {
        void applyFilters(@NonNull MoneyTransactionFilters filters);
        @NonNull
        MoneyTransactionFilters getInitialFilters();
        @NonNull LiveData<List<Category>> getCategoriesLiveData();
        @NonNull LiveData<List<Account>> getAccountsLiveData();
    }
}