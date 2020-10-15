package io.github.alansanchezp.gnomy.ui.account;

import io.github.alansanchezp.gnomy.R;

import androidx.recyclerview.widget.RecyclerView;

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

import java.time.YearMonth;

import static io.github.alansanchezp.gnomy.database.account.Account.*;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountWithBalance;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AccountWithBalance} and makes a call to the
 * specified {@link OnListItemInteractionListener}.
 */
public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.ViewHolder> {

    private List<AccountWithBalance> mValues;
    private YearMonth mMonth;
    private final OnListItemInteractionListener mListener;
    private boolean mAllowClicks = true;

    public AccountRecyclerViewAdapter(OnListItemInteractionListener listener) {
        mListener = listener;
    }

    public void setValues(List<AccountWithBalance> accounts, YearMonth month) {
        mValues = accounts;
        mMonth = month;
        notifyDataSetChanged();
    }

    public void enableClicks() {
        mAllowClicks = true;
    }

    public void disableClicks() {
        mAllowClicks = false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_account_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mValues != null) {
            holder.mItem = mValues.get(position);

            // TODO: Move this logic to ViewHolder for testing purposes
            // https://proandroiddev.com/testing-views-in-isolation-at-romobos-d288e76fe10e
            holder.mNameView.setText(holder.mItem.account.getName());

            if (!mMonth.equals(YearMonth.now())) {
                holder.mProjectedLabelView.setText(R.string.account_accumulated_balance);
            } else {
                holder.mProjectedLabelView.setText(R.string.account_projected_balance);
            }

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
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_piggy_black_24dp);
                    holder.mIconView.setTag(R.drawable.ic_account_balance_piggy_black_24dp);
                    break;
                case SAVINGS:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_savings_black_24dp);
                    holder.mIconView.setTag(R.drawable.ic_account_balance_savings_black_24dp);
                    break;
                case INVERSIONS:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_inversion_black_24dp);
                    holder.mIconView.setTag(R.drawable.ic_account_balance_inversion_black_24dp);
                    break;
                case CREDIT_CARD:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_credit_card_black_24dp);
                    holder.mIconView.setTag(R.drawable.ic_account_balance_credit_card_black_24dp);
                    break;
                case OTHER:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_wallet_black_24dp);
                    holder.mIconView.setTag(R.drawable.ic_account_balance_wallet_black_24dp);
                    break;
                case BANK:
                default:
                    icon = (Drawable) holder.mView.getResources().getDrawable(R.drawable.ic_account_balance_black_24dp);
                    holder.mIconView.setTag(R.drawable.ic_account_balance_black_24dp);
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
        public final TextView mCurrentLabelView;
        public final TextView mProjectedView;
        public final TextView mProjectedLabelView;
        public final ImageView mIconView;
        public final ImageButton mButton;
        public AccountWithBalance mItem;
        public PopupMenu popup;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.account_card_name);
            mCurrentView = (TextView) view.findViewById(R.id.account_card_current);
            mCurrentLabelView = (TextView) view.findViewById(R.id.account_card_current_label);
            mProjectedView = (TextView) view.findViewById(R.id.account_card_projected);
            mProjectedLabelView = (TextView) view.findViewById(R.id.account_card_projected_label);
            mButton = (ImageButton) view.findViewById(R.id.account_card_button);
            mIconView = (ImageView) view.findViewById(R.id.account_card_icon);

            popup = new PopupMenu(mView.getContext(), mButton);
            popup.inflate(R.menu.account_card);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAllowClicks) {
                        disableClicks();
                        mListener.onItemInteraction(mItem.account);
                    }
                }
            });

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (mAllowClicks) {
                        disableClicks();
                        return mListener.onItemMenuItemInteraction(mItem.account, item);
                    }
                    return false;
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
    public interface OnListItemInteractionListener {
        void onItemInteraction(Account account);
        boolean onItemMenuItemInteraction(Account account, MenuItem menuItem);
    }
}
