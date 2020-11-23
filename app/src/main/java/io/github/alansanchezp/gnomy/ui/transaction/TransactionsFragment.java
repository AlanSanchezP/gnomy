package io.github.alansanchezp.gnomy.ui.transaction;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.time.YearMonth;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;

public class TransactionsFragment extends MainNavigationFragment {

    public TransactionsFragment(MainNavigationInteractionInterface _interface) {
        super(_interface);
        // Required empty public constructor
    }

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    /* CONCRETE METHODS INHERITED FROM ABSTRACT CLASS */

    @Override
    protected boolean hasAppbarActions() {
        return true;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.transactions_fragment_toolbar;
    }

    @Override
    protected boolean withOptionalNavigationElements() {
        return true;
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.colorPrimary);
    }

    @Override
    protected String getTitle() {
        return  getResources().getString(R.string.title_transactions);
    }

    @Override
    protected void tintMenuIcons() {
        mMenu.findItem(R.id.action_search)
                .getIcon()
                .setTint(getResources().getColor(R.color.colorTextInverse));
        mMenu.findItem(R.id.action_filter)
                .getIcon()
                .setTint(getResources().getColor(R.color.colorTextInverse));
    }

    @Override
    public void onFABClick(View v) {

    }

    @Override
    public void onMonthChanged(YearMonth month) {

    }
}