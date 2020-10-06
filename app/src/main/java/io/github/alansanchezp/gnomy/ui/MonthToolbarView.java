package io.github.alansanchezp.gnomy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;

import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.github.alansanchezp.gnomy.R;

public class MonthToolbarView extends LinearLayout {
    final static String CALENDAR_PICKER_TAG = "CALENDAR PICKER MODAL";
    Toolbar mInnerToolbar;
    TextView mMonthTextView;
    ImageButton mPrevMonthBtn;
    ImageButton mNextMonthBtn;
    ImageButton mCalendarBtn;
    MonthYearPickerDialog.OnDateSetListener mOnDateSetListener;

    public MonthToolbarView(Context context) {
        super(context);
        initializeView(context);
    }

    public MonthToolbarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public MonthToolbarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context);
    }

    public MonthToolbarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(context);
    }

    private void initializeView(Context context) {
        inflate(getContext(), R.layout.layout_month_toolbar,this);
        mInnerToolbar = (Toolbar) findViewById(R.id.month_toolbar_inner);
        mMonthTextView = (TextView) findViewById(R.id.month_name_view);
        mPrevMonthBtn = (ImageButton) findViewById(R.id.prev_month_btn);
        mNextMonthBtn = (ImageButton) findViewById(R.id.next_month_btn);
        mCalendarBtn =  (ImageButton) findViewById(R.id.return_to_today_bth);
    }

    public void setListeners(final MonthToolbarClickListener listener) {
        mMonthTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMonthPickerClick(listener.onGetYearMonth());
            }
        });
        mPrevMonthBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPreviousMonthClick();
            }
        });
        mNextMonthBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNextMonthClick();
            }
        });
        mCalendarBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReturnToCurrentMonthClick();
            }
        });
        mOnDateSetListener = new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                listener.onDateSet(year, monthOfYear);
            }
        };;
    }

    private void onMonthPickerClick(YearMonth currentYearMonth) {
        // TODO: Implement (when possible) a better looking calendar
        // Current limitation is that open source libraries
        // implementing material design do not support
        // a range limit, causing conflicts with
        // gnomy's inability to handle future balances
        Calendar calendar = Calendar.getInstance();
        int yearSelected = currentYearMonth.getYear();
        int monthSelected = currentYearMonth.getMonthValue();

        // Month representation here ranges from 0 to 11,
        // thus requiring +1 and -1 operations
        calendar.clear();
        calendar.set(YearMonth.now().getYear(), YearMonth.now().getMonthValue()-1, 1);
        long maxDate = calendar.getTimeInMillis();

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected-1, yearSelected, 0, maxDate);

        dialogFragment.setOnDateSetListener(mOnDateSetListener);

        try {
            dialogFragment.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), CALENDAR_PICKER_TAG);
        } catch (NullPointerException npe) {
            Log.e("[Month Toolbar]", "onMonthPickerClick: ", npe);
        }
    }

    public void setMonth(YearMonth month) {
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
            mNextMonthBtn.setVisibility(View.INVISIBLE);
            mCalendarBtn.setVisibility(View.GONE);
        } else {
            mNextMonthBtn.setVisibility(View.VISIBLE);
            mCalendarBtn.setVisibility(View.VISIBLE);
        }

        mMonthTextView.setText(monthString);
    }

    public void setToolbarVisibility(boolean show, int bgColor) {
        mInnerToolbar.setVisibility(View.GONE);

        if (show) {
            mInnerToolbar.setVisibility(View.VISIBLE);
            setToolbarColor(bgColor);
        }
    }

    public void setToolbarColor(int bgColor) {
        mInnerToolbar.setBackgroundColor(bgColor);
    }

    public interface MonthToolbarClickListener {
        void onPreviousMonthClick();
        void onNextMonthClick();
        void onReturnToCurrentMonthClick();
        YearMonth onGetYearMonth();
        void onDateSet(int year, int monthOfYear);
    }
}
