package io.github.alansanchezp.gnomy.ui.account;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.ui.CustomDialogFragmentFactory;
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_archived_account_card, parent, false);
        return new ViewHolder(view);
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
        private final View mView;

        private final TextView mNameView;
        private final ImageView mIconView;
        private final Button mRestoreButton;
        private final Button mDeleteButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.archived_account_card_name);
            mIconView = view.findViewById(R.id.archived_account_card_icon);
            mRestoreButton = view.findViewById(R.id.archived_account_restore_button);
            mDeleteButton = view.findViewById(R.id.archived_account_delete_button);
        }

        public void setAccountData(@NonNull Account account) {
            mItem = account;

            int iconResId = Account.getDrawableResourceId(mItem.getType());
            Drawable icon = ContextCompat.getDrawable(mView.getContext(), iconResId);

            mNameView.setText(mItem.getName());
            mIconView.setImageDrawable(icon);
            mIconView.setColorFilter(mItem.getBackgroundColor());
            mIconView.setTag(iconResId);
        }

        private void setEventListeners(OnArchivedItemInteractionListener listener) {
            SingleClickViewHolder<Button> restoreVH = new SingleClickViewHolder<>(mRestoreButton);
            SingleClickViewHolder<Button> deleteVH = new SingleClickViewHolder<>(mDeleteButton);
            // TODO: Handle async nature of these calls
            restoreVH.setOnClickListener(v -> listener.restoreAccount(mItem));
            deleteVH.setOnClickListener(v -> listener.deleteAccount(mItem));
        }
    }


    public interface OnArchivedItemInteractionListener
            extends CustomDialogFragmentFactory.CustomDialogFragmentInterface {
        void restoreAccount(Account account);
        void deleteAccount(Account account);
    }
}
