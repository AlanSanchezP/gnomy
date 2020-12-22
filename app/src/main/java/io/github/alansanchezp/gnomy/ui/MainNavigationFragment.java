package io.github.alansanchezp.gnomy.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import io.reactivex.disposables.CompositeDisposable;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import java.time.YearMonth;

public abstract class MainNavigationFragment
        extends Fragment {

    protected MainNavigationInteractionInterface mNavigationInterface;

    protected YearMonth mCurrentMonth;
    protected Menu mMenu;
    protected final CompositeDisposable mCompositeDisposable
            = new CompositeDisposable();

    public MainNavigationFragment(MainNavigationInteractionInterface _interface) {
        mNavigationInterface = _interface;
    }

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(hasAppbarActions());
        mNavigationInterface.onFragmentChanged(this.getClass());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMenu != null) tintMenuIcons();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
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
        // TODO: Is this method really necessary? Evaluate (current idea is to use these indexes
        //  for animations)
        void onFragmentChanged(Class<? extends MainNavigationFragment> clazz);
        LiveData<YearMonth> getActiveMonth();
    }
}