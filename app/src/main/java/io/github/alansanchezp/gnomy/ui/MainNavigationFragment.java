package io.github.alansanchezp.gnomy.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import io.github.alansanchezp.gnomy.viewmodel.MainActivityViewModel;
import io.reactivex.disposables.CompositeDisposable;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.YearMonth;
import java.util.Observable;
import java.util.Observer;

public abstract class MainNavigationFragment
        extends Fragment
        implements Observer {

    protected Menu mMenu;
    protected final CompositeDisposable mCompositeDisposable
            = new CompositeDisposable();
    protected MainActivityViewModel mSharedViewModel;

    public MainNavigationFragment() {
        // Empty constructor
    }

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(getMenuResourceId() != null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMenu != null) tintMenuIcons();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //noinspection ConstantConditions
        inflater.inflate(getMenuResourceId(), menu);
        mMenu = menu;
        tintMenuIcons();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedViewModel = new ViewModelProvider(requireActivity())
                .get(MainActivityViewModel.class);
        mSharedViewModel.observeFAB(this);
        mSharedViewModel.toggleOptionalNavigationElements(withOptionalNavigationElements());
        mSharedViewModel.changeThemeColor(getThemeColor());
        mSharedViewModel.changeTitle(getTitle());
        mSharedViewModel.activeMonth.observe(getViewLifecycleOwner(), this::onMonthChanged);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSharedViewModel.removeFABObserver(this);
    }

    public final void update(Observable o, @NonNull Object arg) {
        onFABClick((FloatingActionButton) arg);
    }

    /* ABSTRACT METHODS */

    protected abstract @Nullable Integer getMenuResourceId();

    protected abstract boolean withOptionalNavigationElements();

    protected abstract int getThemeColor();

    protected abstract String getTitle();

    protected abstract void tintMenuIcons();

    /* ABSTRACT CUSTOM LISTENERS */

    protected abstract void onFABClick(View v);

    protected abstract void onMonthChanged(YearMonth month);
}