package io.github.alansanchezp.gnomy.ui.account;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.time.YearMonth;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.databinding.FragmentAccountsBinding;
import io.github.alansanchezp.gnomy.ui.ConfirmationDialogFragment;
import io.github.alansanchezp.gnomy.androidUtil.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.transaction.TransactionsFragment;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountsListViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AccountsFragment
        extends MainNavigationFragment<FragmentAccountsBinding>
        implements  AccountRecyclerViewAdapter.OnListItemInteractionListener,
                    ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface,
                    ConfirmationDialogFragment.OnConfirmationDialogListener {

    public static final String TAG_ARCHIVE_ACCOUNT_DIALOG = "AccountsFragment.ArchiveAccountDialog";
    public static final String TAG_DELETE_ACCOUNT_DIALOG = "AccountsFragment.DeleteAccountDialog";
    private AccountRecyclerViewAdapter mAdapter;
    private AccountsListViewModel mListViewModel;
    private Map<Integer, AccountWithAccumulated> mTodayAccumulatesMap;

    public AccountsFragment() {
        super(R.string.title_accounts,
                R.menu.accounts_fragment_toolbar,
                true,
                FragmentAccountsBinding::inflate);
    }

    private GnomyFragmentFactory getFragmentFactory() {
        return new GnomyFragmentFactory()
                .addMapElement(ArchivedAccountsDialogFragment.class, this)
                .addMapElement(ConfirmationDialogFragment.class, this);
    }

    /* ANDROID LIFECYCLE METHODS */

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        AccountRecyclerViewAdapter.OnListItemInteractionListener listener = this;
        mAdapter = new AccountRecyclerViewAdapter(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getChildFragmentManager().setFragmentFactory(getFragmentFactory());
        super.onCreate(savedInstanceState);
        // TODO: Refactor other usages of SavedStateViewModelFactory
        //  as they will probably crash too at some point
        //  (Fragment threw error: SavedStateProvider with the given key is already registered
        mListViewModel = new ViewModelProvider(this)
                .get(AccountsListViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = $.itemsList;
        recyclerView.setLayoutManager(new LinearLayoutManager($.getRoot().getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        mListViewModel.bindMonth(mSharedViewModel.activeMonth);
        mListViewModel.getTodayAccumulatesList()
                .observe(getViewLifecycleOwner(), this::onTodayAccumulatesListChanged);
        mListViewModel.getAccumulatesListAtMonth()
                .observe(getViewLifecycleOwner(), this::onAccumulatesListChanged);
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

    protected void tintMenuIcons() {
        mMenu.findItem(R.id.action_show_archived)
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
        assert $ != null;
        if (month.isBefore(DateUtil.now())) {
            $.totalProjectedLabel.setText(R.string.account_balance_end_of_month);
        } else {
            $.totalProjectedLabel.setText(R.string.account_projected_balance);
        }
    }

    private void onTodayAccumulatesListChanged(List<AccountWithAccumulated> accumulates) {
        mTodayAccumulatesMap = mListViewModel.getAccumulatesMapFromList(accumulates);
        mAdapter.notifyTodayAccumulatesAreAvailable(mTodayAccumulatesMap.size());
        // TODO: Use global user currency when implemented
        String userCurrencyCode = "USD";
        try {
            BigDecimal totalAccumulates = CurrencyUtil.sumAccountAccumulates(
                    true,
                    accumulates,
                    userCurrencyCode);
            $.totalBalance.setText(CurrencyUtil.format(totalAccumulates, userCurrencyCode));
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AccountsFragment", "setObserver: ", e);
        }
    }

    private void onAccumulatesListChanged(List<AccountWithAccumulated> accumulates) {
        // TODO: Display some helpful information if list is empty, current behavior
        //  just doesn't display any data in recyclerview (not a bug, but UX can be improved)
        if (mSharedViewModel.activeMonth.getValue() == null) return;
        mAdapter.setValues(accumulates);

        // TODO: Use global user currency when implemented
        String userCurrencyCode = "USD";
        try {
            BigDecimal totalEndOfMonth = CurrencyUtil.sumAccountAccumulates(
                    false,
                    accumulates,
                    userCurrencyCode);
            $.totalProjected.setText(CurrencyUtil.format(totalEndOfMonth, userCurrencyCode));
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AccountsFragment", "setObserver: ", e);
        }
    }

    /* INTERFACE METHODS */
    public AccountWithAccumulated getTodayAccumulatedFromAccount(int accountId) {
        return mTodayAccumulatesMap.get(accountId);
    }

    public void onItemInteraction(Account account) {
        Intent detailsIntent = new Intent(getContext(), AccountDetailsActivity.class);
        detailsIntent.putExtra(AccountDetailsActivity.EXTRA_ACCOUNT_ID, account.getId());

        requireActivity().startActivity(detailsIntent);
    }

    public boolean onItemMenuItemInteraction(final Account account, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.account_card_details:
                onItemInteraction(account);
                break;
            case R.id.account_card_modify:
                Intent modifyAccountIntent = new Intent(getContext(), AddEditAccountActivity.class);
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_ACCOUNT_ID, account.getId());

                requireActivity().startActivity(modifyAccountIntent);
                break;
            case R.id.account_card_transactions:
                // TODO: Is it worth including the SafeArgs module?
                Bundle args = new Bundle();
                args.putInt(TransactionsFragment.ARG_DEFAULT_FILTER_ACCOUNT, account.getId());
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_accounts_to_navigation_transactions, args);
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

    public void onUnresolvedTransactions(Account account, YearMonth month) {
        // TODO: Implement when Transactions module is ready
        mAdapter.enableClicks();
    }

    /* FRAGMENT-SPECIFIC METHODS */

    private void displayArchivedAccounts() {
        FragmentManager fm = getChildFragmentManager();
        if (fm.findFragmentByTag(ArchivedAccountsDialogFragment.TAG_ARCHIVED_ACCOUNTS_DIALOG) != null) return;
        ArchivedAccountsDialogFragment dialog = (ArchivedAccountsDialogFragment)
                fm.getFragmentFactory().instantiate(
                        requireContext().getClassLoader(), ArchivedAccountsDialogFragment.class.getName());
        dialog.show(fm, ArchivedAccountsDialogFragment.TAG_ARCHIVED_ACCOUNTS_DIALOG);
    }

    public void archiveAccount(Account account) {
        FragmentManager fm = getChildFragmentManager();
        if (fm.findFragmentByTag(TAG_ARCHIVE_ACCOUNT_DIALOG) != null) return;
        mListViewModel.setTargetIdToArchive(account.getId());
        ConfirmationDialogFragment dialog = (ConfirmationDialogFragment)
                fm.getFragmentFactory().instantiate(
                        requireContext().getClassLoader(), ConfirmationDialogFragment.class.getName());
        Bundle args = new Bundle();
        args.putString(ConfirmationDialogFragment.ARG_TITLE, getString(R.string.account_card_archive));
        args.putString(ConfirmationDialogFragment.ARG_MESSAGE, getString(R.string.account_card_archive_info));
        dialog.setArguments(args);
        dialog.show(fm, TAG_ARCHIVE_ACCOUNT_DIALOG);
    }

    private void effectiveArchiveAccount(int accountId) {
        mCompositeDisposable.add(
                mListViewModel.archive(accountId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                integer ->
                                        mListViewModel.setTargetIdToArchive(0),
                                throwable ->
                                        Toast.makeText(getContext(), R.string.generic_data_error, Toast.LENGTH_LONG).show()));
    }

    @Override
    public void deleteAccount(Account account) {
        FragmentManager fm = getChildFragmentManager();
        if (fm.findFragmentByTag(TAG_DELETE_ACCOUNT_DIALOG) != null) return;
        mListViewModel.setTargetIdToDelete(account.getId());
        ConfirmationDialogFragment dialog = (ConfirmationDialogFragment)
                fm.getFragmentFactory().instantiate(
                        requireContext().getClassLoader(), ConfirmationDialogFragment.class.getName());
        Bundle args = new Bundle();
        args.putString(ConfirmationDialogFragment.ARG_TITLE, getString(R.string.account_card_delete));
        args.putString(ConfirmationDialogFragment.ARG_MESSAGE, getString(R.string.account_card_delete_warning));
        dialog.setArguments(args);
        dialog.show(fm, TAG_DELETE_ACCOUNT_DIALOG);
    }

    private void effectiveDeleteAccount(Account account) {
        mCompositeDisposable.add(
                mListViewModel.delete(account.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                integer ->
                                    mListViewModel.setTargetIdToDelete(0),
                                throwable ->
                                    Toast.makeText(getContext(), R.string.generic_data_error, Toast.LENGTH_LONG).show()));
    }

    @Override
    public void restoreAccount(Account account) {
        mCompositeDisposable.add(
                mListViewModel.restore(account.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                integer -> {},
                                throwable ->
                                        Toast.makeText(getContext(), R.string.generic_data_error, Toast.LENGTH_LONG).show()));
    }

    @Override
    public LiveData<List<Account>> getArchivedAccounts() {
        return mListViewModel.getArchivedAccounts();
    }

    @Override
    public void restoreAllAccounts() {
        mCompositeDisposable.add(
                mListViewModel.restoreAll()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                integer -> {},
                                throwable ->
                                        Toast.makeText(getContext(), R.string.generic_data_error, Toast.LENGTH_LONG).show()));
    }

    @Override
    public void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which) {
        if (dialogTag.equals(TAG_ARCHIVE_ACCOUNT_DIALOG)) {
            int idToArchive = mListViewModel.getTargetIdToArchive();
            if (idToArchive == 0)  {
                Log.wtf("AccountsFragment", "onConfirmationDialogYes: Trying to archive null object.");
                return;
            }
            effectiveArchiveAccount(idToArchive);
        } else if (dialogTag.equals(TAG_DELETE_ACCOUNT_DIALOG)) {
            int idToDelete = mListViewModel.getTargetIdToDelete();
            if (idToDelete == 0)  {
                Log.wtf("AccountsFragment", "onConfirmationDialogYes: Trying to delete null object.");
                return;
            }

            ArchivedAccountsDialogFragment archivedAccountsDialog
                    = (ArchivedAccountsDialogFragment) getChildFragmentManager()
                        .findFragmentByTag(ArchivedAccountsDialogFragment.TAG_ARCHIVED_ACCOUNTS_DIALOG);
            if (archivedAccountsDialog != null &&
                    Objects.requireNonNull(getArchivedAccounts().getValue()).size() == 1) {
                archivedAccountsDialog.dismiss();
            }

            effectiveDeleteAccount(new Account(idToDelete));
        }

    }

    @Override
    public void onConfirmationDialogNo(DialogInterface dialog, String dialogTag, int which) {
        if (dialogTag.equals(TAG_ARCHIVE_ACCOUNT_DIALOG)) {
            mListViewModel.setTargetIdToArchive(0);
        } else if (dialogTag.equals(TAG_DELETE_ACCOUNT_DIALOG)) {
            mListViewModel.setTargetIdToDelete(0);
        }
    }

    @Override
    public void onConfirmationDialogCancel(DialogInterface dialog, String dialogTag) {
    }

    @Override
    public void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag) {
        mAdapter.enableClicks();
    }
}