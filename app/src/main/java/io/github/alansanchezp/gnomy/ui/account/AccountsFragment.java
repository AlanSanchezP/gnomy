package io.github.alansanchezp.gnomy.ui.account;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
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
import io.github.alansanchezp.gnomy.ui.BaseMainNavigationFragment;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.AccountsListViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountsFragment extends BaseMainNavigationFragment
        implements AccountRecyclerViewAdapter.OnListItemInteractionListener {

    private RecyclerView mRecyclerView;
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
    public void onAttach(Context context) {
        super.onAttach(context);
        AccountRecyclerViewAdapter.OnListItemInteractionListener listener = (AccountRecyclerViewAdapter.OnListItemInteractionListener) this;
        mAdapter = new AccountRecyclerViewAdapter(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getActivity().getApplication())).get(AccountsListViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        Context context = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.items_list);

        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);
        mBalance = (TextView) view.findViewById(R.id.total_balance);
        mProjected = (TextView) view.findViewById(R.id.total_projected);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mListViewModel.shouldInitMonthFilter()) {
            mListViewModel.initMonthFilter(mNavigationInterface.getSelectedMonth());
        }
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

    protected boolean displaySecondaryToolbar() {
        return true;
    }

    protected int getAppbarColor() {
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
        getActivity().startActivity(newAccountIntent);
        mAdapter.disableClicks();
    }

    public void onMonthChanged(YearMonth month) {
        if (month == null) return;
        View v = getView();
        if (month.equals(YearMonth.now())) {
            ((TextView) v.findViewById(R.id.total_projected_label)).setText(R.string.account_projected_balance);
        } else {
            ((TextView) v.findViewById(R.id.total_projected_label)).setText(R.string.account_accumulated_balance);
        }

        mCurrentMonth = month;
    }

    public void onAccountsListChanged(List<AccountWithBalance> accounts) {
        if (mCurrentMonth == null) return;
        mAdapter.setValues(accounts, mCurrentMonth);
        BigDecimal balance = new BigDecimal("0");
        BigDecimal projected = null;

        // This loop is here just so we can display something
        // TODO: Calculate proper value once global user currency is implemented
        for (AccountWithBalance mb : accounts) {
            if (mb.projectedBalance != null) {
                if (projected == null) projected = new BigDecimal("0");
                projected = projected.add(mb.projectedBalance);
            }
            balance = balance.add(mb.accumulatedBalance);
        }

        try {
            // TODO: Use global user currency when implemented
            mBalance.setText(CurrencyUtil.format(balance, "USD"));
            mProjected.setText(CurrencyUtil.format(projected, "USD"));
        } catch (GnomyCurrencyException e) {
            // This shouldn't happen
            Log.wtf("AccountsFragment", "setObserver: ", e);
        }
    }

    /* INTERFACE METHODS */

    public void onItemInteraction(Account account) {
        Intent detailsIntent = new Intent(getContext(), AccountDetailsActivity.class);
        detailsIntent.putExtra(AccountDetailsActivity.EXTRA_ID, account.getId());
        detailsIntent.putExtra(AccountDetailsActivity.EXTRA_BG_COLOR, account.getBackgroundColor());

        getActivity().startActivity(detailsIntent);
    }

    public boolean onItemMenuItemInteraction(final Account account, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.account_card_details:
                onItemInteraction(account);
                break;
            case R.id.account_card_modify:
                Intent modifyAccountIntent = new Intent(getContext(), AddEditAccountActivity.class);
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_ID, account.getId());
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_BG_COLOR, account.getBackgroundColor());
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_NAME, account.getName());
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_INITIAL_VALUE, account.getInitialValue().toPlainString());
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_INCLUDED_IN_SUM, account.isShowInDashboard());
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_CURRENCY, account.getDefaultCurrency());
                modifyAccountIntent.putExtra(AddEditAccountActivity.EXTRA_TYPE, account.getType());

                getActivity().startActivity(modifyAccountIntent);
                break;
            case R.id.account_card_transactions:
                break;
            case R.id.account_card_archive:
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.account_card_archive))
                        .setMessage(getString(R.string.account_card_archive_info))
                        .setPositiveButton(getString(R.string.confirmation_dialog_yes), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                archiveAccount(account);
                            }
                        })
                        .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener()
                        {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mAdapter.enableClicks();
                            }
                        })
                        .show();
                break;
            case R.id.account_card_delete:
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.account_card_delete))
                        .setMessage(getString(R.string.account_card_delete_warning))
                        .setPositiveButton(getString(R.string.confirmation_dialog_yes), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAccount(account);
                            }
                        })
                        .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener()
                        {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mAdapter.enableClicks();
                            }
                        })
                        .show();
                break;
            default:
                mAdapter.enableClicks();
                return false;
        }

        return true;
    }

    /* FRAGMENT-SPECIFIC METHODS */

    private void displayArchivedAccounts() {
        ArchivedAccountsDialogFragment dialog = new ArchivedAccountsDialogFragment();
        dialog.show(getFragmentManager(), ArchivedAccountsDialogFragment.TAG);
    }

    private void setObservers() {
        mNavigationInterface.getSelectedMonth().observe(getViewLifecycleOwner(), new Observer<YearMonth>() {
            @Override
            public void onChanged(@Nullable final YearMonth month) {
                onMonthChanged(month);
            }
        });

        mAccountBalances.observe(getViewLifecycleOwner(), new Observer<List<AccountWithBalance>>() {
            @Override
            public void onChanged(@Nullable final List<AccountWithBalance> accounts) {
                onAccountsListChanged(accounts);
            }
        });
    }

    public void archiveAccount(Account account) {
        mListViewModel.archive(account);
    }

    public void deleteAccount(Account account) {
        mListViewModel.delete(account);
    }
}