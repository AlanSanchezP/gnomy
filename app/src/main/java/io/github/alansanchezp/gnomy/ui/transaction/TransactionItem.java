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
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.data.transaction.TransactionDisplayData;
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
        int iconResId = context.getResources().getIdentifier(mItem.categoryResourceName, "drawable", context.getPackageName());
        int iconBgColor = mItem.categoryColor;
        int iconColor = ColorUtil.getTextColor(iconBgColor);

        $.transactionCardConcept.setText(mItem.transaction.getConcept());
        $.transactionCardAccount.setText(mFullData ? mItem.accountName : "");
        if (mItem.transaction.getType() == MoneyTransaction.TRANSFER)
            $.transactionCardAccount.append(" \u203A " + mItem.transferDestinationAccountName);
        try {
            setAmountText($);
        } catch (GnomyCurrencyException e) {
            Log.wtf("TransactionItem", "bind: ", e);
        }

        Drawable icon = ContextCompat.getDrawable(context, iconResId);
        $.transactionCardIcon.setImageDrawable(icon);
        ((GradientDrawable) $.transactionCardIcon.getBackground()).setColor(iconBgColor);
        $.transactionCardIcon.setColorFilter(iconColor);
        $.transactionCardIcon.setTag(iconResId);
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
        // XXX: [#53] Best format for incomes and expenses?
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
