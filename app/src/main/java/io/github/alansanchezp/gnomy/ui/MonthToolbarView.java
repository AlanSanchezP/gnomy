package io.github.alansanchezp.gnomy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.databinding.LayoutMonthToolbarBinding;
import io.github.alansanchezp.gnomy.viewmodel.MainActivityViewModel;

// TODO: Use data binding to avoid cluttered mock activity
// TODO: Move to a different package
public class MonthToolbarView extends LinearLayout {
    final static String CALENDAR_PICKER_TAG = "CALENDAR PICKER MODAL";
    private LayoutMonthToolbarBinding mBinding;

    public MonthToolbarView(Context context) {
        this(context, null);
    }

    public MonthToolbarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthToolbarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public MonthToolbarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = LayoutMonthToolbarBinding.inflate(inflater, this, true);
        try {
            setViewModel(
                new ViewModelProvider(
                        ((AppCompatActivity) getContext()),
                        ViewModelProvider.AndroidViewModelFactory.getInstance(
                            ((AppCompatActivity) getContext()).getApplication()))
                        .get(MainActivityViewModel.class));
        } catch (NullPointerException npe) {
            Log.e("[Month Toolbar]", "initializeView: ", npe);
        }
    }

    private void setViewModel(MainActivityViewModel viewModel) {
        mBinding.setViewmodel(viewModel);
        mBinding.monthNameView.setOnClickListener(v -> onMonthPickerClick());
        mBinding.getViewmodel().selectedMonth
                .observe(((AppCompatActivity) getContext()), month -> updateMonthText(month));
    }

    private void onMonthPickerClick() {
        // TODO: Implement (when possible) a better looking calendar
        // Current limitation is that open source libraries
        // implementing material design do not support
        // a range limit, causing conflicts with
        // gnomy's inability to handle future balances
        Calendar calendar = Calendar.getInstance();
        YearMonth currentYearMonth = mBinding.getViewmodel().selectedMonth.getValue();
        int yearSelected = currentYearMonth.getYear();
        int monthSelected = currentYearMonth.getMonthValue();

        // Month representation here ranges from 0 to 11,
        // thus requiring +1 and -1 operations
        calendar.clear();
        calendar.set(YearMonth.now().getYear(), YearMonth.now().getMonthValue()-1, 1);
        long maxDate = calendar.getTimeInMillis();

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected-1, yearSelected, 0, maxDate);

        dialogFragment.setOnDateSetListener((year, monthOfYear) -> {
            YearMonth month = YearMonth.of(year, monthOfYear+1);
            mBinding.getViewmodel().setMonth(month);
        });

        try {
            dialogFragment.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), CALENDAR_PICKER_TAG);
        } catch (NullPointerException npe) {
            Log.e("[Month Toolbar]", "onMonthPickerClick: ", npe);
        }
    }

    private void updateMonthText(YearMonth month) {
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
            mBinding.nextMonthBtn.setVisibility(View.INVISIBLE);
            mBinding.returnToTodayBth.setVisibility(View.GONE);
        } else {
            mBinding.nextMonthBtn.setVisibility(View.VISIBLE);
            mBinding.returnToTodayBth.setVisibility(View.VISIBLE);
        }

        mBinding.monthNameView.setText(monthString);
    }

    public void setToolbarVisibility(boolean show, int bgColor) {
        mBinding.monthToolbarInner.setVisibility(View.GONE);

        if (show) {
            mBinding.monthToolbarInner.setVisibility(View.VISIBLE);
            setToolbarColor(bgColor);
        }
    }

    public void setToolbarColor(int bgColor) {
        mBinding.monthToolbarInner.setBackgroundColor(bgColor);
    }

    public LiveData<YearMonth> getSelectedMonth() {
        return mBinding.getViewmodel().selectedMonth;
    }
}
