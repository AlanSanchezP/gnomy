package io.github.alansanchezp.gnomy.ui.account;

import androidx.annotation.NonNull;
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
            holder.setAccountData(mValues.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (mValues != null)
            return mValues.size();
        else return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // TODO: Test in isolation
        //  use https://proandroiddev.com/testing-views-in-isolation-at-romobos-d288e76fe10e

        private final View mView;
        private final TextView mNameView;
        private final TextView mCurrentView;
        private final TextView mCurrentLabelView;
        private final TextView mProjectedView;
        private final TextView mProjectedLabelView;
        private final ImageView mIconView;
        private final ImageButton mButton;
        private AccountWithBalance mItem;
        private PopupMenu popup;


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

        public void setAccountData(@NonNull AccountWithBalance awb) {
            mItem = awb;

            int accountColor = mItem.account.getBackgroundColor();
            int iconColor = ColorUtil.getTextColor(accountColor);
            int iconResId = Account.getDrawableResourceId(mItem.account.getType());
            Drawable icon = (Drawable) mView.getResources().getDrawable(iconResId);

            ((GradientDrawable) mIconView.getBackground()).setColor(accountColor);
            mIconView.setImageDrawable(icon);
            mIconView.setColorFilter(iconColor);
            mIconView.setTag(iconResId);

            mNameView.setText(mItem.account.getName());

            if (!mMonth.equals(YearMonth.now())) {
                mProjectedLabelView.setText(R.string.account_accumulated_balance);
            } else {
                mProjectedLabelView.setText(R.string.account_projected_balance);
            }

            try {
                mCurrentView.setText(
                        CurrencyUtil.format(
                                mItem.accumulatedBalance,
                                mItem.account.getDefaultCurrency()))
                ;
                mProjectedView.setText(
                        CurrencyUtil.format(
                                mItem.projectedBalance,
                                mItem.account.getDefaultCurrency())
                );
            } catch (GnomyCurrencyException e) {
                Log.wtf("AccountRecyclerViewA...", "onBindViewHolder: You somehow managed to store an invalid currency", e);
            }
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
