package io.github.alansanchezp.gnomy.ui.account;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.github.alansanchezp.gnomy.R;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import java.math.BigDecimal;

import io.github.alansanchezp.gnomy.data.account.Account;
import io.github.alansanchezp.gnomy.data.account.AccountWithAccumulated;
import io.github.alansanchezp.gnomy.databinding.LayoutAccountCardBinding;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AccountWithAccumulated} and makes a call to the
 * specified {@link OnListItemInteractionListener}.
 */
public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.ViewHolder> {

    private List<AccountWithAccumulated> mValues;
    private final OnListItemInteractionListener mListener;
    private boolean mAllowClicks = true;
    private int mMapSize = 0;

    public AccountRecyclerViewAdapter(OnListItemInteractionListener listener) {
        mListener = listener;
    }

    public void notifyTodayAccumulatesAreAvailable(int mapSize) {
        mMapSize = mapSize;
        if (mValues != null && mValues.size() <= mMapSize) {
            notifyDataSetChanged();
        }
    }

    public void setValues(List<AccountWithAccumulated> accumulates) {
        mValues = accumulates;
        if (accumulates.size() <= mMapSize) {
            notifyDataSetChanged();
        }
    }

    public void enableClicks() {
        mAllowClicks = true;
    }

    public void disableClicks() {
        mAllowClicks = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutAccountCardBinding viewBinding = LayoutAccountCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(viewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (mValues != null) {
            AccountWithAccumulated targetMonthAccumulated = mValues.get(position);
            AccountWithAccumulated todayAccumulated =  mListener.getTodayAccumulatedFromAccount(
                    targetMonthAccumulated.account.getId()
            );
            holder.setAccountData(targetMonthAccumulated, todayAccumulated);
            // ClickDisablerInterface is needed: SingleClickViewHolder cannot be used here
            //  because blocked actions go beyond individual views scope.
            holder.setEventListeners(mListener, new ClickDisablerInterface() {
                @Override
                public void disableClicks() {
                    AccountRecyclerViewAdapter.this.disableClicks();
                }

                @Override
                public boolean clicksEnabled() {
                    return mAllowClicks;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mValues != null)
            return mValues.size();
        else return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private AccountWithAccumulated mItem;
        private final PopupMenu popup;
        private final LayoutAccountCardBinding $;

        public ViewHolder(LayoutAccountCardBinding viewBinding) {
            super(viewBinding.getRoot());
            $ = viewBinding;

            popup = new PopupMenu($.getRoot().getContext(), $.accountCardButton);
            popup.inflate(R.menu.account_card);
        }

        public void setAccountData(@NonNull AccountWithAccumulated targetAWA,
                                   @NonNull AccountWithAccumulated todayAWA) {
            Context context = $.getRoot().getContext();
            mItem = targetAWA;

            if (mItem.getUnresolvedTransactions() == null ||
                    mItem.getUnresolvedTransactions().compareTo(BigDecimal.ZERO) == 0) {
                $.accountCardAlertIcon.setVisibility(View.GONE);
            } else {
                $.accountCardAlertIcon.setVisibility(View.VISIBLE);
            }

            int accountColor = mItem.account.getBackgroundColor();
            int iconColor = ColorUtil.getTextColor(accountColor);
            int iconResId = context.getResources().getIdentifier(
                    mItem.account.getDrawableResourceName(), "drawable", context.getPackageName());
            Drawable icon = ContextCompat.getDrawable(context, iconResId);

            ((GradientDrawable) $.accountCardIcon.getBackground()).setColor(accountColor);
            $.accountCardIcon.setImageDrawable(icon);
            $.accountCardIcon.setColorFilter(iconColor);
            $.accountCardIcon.setTag(iconResId);

            $.accountCardName.setText(mItem.account.getName());

            if (targetAWA.targetMonth.isBefore(DateUtil.now())) {
                $.accountCardProjectedLabel.setText(R.string.account_balance_end_of_month);
            } else {
                $.accountCardProjectedLabel.setText(R.string.account_projected_balance);
            }

            try {
                $.accountCardCurrent.setText(
                        CurrencyUtil.format(
                                todayAWA.getConfirmedAccumulatedBalanceAtMonth(),
                                mItem.account.getDefaultCurrency()))
                ;
                $.accountCardProjected.setText(
                        CurrencyUtil.format(
                                mItem.getBalanceAtEndOfMonth(),
                                mItem.account.getDefaultCurrency())
                );
            } catch (GnomyCurrencyException e) {
                Log.wtf("AccountRecyclerViewA...", "onBindViewHolder: You somehow managed to store an invalid currency", e);
            }
        }

        private void setEventListeners(OnListItemInteractionListener listener,
                                       ClickDisablerInterface clickInterface) {
            // TODO: Find a way to test if clicks are effectively disabled
            $.getRoot().setOnClickListener(v -> {
                if (clickInterface.clicksEnabled()) {
                    clickInterface.disableClicks();
                    listener.onItemInteraction(mItem.account);
                }
            });

            $.accountCardAlertIcon.setOnClickListener(v -> {
                if (clickInterface.clicksEnabled()) {
                    clickInterface.disableClicks();
                    listener.onUnresolvedTransactions(mItem.account);
                }
            });

            popup.setOnMenuItemClickListener(item -> {
                if (clickInterface.clicksEnabled()) {
                    clickInterface.disableClicks();
                    return listener.onItemMenuItemInteraction(mItem.account, item);
                }
                return false;
            });
            $.accountCardButton.setOnClickListener(v -> popup.show());
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
        AccountWithAccumulated getTodayAccumulatedFromAccount(int accountId);
        void onItemInteraction(Account account);
        boolean onItemMenuItemInteraction(Account account, MenuItem menuItem);
        void onUnresolvedTransactions(Account account);
    }

    private interface ClickDisablerInterface {
        void disableClicks();
        boolean clicksEnabled();
    }
}
