package io.github.alansanchezp.gnomy.ui.account;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.threeten.bp.YearMonth;

import java.math.BigDecimal;
import java.util.List;

import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;
import io.github.alansanchezp.gnomy.viewmodel.AccountViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountsFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private RecyclerView mRecyclerView;
    private OnListFragmentInteractionListener mListener;
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
     * @return A new instance of fragment NewAccountFragment.
     */

    public static AccountsFragment newInstance(int columnCount) {
        AccountsFragment fragment = new AccountsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        mAccountViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getActivity().getApplication())).get(AccountViewModel.class);
        updateDataSet();
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
        setObserver();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
            mAdapter = new AccountRecyclerViewAdapter(mListener);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateDataSet() {
        // TODO: Implement filters here
        mAccountBalances = mAccountViewModel.getAllFromMonth(YearMonth.now());
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Account account);
        boolean onListFragmentMenuItemInteraction(Account account, MenuItem menuItem);
    }
}
