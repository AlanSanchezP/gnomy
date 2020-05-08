package io.github.alansanchezp.gnomy.ui.account;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.account.Account;

import static io.github.alansanchezp.gnomy.database.account.Account.BANK;
import static io.github.alansanchezp.gnomy.database.account.Account.CREDIT_CARD;
import static io.github.alansanchezp.gnomy.database.account.Account.INFORMAL;
import static io.github.alansanchezp.gnomy.database.account.Account.INVERSIONS;
import static io.github.alansanchezp.gnomy.database.account.Account.OTHER;
import static io.github.alansanchezp.gnomy.database.account.Account.SAVINGS;

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_archived_account_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mValues != null) {
            Drawable icon;

            holder.mItem = mValues.get(position);
            holder.mNameView.setText(holder.mItem.getName());

            switch (holder.mItem.getType()) {
                case INFORMAL:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_piggy_black_24dp);
                    break;
                case SAVINGS:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_savings_black_24dp);
                    break;
                case INVERSIONS:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_inversion_black_24dp);
                    break;
                case CREDIT_CARD:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_credit_card_black_24dp);
                    break;
                case OTHER:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_wallet_black_24dp);
                    break;
                case BANK:
                default:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_black_24dp);
                    break;
            }

            holder.mIconView.setImageDrawable(icon);
            holder.mIconView.setColorFilter(holder.mItem.getBackgroundColor());
        }
    }

    @Override
    public int getItemCount() {
        if (mValues != null)
            return mValues.size();
        else return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public Account mItem;
        public View mView;

        public final TextView mNameView;
        public final ImageView mIconView;
        private final Button mRestoreButton;
        private final Button mDeleteButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.archived_account_card_name);
            mIconView = (ImageView) view.findViewById(R.id.archived_account_card_icon);
            mRestoreButton = (Button) view.findViewById(R.id.archived_account_restore_button);
            mDeleteButton = (Button) view.findViewById(R.id.archived_account_delete_button);

            mRestoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.restoreAccount(mItem);
                }
            });

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.deleteAccount(mItem);
                }
            });
        }
    }


    public interface OnArchivedItemInteractionListener {
        void restoreAccount(Account account);
        void deleteAccount(Account account);
    }
}
