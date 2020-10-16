package io.github.alansanchezp.gnomy.ui.account;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.viewmodel.account.AccountsListViewModel;

public class ArchivedAccountsDialogFragment extends DialogFragment
        implements ArchivedAccountsRecyclerViewAdapter.OnArchivedItemInteractionListener {
    public static String TAG = "ARCHIVED-ACCOUNTS-DIALOG";

    private RecyclerView mRecyclerView;
    private ArchivedAccountsRecyclerViewAdapter mAdapter;
    private LiveData<List<Account>> mAccounts;
    private AccountsListViewModel mListViewModel;
    private Button mRestoreAllButton;
    private TextView mEmptyListText;
    private int mListSize = -1;

    public ArchivedAccountsDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ArchivedAccountsRecyclerViewAdapter.OnArchivedItemInteractionListener listener = (ArchivedAccountsRecyclerViewAdapter.OnArchivedItemInteractionListener) this;
        mAdapter = new ArchivedAccountsRecyclerViewAdapter(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getActivity().getApplication())).get(AccountsListViewModel.class);
        mAccounts = mListViewModel.getArchivedAccounts();

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().setTitle(getString(R.string.title_archived_accounts));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archived_accounts_dialog, container, false);
        Context context = view.getContext();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.archived_items_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(mAdapter);

        mRestoreAllButton = (Button) view.findViewById(R.id.restore_all_accounts_button);
        mRestoreAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListViewModel.restoreAll();
                dismiss();
            }
        });

        mEmptyListText = (TextView) view.findViewById(R.id.archived_items_empty);

        mAccounts.observe(getViewLifecycleOwner(), new Observer<List<Account>>() {
            @Override
            public void onChanged(@Nullable final List<Account> accounts) {
                int itemsCount = accounts.size();

                if (mListSize != -1 && itemsCount == 0) {
                    dismiss();
                    return;
                }

                switch (itemsCount) {
                    case 0:
                        mEmptyListText.setVisibility(View.VISIBLE);
                        mRestoreAllButton.setVisibility(View.GONE);
                    case 1:
                        mEmptyListText.setVisibility(View.GONE);
                        mRestoreAllButton.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mRestoreAllButton.setVisibility(View.VISIBLE);
                }

                if (itemsCount == 0) {
                    mEmptyListText.setVisibility(View.VISIBLE);
                    mRestoreAllButton.setVisibility(View.GONE);
                } else if (itemsCount == 1) {
                    mEmptyListText.setVisibility(View.GONE);
                    mRestoreAllButton.setVisibility(View.GONE);
                } else {
                    mEmptyListText.setVisibility(View.GONE);
                    mRestoreAllButton.setVisibility(View.VISIBLE);
                }

                mListSize = itemsCount;
                mAdapter.setValues(accounts);
            }
        });


        return view;
    }

    public void restoreAccount(Account account) {
        mListViewModel.restore(account);
    }

    public void deleteAccount(final Account account) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.account_card_delete))
                .setMessage(getString(R.string.account_card_delete_warning))
                .setPositiveButton(getString(R.string.confirmation_dialog_yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListViewModel.delete(account);
                    }
                })
                .setNegativeButton(getString(R.string.confirmation_dialog_no), null)
                .show();
    }
}
