package io.github.alansanchezp.gnomy.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;
import io.github.alansanchezp.gnomy.viewmodel.MainActivityViewModel;
import io.reactivex.disposables.CompositeDisposable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

/**
 * Base class for fragments used in MainActivity navigation.
 *
 * @param <B>   ViewBinding class to use.
 */
public abstract class MainNavigationFragment<B extends ViewBinding>
        extends Fragment
        implements Observer {

    protected Menu mMenu;
    protected final CompositeDisposable mCompositeDisposable
            = new CompositeDisposable();
    protected MainActivityViewModel mSharedViewModel;
    protected B $;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        try {
            Class<B> clazz = getBindingClass();
            Method inflate = clazz.getMethod("inflate", LayoutInflater.class, ViewGroup.class, Boolean.TYPE);
            //noinspection unchecked
            $ = (B) inflate.invoke(null, inflater, container, false);
            v = Objects.requireNonNull($).getRoot();
        } catch (NullPointerException |
                NoSuchMethodException |
                IllegalAccessException |
                InvocationTargetException e) {
            Log.w("MainNavigationFragment", "onCreateView: Failed to initialize ViewBinding object. ", e);
            v = super.onCreateView(inflater, container, savedInstanceState);
        }

        return v;
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

    private Class<B> getBindingClass() {
        Class<B> result = null;
        Type type = this.getClass().getGenericSuperclass();

        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] fieldArgTypes = pt.getActualTypeArguments();
            //noinspection unchecked
            result = (Class<B>) fieldArgTypes[0];
        }
        return result;
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