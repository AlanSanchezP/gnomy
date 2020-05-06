package io.github.alansanchezp.gnomy.ui.account;

import io.github.alansanchezp.gnomy.R;

import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import static io.github.alansanchezp.gnomy.database.account.Account.*;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.ui.account.AccountsFragment.OnListFragmentInteractionListener;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AccountWithBalance} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.ViewHolder> {

    private List<AccountWithBalance> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Resources resources;

    public AccountRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }

    public void setValues(List<AccountWithBalance> accounts) {
        mValues = accounts;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        resources = parent.getResources();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_account_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mValues != null) {
            holder.mItem = mValues.get(position);
            holder.mNameView.setText(holder.mItem.account.getName());
            try {
                holder.mCurrentView.setText(CurrencyUtil.format(holder.mItem.accumulatedBalance, holder.mItem.account.getDefaultCurrency()));
                holder.mProjectedView.setText(CurrencyUtil.format(holder.mItem.projectedBalance, holder.mItem.account.getDefaultCurrency()));
            } catch (GnomyCurrencyException e) {
                Log.wtf("AccountRecyclerViewA...", "onBindViewHolder: You somehow managed to store an invalid currency", e);
            }

            int accountColor = holder.mItem.account.getBackgroundColor();
            int iconColor = ColorUtil.getTextColor(accountColor);
            Drawable icon;

            switch (holder.mItem.account.getType()) {
                case INFORMAL:
                    icon = (Drawable) resources.getDrawable(R.drawable.ic_account_balance_piggy_black_24dp);
                    break;
                case SAVINGS:
                    icon = (Drawable) resources.getDrawable(R.drawable.ic_account_balance_savings_black_24dp);
                    break;
                case INVERSIONS:
                    icon = (Drawable) resources.getDrawable(R.drawable.ic_account_balance_inversion_black_24dp);
                    break;
                case CREDIT_CARD:
                    icon = (Drawable) resources.getDrawable(R.drawable.ic_account_balance_credit_card_black_24dp);
                    break;
                case OTHER:
                    icon = (Drawable) resources.getDrawable(R.drawable.ic_account_balance_wallet_black_24dp);
                    break;
                case BANK:
                default:
                    icon = (Drawable) resources.getDrawable(R.drawable.ic_account_balance_black_24dp);
                    break;
            }

            ((GradientDrawable) holder.mIconView.getBackground()).setColor(accountColor);
            holder.mIconView.setImageDrawable(icon);
            holder.mIconView.setColorFilter(iconColor);
        }
    }

    @Override
    public int getItemCount() {
        if (mValues != null)
            return mValues.size();
        else return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mNameView;
        public final TextView mCurrentView;
        public final TextView mProjectedView;
        public final ImageView mIconView;
        public final ImageButton mButton;
        public AccountWithBalance mItem;
        public PopupMenu popup;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.account_card_name);
            mCurrentView = (TextView) view.findViewById(R.id.account_card_current);
            mProjectedView = (TextView) view.findViewById(R.id.account_card_projected);
            mButton = (ImageButton) view.findViewById(R.id.account_card_button);
            mIconView = (ImageView) view.findViewById(R.id.account_card_icon);

            popup = new PopupMenu(mView.getContext(), mButton);
            popup.inflate(R.menu.account_card);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return mListener.onListFragmentMenuItemInteraction(mItem.account, item);
                }
            });
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup.show();
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
