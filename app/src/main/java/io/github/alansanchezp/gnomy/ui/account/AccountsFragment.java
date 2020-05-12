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

import org.threeten.bp.YearMonth;

import java.math.BigDecimal;
import java.util.List;

import io.github.alansanchezp.gnomy.R;
import static io.github.alansanchezp.gnomy.database.GnomyTypeConverters.yearMonthToInt;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.ui.BaseMainNavigationFragment;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.AccountViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountsFragment extends BaseMainNavigationFragment
        implements AccountRecyclerViewAdapter.OnListItemInteractionListener {

    private RecyclerView mRecyclerView;
    private AccountRecyclerViewAdapter mAdapter;
    private AccountViewModel mAccountViewModel;
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

    public static AccountsFragment newInstance(int columnCount, int index, YearMonth month) {
        AccountsFragment fragment = new AccountsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_NAVIGATION_INDEX, index);
        args.putInt(ARG_MONTH, yearMonthToInt(month));
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
        mAccountViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getActivity().getApplication())).get(AccountViewModel.class);
        mAccountBalances = mAccountViewModel.getBalances();
        if (mAccountViewModel.getMonth() != null) {
            mCurrentMonth = null;
        }
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

        onMonthChanged(mCurrentMonth, view);
        setObserver();

        return view;
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
    }

    public void onMonthChanged(YearMonth month) {
        if (month == null) return;
        onMonthChanged(month, getView());
    }

    private void onMonthChanged(YearMonth month, View v) {
        if (month == null) {
            month = mAccountViewModel.getMonth();
        } else {
            mAccountViewModel.setMonth(month);
        }

        if (month.equals(YearMonth.now())) {
            ((TextView) v.findViewById(R.id.total_projected_lable)).setText(R.string.account_projected_balance);
        } else {
            ((TextView) v.findViewById(R.id.total_projected_lable)).setText(R.string.account_accumulated_balance);
        }

        mCurrentMonth = month;
    }

    public YearMonth getMonth() {
        return mCurrentMonth;
    }

    /* INTERFACE METHODS */

    public void onItemInteraction(Account account) { }

    public boolean onItemMenuItemInteraction(final Account account, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.account_card_details:
                break;
            case R.id.account_card_modify:
                Intent modifyAccountIntent = new Intent(getContext(), AddEditAccountActivity.class);
                modifyAccountIntent.putExtra("accountId", account.getId());
                modifyAccountIntent.putExtra("accountBgColor", account.getBackgroundColor());
                modifyAccountIntent.putExtra("accountName", account.getName());
                modifyAccountIntent.putExtra("accountInitialValue", account.getInitialValue().toPlainString());
                modifyAccountIntent.putExtra("accountIncludedInSum", account.isShowInDashboard());
                modifyAccountIntent.putExtra("accountCurrency", account.getDefaultCurrency());
                modifyAccountIntent.putExtra("accountType", account.getType());

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
                        .show();
                break;
            default:
                return false;
        }

        return true;
    }

    /* FRAGMENT-SPECIFIC METHODS */

    private void displayArchivedAccounts() {
        ArchivedAccountsDialogFragment dialog = new ArchivedAccountsDialogFragment();
        dialog.show(getFragmentManager(), ArchivedAccountsDialogFragment.TAG);
    }

    private void setObserver() {
        mAccountBalances.observe(getViewLifecycleOwner(), new Observer<List<AccountWithBalance>>() {
            @Override
            public void onChanged(@Nullable final List<AccountWithBalance> accounts) {
                mAdapter.setValues(accounts);
                BigDecimal balance = new BigDecimal("0");
                BigDecimal projected = new BigDecimal("0");

                // TODO: Handle conversion to global currency
                // This loop is here just so we can display something
                for (AccountWithBalance mb : accounts) {
                    balance = balance.add(mb.accumulatedBalance);
                    projected = projected.add(mb.projectedBalance);
                }

                try {
                    mBalance.setText(CurrencyUtil.format(balance, "USD"));
                    mProjected.setText(CurrencyUtil.format(projected, "USD"));
                } catch (GnomyCurrencyException e) {
                    // This shouldn't happen
                    Log.wtf("AccountsFragment", "setObserver: ", e);
                }
            }
        });
    }

    public void archiveAccount(Account account) {
        mAccountViewModel.archive(account);
    }

    public void deleteAccount(Account account) {
        mAccountViewModel.delete(account);
    }
}