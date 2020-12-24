package io.github.alansanchezp.gnomy.ui.account;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.databinding.LayoutArchivedAccountCardBinding;
import io.github.alansanchezp.gnomy.util.android.SingleClickViewHolder;

public class ArchivedAccountsRecyclerViewAdapter
        extends RecyclerView.Adapter<ArchivedAccountsRecyclerViewAdapter.ViewHolder>{
    private List<Account> mValues;
    private final OnArchivedItemInteractionListener mListener;

    public ArchivedAccountsRecyclerViewAdapter(OnArchivedItemInteractionListener listener) {
        mListener = listener;
    }

    public void setValues(List<Account> accounts) {
        mValues = accounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutArchivedAccountCardBinding viewBinding = LayoutArchivedAccountCardBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(viewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (mValues != null) {
            holder.setAccountData(mValues.get(position));
            holder.setEventListeners(mListener);
        }
    }

    @Override
    public int getItemCount() {
        if (mValues != null)
            return mValues.size();
        else return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private Account mItem;
        private final LayoutArchivedAccountCardBinding $;

        public ViewHolder(LayoutArchivedAccountCardBinding viewBinding) {
            super(viewBinding.getRoot());
            $ = viewBinding;
        }

        public void setAccountData(@NonNull Account account) {
            mItem = account;

            int iconResId = Account.getDrawableResourceId(mItem.getType());
            Drawable icon = ContextCompat.getDrawable($.getRoot().getContext(), iconResId);

            $.archivedAccountCardName.setText(mItem.getName());
            $.archivedAccountCardIcon.setImageDrawable(icon);
            $.archivedAccountCardIcon.setColorFilter(mItem.getBackgroundColor());
            $.archivedAccountCardIcon.setTag(iconResId);
        }

        private void setEventListeners(OnArchivedItemInteractionListener listener) {
            SingleClickViewHolder<Button> restoreVH = new SingleClickViewHolder<>($.archivedAccountRestoreButton);
            SingleClickViewHolder<Button> deleteVH = new SingleClickViewHolder<>($.archivedAccountDeleteButton);
            restoreVH.setOnClickListener(v -> listener.restoreAccount(mItem));
            deleteVH.setOnClickListener(v -> listener.deleteAccount(mItem));
        }
    }


    public interface OnArchivedItemInteractionListener {
        void restoreAccount(Account account);
        void deleteAccount(Account account);
    }
}
