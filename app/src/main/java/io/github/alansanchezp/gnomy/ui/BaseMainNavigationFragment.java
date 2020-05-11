package io.github.alansanchezp.gnomy.ui;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public abstract class BaseMainNavigationFragment
        extends Fragment {

    public static final String ARG_NAVIGATION_INDEX = "navigation-index";
    protected MainNavigationInteractionInterface mNavigationInterface;
    protected int mFragmentIndex = 0;

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainNavigationInteractionInterface) {
            mNavigationInterface = (MainNavigationInteractionInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AppbarInteractionInterface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(hasAppbarActions());
        if (getArguments() != null) {
            mFragmentIndex = getArguments().getInt(ARG_NAVIGATION_INDEX);
            mNavigationInterface.onFragmentChanged(mFragmentIndex);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(getMenuResourceId(), menu);
        tintMenuIcons(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        setTitle();
        tintAppbars();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNavigationInterface = null;
    }

    /* COMMON METHODS ACROSS CONCRETE CLASSES */

    protected void setTitle() {
        getActivity().setTitle(getTitle());
    }

    protected void tintAppbars() {
        mNavigationInterface.tintAppbars(
                getAppbarColor(),
                displaySecondaryToolbar()
        );
    }

    /* ABSTRACT METHODS */

    protected abstract boolean hasAppbarActions();

    protected abstract int getMenuResourceId();

    protected abstract boolean displaySecondaryToolbar();

    protected abstract int getAppbarColor();

    protected abstract String getTitle();

    protected abstract void tintMenuIcons(Menu menu);

    /* ABSTRACT CUSTOM LISTENERS */

    public abstract void onFABClick(View v);

    // TODO: Listener for month changes from MainActivity

    public interface MainNavigationInteractionInterface {
        void tintAppbars(int mainColor, boolean showSecondaryToolbar);
        void onFragmentChanged(int index);
    }
}