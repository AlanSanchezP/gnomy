package io.github.alansanchezp.gnomy.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.YearMonth;

import java.math.BigDecimal;
import java.util.List;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountsListViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountsFragment extends MainNavigationFragment
        implements AccountRecyclerViewAdapter.OnListItemInteractionListener,
        ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface {

    private AccountRecyclerViewAdapter mAdapter;
    private AccountsListViewModel mListViewModel;
    private LiveData<List<AccountWithBalance>> mAccountBalances;
    private TextView mBalance, mProjected;

    public AccountsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccountsFragment.
     */

    public static AccountsFragment newInstance(int columnCount, int index) {
        AccountsFragment fragment = new AccountsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_NAVIGATION_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        AccountRecyclerViewAdapter.OnListItemInteractionListener listener = (AccountRecyclerViewAdapter.OnListItemInteractionListener) this;
        mAdapter = new AccountRecyclerViewAdapter(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.requireActivity().getApplication())).get(AccountsListViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.items_list);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        mBalance = view.findViewById(R.id.total_balance);
        mProjected = view.findViewById(R.id.total_projected);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListViewModel.bindMonth(mNavigationInterface.getActiveMonth());
        if (mAccountBalances == null) {
            mAccountBalances = mListViewModel.getBalances();
        }
        setObservers();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableClicks();
    }

    /* ANDROID EVENT LISTENERS */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.action_show_archived:
                displayArchivedAccounts();
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    /* CONCRETE METHODS INHERITED FROM ABSTRACT CLASS */

    protected boolean hasAppbarActions() {
        return true;
    }

    protected int getMenuResourceId() {
        return R.menu.accounts_fragment_toolbar;
    }

    protected boolean withOptionalNavigationElements() {
        return true;
    }

    protected int getThemeColor() {
        return getResources().getColor(R.color.colorPrimary);
    }

    protected String getTitle() {
        return getResources().getString(R.string.title_accounts);
    }

    protected void tintMenuIcons(Menu menu) {
        menu.findItem(R.id.action_show_archived)
                .getIcon()
                .setTint(getResources().getColor(R.color.colorTextInverse));
    }

    /* CONCRETE LISTENERS INHERITED FROM ABSTRACT CLASS */

    public void onFABClick(View v) {
        Intent newAccountIntent = new Intent(getActivity(), AddEditAccountActivity.class);
        requireActivity().startActivity(newAccountIntent);
        mAdapter.disableClicks();
    }

    public void onMonthChanged(YearMonth month) {
        if (month == null) return;
        View v = getView();
        assert v != null;
        if (month.equals(DateUtil.now())) {
            ((TextView) v.findViewById(R.id.total_projected_label)).setText(R.string.account_projected_balance);
        } else {
            ((TextView) v.findViewById(R.id.total_projected_label)).setText(R.string.account_accumulated_balance);
        }

        mCurrentMonth = month;
    }

    public void onAccountsListChanged(List<AccountWithBalance> accounts) {
        if (mCurrentMonth == null) return;
        mAdapter.setValues(accounts, mCurrentMonth);

        // TODO: Use global user currency when implemented
        String userCurrencyCode = "USD";
        try {
            BigDecimal[] totalBalances = CurrencyUtil.sumAccountListBalances(accounts, userCurrencyCode);
            mBalance.setText(CurrencyUtil.format(totalBalances[0], userCurrencyCode));
            mProjected.setText(CurrencyUtil.format(totalBalances[1], userCurrencyCode));
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AccountsFragment", "setObserver: ", e);
        }
    }

    /* INTERFACE METHODS */

    public void onItemInteraction(Account account) {
        Intent detailsIntent = new Intent(getContext(), AccountDetailsActivity.class);
        detailsIntent.putExtra(AccountDetailsActivity.EXTRA_ID, account.getId());

        requireActivity().startActivity(detailsIntent);
    }

    public boolean onItemMenuItemInteraction(final Account account, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.account_card_details:
                onItemInteraction(account);
                break;
            case R.id.account_card_modify:
                Intent modifyAccountIntent = new Intent(getContext(), AddEditAccountActivity.class);
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_ID, account.getId());

                requireActivity().startActivity(modifyAccountIntent);
                break;
            case R.id.account_card_transactions:
                break;
            case R.id.account_card_archive:
                archiveAccount(account);
                break;
            case R.id.account_card_delete:
                deleteAccount(account);
                break;
            default:
                mAdapter.enableClicks();
                return false;
        }

        return true;
    }

    /* FRAGMENT-SPECIFIC METHODS */

    private void displayArchivedAccounts() {
        if (getChildFragmentManager()
                .findFragmentByTag(ArchivedAccountsDialogFragment.TAG) != null) return;
        ArchivedAccountsDialogFragment dialog =
                new ArchivedAccountsDialogFragment((ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface) this);
        dialog.show(getChildFragmentManager(), ArchivedAccountsDialogFragment.TAG);
    }

    private void setObservers() {
        mNavigationInterface.getActiveMonth().observe(getViewLifecycleOwner(), this::onMonthChanged);

        mAccountBalances.observe(getViewLifecycleOwner(), this::onAccountsListChanged);
    }

    public void archiveAccount(Account account) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.account_card_archive))
                .setMessage(getString(R.string.account_card_archive_info))
                .setPositiveButton(getString(R.string.confirmation_dialog_yes), (dialog, which) -> effectiveArchiveAccount(account))
                .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                .setOnDismissListener(dialog -> mAdapter.enableClicks())
                .show();
    }

    private void effectiveArchiveAccount(Account account) {
        mListViewModel.archive(account);
    }

    @Override
    public void deleteAccount(Account account) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.account_card_delete))
                .setMessage(getString(R.string.account_card_delete_warning))
                .setPositiveButton(getString(R.string.confirmation_dialog_yes), (dialog, which) -> effectiveDeleteAccount(account))
                .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                .setOnDismissListener(dialog -> mAdapter.enableClicks())
                .show();
    }

    private void effectiveDeleteAccount(Account account) {
        mListViewModel.delete(account);
    }

    @Override
    public void restoreAccount(Account account) {
        mListViewModel.restore(account);
    }

    @Override
    public LiveData<List<Account>> getArchivedAccounts() {
        return mListViewModel.getArchivedAccounts();
    }

    @Override
    public void restoreAllAccounts() {
        mListViewModel.restoreAll();
    }
}