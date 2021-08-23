package io.github.alansanchezp.gnomy.ui.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;

import java.time.YearMonth;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import io.github.alansanchezp.gnomy.databinding.LayoutMonthToolbarBinding;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.viewmodel.customView.MonthToolbarViewModel;

import static com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment.NULL_INT;

public class MonthToolbarView extends LinearLayout {
    final static String CALENDAR_PICKER_TAG = "MonthToolbarView.CalendarDialog";
    private LayoutMonthToolbarBinding mBinding;
    private MonthToolbarViewModel mViewModel;

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
        mBinding = LayoutMonthToolbarBinding.inflate(Objects.requireNonNull(inflater),
                this);
    }

    public void setViewModel(MonthToolbarViewModel viewModel) {
        mViewModel = viewModel;
        SingleClickViewHolder<TextView> monthNameVH =
                new SingleClickViewHolder<>(mBinding.monthNameView);
        monthNameVH.setOnClickListener(v -> onMonthPickerClick());
        mViewModel.activeMonth
                .observe(((AppCompatActivity) getContext()), this::updateMonthText);
        mBinding.prevMonthBtn.setOnClickListener(v -> mViewModel.prevMonth());
        mBinding.nextMonthBtn.setOnClickListener(v -> mViewModel.nextMonth());
        mBinding.returnToTodayBth.setOnClickListener(v -> mViewModel.today());
    }

    private void onMonthPickerClick() {
        // TODO: [#50] Implement (when possible) a better looking calendar
        // Current limitation is that open source libraries
        //  implementing material design do not support
        //  a range limit, and it might still be desirable to limit
        //  past dates to the month of account's creation
        YearMonth activeYearMonth = mViewModel.activeMonth.getValue();
        int activeYear = Objects.requireNonNull(activeYearMonth).getYear();
        int activeMonth = activeYearMonth.getMonthValue();

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(activeMonth-1, activeYear, NULL_INT, NULL_INT);

        dialogFragment.setOnDateSetListener((year, monthOfYear) -> {
            YearMonth month = YearMonth.of(year, monthOfYear+1);
            mViewModel.setMonth(month);
        });

        try {
            dialogFragment.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), CALENDAR_PICKER_TAG);
        } catch (NullPointerException npe) {
            Log.e("[Month Toolbar]", "onMonthPickerClick: ", npe);
        }
    }

    private void updateMonthText(YearMonth month) {
        String monthString = DateUtil.getYearMonthString(month);

        if (month.equals(DateUtil.now())) {
            mBinding.returnToTodayBth.setVisibility(View.GONE);
        } else {
            mBinding.returnToTodayBth.setVisibility(View.VISIBLE);
        }

        mBinding.monthNameView.setText(monthString);
    }

    public void toggleVisibility(boolean show) {
        if (show) {
            mBinding.monthToolbarInner.setVisibility(View.VISIBLE);
        } else {
            mBinding.monthToolbarInner.setVisibility(View.GONE);
        }
    }

    public boolean isVisible() {
        return mBinding.monthToolbarInner.getVisibility() == View.VISIBLE;
    }

    public void tintElements(int themeColor) {
        tintElements(themeColor, ColorUtil.getTextColor(themeColor));
    }

    public void tintElements(int themeColor, int themeTextColor) {
        mBinding.monthToolbarInner.setBackgroundColor(themeColor);
        mBinding.monthNameView.setTextColor(themeTextColor);
        mBinding.prevMonthBtn.getDrawable().setTint(themeTextColor);
        mBinding.nextMonthBtn.getDrawable().setTint(themeTextColor);
        mBinding.returnToTodayBth.getDrawable().setTint(themeTextColor);
    }
}
