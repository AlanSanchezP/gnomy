package io.github.alansanchezp.gnomy.ui.account;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;

public class ArchivedAccountsDialogFragment extends DialogFragment {
    public static final String TAG = "ARCHIVED-ACCOUNTS-DIALOG";

    private ArchivedAccountsRecyclerViewAdapter mAdapter;
    private ArchivedAccountsDialogInterface mListener;
    private SingleClickViewHolder<Button> mRestoreAllButtonVH;
    private TextView mEmptyListText;
    private int mListSize = -1;

    public ArchivedAccountsDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            Objects.requireNonNull(requireDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            requireDialog().setTitle(getString(R.string.title_archived_accounts));
        } catch(IllegalStateException ise) {
            Log.w("ArchivedAccountsDialog", "onStart: This should only happen during tests!", ise);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archived_accounts_dialog, container, false);
        Context context = view.getContext();

        try {
            if (getParentFragment() == null) {
                mListener = (ArchivedAccountsDialogInterface) context;
            } else {
                mListener = (ArchivedAccountsDialogInterface) getParentFragment();
            }
        } catch (NullPointerException npe) {
            Log.wtf("ArchivedAccountsDF", "onCreateView(): ", npe);
            throw new ClassCastException(npe.getMessage());
        } catch (ClassCastException cce) {
            Log.w("ArchivedAccountsDF", "onCreateView(): No listener was provided. Fallback to dummy listener.", cce);
            mListener = new ArchivedAccountsDialogInterface() {
                @Override
                public LiveData<List<Account>> getArchivedAccounts() {
                    MutableLiveData<List<Account>> mlv = new MutableLiveData<>();
                    mlv.setValue(new ArrayList<>());
                    return (LiveData<List<Account>>) mlv;
                }

                @Override
                public void restoreAllAccounts() {
                }

                @Override
                public void restoreAccount(Account account) {
                }

                @Override
                public void deleteAccount(Account account) {
                }
            };
        }

        mAdapter = new ArchivedAccountsRecyclerViewAdapter(mListener);

        RecyclerView recyclerView = view.findViewById(R.id.archived_items_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAdapter);

        mRestoreAllButtonVH = new SingleClickViewHolder<>(view.findViewById(R.id.restore_all_accounts_button));
        mRestoreAllButtonVH.setOnClickListener(v -> {
            mListener.restoreAllAccounts();
            dismiss();
        });

        mEmptyListText = view.findViewById(R.id.archived_items_empty);

        mListener.getArchivedAccounts()
                .observe(getViewLifecycleOwner(), this::onAccountsListChanged);

        return view;
    }

    public void onAccountsListChanged(List<Account> accounts) {
        int itemsCount = accounts.size();
        if (mListSize != -1 && itemsCount == 0) {
            dismiss();
            return;
        }

        if (itemsCount == 0) {
            mEmptyListText.setVisibility(View.VISIBLE);
            mRestoreAllButtonVH.onView(v -> v.setVisibility(View.GONE));
        } else if (itemsCount == 1) {
            mEmptyListText.setVisibility(View.GONE);
            mRestoreAllButtonVH.onView(v -> v.setVisibility(View.GONE));
        } else {
            mEmptyListText.setVisibility(View.GONE);
            mRestoreAllButtonVH.onView(v -> v.setVisibility(View.VISIBLE));
        }

        mListSize = itemsCount;
        mAdapter.setValues(accounts);
    }

    public interface ArchivedAccountsDialogInterface
            extends ArchivedAccountsRecyclerViewAdapter.OnArchivedItemInteractionListener {
        LiveData<List<Account>> getArchivedAccounts();
        void restoreAllAccounts();
    }
}
