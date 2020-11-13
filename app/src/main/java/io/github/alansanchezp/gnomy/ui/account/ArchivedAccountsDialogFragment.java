package io.github.alansanchezp.gnomy.ui.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;

public class ArchivedAccountsDialogFragment extends DialogFragment {
    public static final String TAG_ARCHIVED_ACCOUNTS_DIALOG = "ArchivedAccountsDialogFragment.Dialog";

    private ArchivedAccountsRecyclerViewAdapter mAdapter;
    private final ArchivedAccountsDialogInterface mListener;
    private SingleClickViewHolder<Button> mRestoreAllButtonVH;
    private TextView mEmptyListText;
    private int mListSize = -1;

    public ArchivedAccountsDialogFragment() {
        throw new IllegalArgumentException("This class must be provided with an ArchivedAccountsDialogInterface instance.");

    }

    public ArchivedAccountsDialogFragment(ArchivedAccountsDialogInterface _listener) {
        mListener = _listener;
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
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.title_archived_accounts));
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archived_accounts_dialog, container, false);
        Context context = view.getContext();

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

    private void onAccountsListChanged(List<Account> accounts) {
        int itemsCount = accounts.size();
        if (mListSize != -1 && itemsCount == 0) {
            dismiss();
            return;
        }

        if (itemsCount == 0) {
            mEmptyListText.setVisibility(View.VISIBLE);
            mRestoreAllButtonVH.onView(requireActivity(), v -> v.setVisibility(View.GONE));
        } else if (itemsCount == 1) {
            mEmptyListText.setVisibility(View.GONE);
            mRestoreAllButtonVH.onView(requireActivity(), v -> v.setVisibility(View.GONE));
        } else {
            mEmptyListText.setVisibility(View.GONE);
            mRestoreAllButtonVH.onView(requireActivity(), v -> v.setVisibility(View.VISIBLE));
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
