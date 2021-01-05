package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;

import com.xwray.groupie.viewbinding.BindableItem;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.databinding.LayoutTransactionCardBinding;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

public class TransactionItem
        extends BindableItem<LayoutTransactionCardBinding> {
    private final TransactionDisplayData mItem;
    private final boolean mFullData;

    /**
     *
     * @param item              Item to use.
     * @param displayAllData    If set to false, account name and status
     *                          won't be displayed. In the case of transfers, only
     *                          destination account will be displayed.
     */
    public TransactionItem(TransactionDisplayData item, boolean displayAllData) {
        mItem = item;
        mFullData = displayAllData;
    }

    @NonNull
    @Override
    protected LayoutTransactionCardBinding initializeViewBinding(@NonNull View view) {
        return LayoutTransactionCardBinding.bind(view);
    }

    @Override
    public void bind(@NonNull LayoutTransactionCardBinding $, int position) {
        Context context = $.getRoot().getContext();
        int iconResId;
        int iconBgColor;
        int iconColor;

        $.transactionCardConcept.setText(mItem.transaction.getConcept());
        $.transactionCardAccount.setText(mFullData ? mItem.accountName : "");
        if (mItem.transaction.getType() == MoneyTransaction.TRANSFER) {
            iconResId = R.drawable.ic_compare_arrows_black_24dp;
            iconBgColor = ContextCompat.getColor(context, R.color.colorTransfers);
            $.transactionCardAccount.append(" \u203A " + mItem.transferDestinationAccountName);
        } else {
            iconResId = context.getResources().getIdentifier(mItem.categoryResourceName, "drawable", context.getPackageName());
            iconBgColor = mItem.categoryColor;
        }
        try {
            setAmountText($);
        } catch (GnomyCurrencyException e) {
            Log.wtf("TransactionItem", "bind: ", e);
        }
        iconColor = ColorUtil.getTextColor(iconBgColor);
        Drawable icon = ContextCompat.getDrawable(context, iconResId);
        $.transactionCardIcon.setImageDrawable(icon);
        ((GradientDrawable) $.transactionCardIcon.getBackground()).setColor(iconBgColor);
        $.transactionCardIcon.setColorFilter(iconColor);
        $.transactionCardIcon.setTag(iconResId);
        // TODO: Is this the best condition to use?
        if (!mItem.transaction.isConfirmed() && mFullData)
            $.transactionCardAlertIcon.setVisibility(View.VISIBLE);
        else
            $.transactionCardAlertIcon.setVisibility(View.GONE);
    }

    @Override
    public int getLayout() {
        return R.layout.layout_transaction_card;
    }

    private void setAmountText(LayoutTransactionCardBinding $)
            throws GnomyCurrencyException {
        // TODO: Best format for incomes and expenses?
        //  Options are:
        //      a) + for incomes, - for expenses (current implementation)
        //      b) nothing for incomes, () for expenses
        //      c) rising/falling arrows (drawables)
        BigDecimal amount = mItem.transaction.getCalculatedValue();
        if (mItem.transaction.getType() == MoneyTransaction.INCOME) {
            $.transactionCardAmount.setTextColor(
                    $.getRoot().getResources().getColor(R.color.colorIncomesDark));
        } else if (mItem.transaction.getType() == MoneyTransaction.EXPENSE) {
            amount = amount.negate();
            $.transactionCardAmount.setTextColor(
                    $.getRoot().getResources().getColor(R.color.colorExpensesDark));
        } else {
            $.transactionCardAmount.setTextColor(
                    $.getRoot().getResources().getColor(R.color.colorTextSecondary));
        }
        $.transactionCardAmount.setText(
                CurrencyUtil.format(amount, mItem.transaction.getCurrency()));
    }

    public int getTransactionId() {
        return mItem.transaction.getId();
    }

    public int getTransactionType() {
        return mItem.transaction.getType();
    }
}
