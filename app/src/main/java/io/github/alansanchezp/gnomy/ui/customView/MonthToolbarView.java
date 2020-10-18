package io.github.alansanchezp.gnomy.ui.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;

import java.time.YearMonth;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.databinding.LayoutMonthToolbarBinding;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.viewmodel.customView.MonthToolbarViewModel;

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
                        .get(MonthToolbarViewModel.class));
        } catch (NullPointerException npe) {
            Log.e("[Month Toolbar]", "initializeView: ", npe);
        }
    }

    private void setViewModel(MonthToolbarViewModel viewModel) {
        mBinding.setViewmodel(viewModel);
        mBinding.monthNameView.setOnClickListener(v -> onMonthPickerClick());
        mBinding.getViewmodel().activeMonth
                .observe(((AppCompatActivity) getContext()), month -> updateMonthText(month));
    }

    private void onMonthPickerClick() {
        // TODO: Implement (when possible) a better looking calendar
        // Current limitation is that open source libraries
        // implementing material design do not support
        // a range limit, causing conflicts with
        // gnomy's inability to handle future balances
        Calendar calendar = Calendar.getInstance();
        YearMonth activeYearMonth = mBinding.getViewmodel().activeMonth.getValue();
        int activeYear = activeYearMonth.getYear();
        int activeMonth = activeYearMonth.getMonthValue();

        // Month representation here ranges from 0 to 11,
        // thus requiring +1 and -1 operations
        calendar.clear();
        calendar.set(DateUtil.now().getYear(), DateUtil.now().getMonthValue()-1, 1);
        long maxDate = calendar.getTimeInMillis();

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(activeMonth-1, activeYear, 0, maxDate);

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
        String monthString = DateUtil.getYearMonthString(month);
        /* Temporal limitation
           TODO: Handle projected balances for future months (as there is no MonthlyBalance instance for those) */
        if (month.equals(DateUtil.now())) {
            mBinding.nextMonthBtn.setVisibility(View.INVISIBLE);
            mBinding.returnToTodayBth.setVisibility(View.GONE);
        } else {
            mBinding.nextMonthBtn.setVisibility(View.VISIBLE);
            mBinding.returnToTodayBth.setVisibility(View.VISIBLE);
        }

        mBinding.monthNameView.setText(monthString);
    }

    // TODO: Evaluate if should test these two methods
    //  setToolbarVisibility and setToolbarColor
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

    public LiveData<YearMonth> getActiveMonth() {
        return mBinding.getViewmodel().activeMonth;
    }
}
