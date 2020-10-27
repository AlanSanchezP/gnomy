package io.github.alansanchezp.gnomy.viewmodel.customView;

import android.app.Application;
import android.util.Log;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import io.github.alansanchezp.gnomy.util.DateUtil;

public class MonthToolbarViewModel extends AndroidViewModel {
    protected SavedStateHandle mSavedState;
    protected static final String YEAR_MONTH_STRING = "active-month";
    protected MutableLiveData<YearMonth> mutableActiveMonth = new MutableLiveData<>();
    public final LiveData<YearMonth> activeMonth = (LiveData<YearMonth>) mutableActiveMonth;

    public MonthToolbarViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        mSavedState = savedStateHandle;
        try {
            if (mSavedState.get(YEAR_MONTH_STRING) == null)
                throw new IllegalArgumentException("Uninitialized string representation of YearMonth.");
            mutableActiveMonth.setValue(YearMonth.parse(
                    mSavedState.get(YEAR_MONTH_STRING)));
        } catch(DateTimeParseException dtpe) {
            throw new IllegalArgumentException("Invalid string representation of YearMonth.");
        } catch(IllegalArgumentException iae) {
            Log.d("MonthToolbarViewModel", "(): Fallback to now()", iae);
            today();
        }
    }

    protected void updateMonthState(YearMonth month) {
        mutableActiveMonth.setValue(month);
        mSavedState.set(YEAR_MONTH_STRING, month.toString());
    }

    public void prevMonth() {
        updateMonthState(Objects.requireNonNull(
                mutableActiveMonth.getValue()).minusMonths(1));
    }

    public void nextMonth() {
        updateMonthState(Objects.requireNonNull(
                mutableActiveMonth.getValue()).plusMonths(1));
    }

    public void today() {
        updateMonthState(DateUtil.now());
    }

    public void setMonth(YearMonth month) {
        if (month == null) return;
        updateMonthState(month);
    }
}