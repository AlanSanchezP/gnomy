package io.github.alansanchezp.gnomy.ui.account;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateViewModelFactory;
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
import android.widget.Toast;

import java.time.YearMonth;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.ui.ConfirmationDialogFragment;
import io.github.alansanchezp.gnomy.ui.CustomDialogFragmentFactory;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountsListViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountsFragment extends MainNavigationFragment
        implements AccountRecyclerViewAdapter.OnListItemInteractionListener,
        ArchivedAccountsDialogFragment.ArchivedAccountsDialogInterface,
        ConfirmationDialogFragment.OnConfirmationDialogListener {

    public static final String TAG_ARCHIVE_ACCOUNT_DIALOG = "AccountsFragment.ArchiveAccountDialog";
    public static final String TAG_DELETE_ACCOUNT_DIALOG = "AccountsFragment.DeleteAccountDialog";
    private AccountRecyclerViewAdapter mAdapter;
    private AccountsListViewModel mListViewModel;
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

    private Map<Class<? extends Fragment>, CustomDialogFragmentFactory.CustomDialogFragmentInterface>
    getInterfacesMapping() {
        Map<Class<? extends Fragment>, CustomDialogFragmentFactory.CustomDialogFragmentInterface>
                interfacesMapping = new HashMap<>();
        interfacesMapping.put(
                ArchivedAccountsDialogFragment.class, this);
        interfacesMapping.put(
                ConfirmationDialogFragment.class, this);
        return interfacesMapping;
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
        getChildFragmentManager().setFragmentFactory(
                new CustomDialogFragmentFactory(getInterfacesMapping()));
        super.onCreate(savedInstanceState);
        mListViewModel = new ViewModelProvider(this,
                new SavedStateViewModelFactory(
                        this.requireActivity().getApplication(),
                        this.requireActivity()))
                .get(AccountsListViewModel.class);
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

        mListViewModel.bindMonth(mNavigationInterface.getActiveMonth());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBalance = view.findViewById(R.id.total_balance);
        mProjected = view.findViewById(R.id.total_projected);

        mNavigationInterface.getActiveMonth()
                .observe(getViewLifecycleOwner(), this::onMonthChanged);
        mListViewModel.getBalances()
                .observe(getViewLifecycleOwner(), this::onAccountsListChanged);
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

    private void onAccountsListChanged(List<AccountWithBalance> accounts) {
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
                mListViewModel.delete(account)
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