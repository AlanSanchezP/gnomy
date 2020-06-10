package io.github.alansanchezp.gnomy.viewmodel;

import android.app.Application;

import org.threeten.bp.YearMonth;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MainActivityViewModel extends AndroidViewModel {
    protected MutableLiveData<YearMonth> mMonthFilter = new MutableLiveData<>();

    public MainActivityViewModel (Application application) {
        super(application);
        if (mMonthFilter.getValue() == null) mMonthFilter.postValue(YearMonth.now());
    }

    public void setMonth(YearMonth month) {
        if (month == null) return;
        mMonthFilter.postValue(month);
    }

    public YearMonth getMonth() {
        return mMonthFilter.getValue();
    }

    public LiveData<YearMonth> getPublicMonthFilter() {
        return (LiveData) mMonthFilter;
    }
}