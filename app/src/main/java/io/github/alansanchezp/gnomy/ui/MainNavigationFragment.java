package io.github.alansanchezp.gnomy.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;
import static io.github.alansanchezp.gnomy.R.color.colorPrimary;
import io.github.alansanchezp.gnomy.viewmodel.MainActivityViewModel;
import io.reactivex.disposables.CompositeDisposable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.YearMonth;
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
    protected final Integer mFragmentTitleResourceId;
    protected final Integer mMenuResourceId;
    protected final boolean mWithOptionalNavigationElements;
    protected final FragmentViewBindingInflater<B> mViewBindingInflater;
    protected MainActivityViewModel mSharedViewModel;
    // ViewBinding object. Named using javascript's jquery style
    protected B $;

    /**
     * Creates a new instance, initializing class fields.
     *
     * @param fragmentTitleResourceId           String resource to display in the host
     *                                          Activity appbar. Set to null if the
     *                                          title will change dynamically.
     *                                          Child class will be responsible of
     *                                          displaying the correct title in that case.
     * @param menuResourceId                    Menu to use in the host Activity
     *                                          appbar. Set to null if no menu is required.
     * @param withOptionalNavigationElements    Specifies if the hosting activity is expected
     *                                          to display additional elements (other than
     *                                          appbar and bottom navigation view). Currently,
     *                                          month toolbar and FAB are expected.
     * @param viewBindingInflater               Inflater method to retrieve the ViewBinding object.
     *                                          In order to avoid using Java reflection, this must be
     *                                          provided by each child class. Lambda method
     *                                          references can be used as follows:
     *                                          ViewBindingClass::inflate
     */
    protected MainNavigationFragment(@Nullable Integer fragmentTitleResourceId,
                                     @Nullable Integer menuResourceId,
                                     boolean withOptionalNavigationElements,
                                     @NonNull FragmentViewBindingInflater<B> viewBindingInflater) {
        super();
        mMenuResourceId = menuResourceId;
        mFragmentTitleResourceId = fragmentTitleResourceId;
        mWithOptionalNavigationElements = withOptionalNavigationElements;
        mViewBindingInflater = viewBindingInflater;
    }

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(mMenuResourceId != null);
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
        inflater.inflate(mMenuResourceId, menu);
        mMenu = menu;
        tintMenuIcons();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        $ = mViewBindingInflater.inflateViewBinding(inflater, container, false);
        return $.getRoot();
    }

    /**
     * Initializes shared view model instance to communicate with parent
     * Activity and sends fragment's data.
     *
     * @param view                  Root view
     * @param savedInstanceState    Saved instance
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedViewModel = new ViewModelProvider(requireActivity())
                .get(MainActivityViewModel.class);
        mSharedViewModel.toggleOptionalNavigationElements(mWithOptionalNavigationElements);
        mSharedViewModel.changeThemeColor(getResources().getColor(colorPrimary));
        mSharedViewModel.activeMonth.observe(getViewLifecycleOwner(), this::onMonthChanged);

        if (mWithOptionalNavigationElements)
            mSharedViewModel.observeFAB(this);
        if (mFragmentTitleResourceId != null)
            mSharedViewModel.changeTitle(getResources().getString(mFragmentTitleResourceId));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWithOptionalNavigationElements)
            mSharedViewModel.removeFABObserver(this);
    }

    /**
     * {@link Observer} method to handle parent Activity's FAB click.
     *
     * @param o     Observable instance. Not used
     *              (assuming it will always correspond to the shared view model)
     * @param arg   Expected to be a reference to MainActivity's FAB.
     *
     * @throws ClassCastException If arg is not an instance of FloatingActionButton
     */
    public final void update(Observable o, @NonNull Object arg) {
        if (!mWithOptionalNavigationElements) return;
        onFABClick((FloatingActionButton) arg);
    }

    /* ABSTRACT METHODS */

    /**
     * Called to tint menu icon resources. If the fragment doesn't use
     * any menu, implement as an empty method.
     */
    protected abstract void tintMenuIcons();

    /* ABSTRACT CUSTOM LISTENERS */

    /**
     * Called when hosting Activity's FAB is pressed. If the fragment doesn't use
     * optional navigation elements, implement as empty method.
     *
     * @param v     FloatingActionButton reference.
     */
    protected abstract void onFABClick(View v);

    /**
     * Called when month toolbar values change. If the fragment doesn't use
     * this values, implement as empty method.
     *
     * @param month     Received YearMonth value.
     */
    protected abstract void onMonthChanged(YearMonth month);

    /**
     * Helper interface to dynamically inflate a layout using ViewBinding.
     * Intended to wrap a ViewBindingSubClass.inflate method.
     *
     * @param <B>   Target ViewBinding subclass
     */
    protected interface FragmentViewBindingInflater<B extends ViewBinding> {
        /**
         * Wrapper for ViewBindingSubClass.inflate method.
         *
         * @param inflater          LayoutInflater to use.
         * @param parent            View's parent.
         * @param attachToParent    Indicates if the generated view
         *                          should be attached to its parent's root.
         * @return                  ViewBinding subclass instance.
         */
        @NonNull
        B inflateViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent);
    }
}