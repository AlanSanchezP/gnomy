package io.github.alansanchezp.gnomy.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.reactivex.disposables.CompositeDisposable;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.time.YearMonth;

public abstract class MainNavigationFragment
        extends Fragment {

    public static final String ARG_COLUMN_COUNT = "MainNavigationFragment.ColumnCount";
    public static final String ARG_NAVIGATION_INDEX = "MainNavigationFragment.NavigationIndex";

    protected MainNavigationInteractionInterface mNavigationInterface;

    protected int mColumnCount = 1;
    protected int mFragmentIndex = 0;
    protected YearMonth mCurrentMonth;
    protected Menu mMenu;
    protected final CompositeDisposable mCompositeDisposable
            = new CompositeDisposable();

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainNavigationInteractionInterface) {
            mNavigationInterface = (MainNavigationInteractionInterface) context;
        } else  {
            Log.w("BASE FRAGMENT", "onAttach: Navigation interface not found. Fallback to empty instance.");
            mNavigationInterface = new MainNavigationInteractionInterface() {
                @Override
                public void tintNavigationElements(int themeColor) {
                }

                @Override
                public void toggleOptionalNavigationElements(boolean showOptionalElements) {
                }

                @Override
                public void onFragmentChanged(int index) {
                }

                @Override
                public LiveData<YearMonth> getActiveMonth() {
                    MutableLiveData<YearMonth> mutableActiveMonth = new MutableLiveData<>();
                    mutableActiveMonth.postValue(DateUtil.now());
                    return mutableActiveMonth;
                }
            };
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(hasAppbarActions());
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT, 1);
            mFragmentIndex = getArguments().getInt(ARG_NAVIGATION_INDEX, 0);
            mNavigationInterface.onFragmentChanged(mFragmentIndex);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMenu != null) tintMenuIcons();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(getMenuResourceId(), menu);
        mMenu = menu;
        tintMenuIcons();
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().setTitle(getTitle());
        mNavigationInterface.toggleOptionalNavigationElements(
                withOptionalNavigationElements()
        );
        mNavigationInterface.tintNavigationElements(
                getThemeColor()
        );
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNavigationInterface = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    /* ABSTRACT METHODS */

    protected abstract boolean hasAppbarActions();

    protected abstract int getMenuResourceId();

    protected abstract boolean withOptionalNavigationElements();

    protected abstract int getThemeColor();

    protected abstract String getTitle();

    protected abstract void tintMenuIcons();

    /* ABSTRACT CUSTOM LISTENERS */

    public abstract void onFABClick(View v);

    public abstract void onMonthChanged(YearMonth month);

    public interface MainNavigationInteractionInterface {
        void tintNavigationElements(int themeColor);
        void toggleOptionalNavigationElements(boolean showOptionalElements);
        void onFragmentChanged(int index);
        LiveData<YearMonth> getActiveMonth();
    }
}