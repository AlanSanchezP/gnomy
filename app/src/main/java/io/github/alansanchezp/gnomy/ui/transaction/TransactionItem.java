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
import io.github.alansanchezp.gnomy.databinding.FragmentTransactionCardBinding;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

public class TransactionItem
        extends BindableItem<FragmentTransactionCardBinding> {
    private final TransactionDisplayData mItem;

    public TransactionItem(TransactionDisplayData item) {
        mItem = item;
    }

    @NonNull
    @Override
    protected FragmentTransactionCardBinding initializeViewBinding(@NonNull View view) {
        return FragmentTransactionCardBinding.bind(view);
    }

    @Override
    public void bind(@NonNull FragmentTransactionCardBinding viewBinding, int position) {
        Context context = viewBinding.getRoot().getContext();
        int iconResId;
        int iconBgColor;
        int iconColor;

        viewBinding.transactionCardConcept.setText(mItem.transaction.getConcept());
        viewBinding.transactionCardAccount.setText(mItem.accountName);
        if (mItem.transaction.getType() == MoneyTransaction.TRANSFER) {
            viewBinding.transactionCardAccount.append(" \u203A " + mItem.transferDestinationAccountName);
            iconResId = R.drawable.ic_compare_arrows_black_24dp;
            iconBgColor = ContextCompat.getColor(context, R.color.colorTransfers);
        } else {
            iconResId = context.getResources().getIdentifier(mItem.categoryResourceName, "drawable", context.getPackageName());
            iconBgColor = mItem.categoryColor;
        }
        try {
            setAmountText(viewBinding);
        } catch (GnomyCurrencyException e) {
            Log.wtf("TransactionItem", "bind: ", e);
        }
        iconColor = ColorUtil.getTextColor(iconBgColor);
        Drawable icon = ContextCompat.getDrawable(context, iconResId);
        viewBinding.transactionCardIcon.setImageDrawable(icon);
        ((GradientDrawable) viewBinding.transactionCardIcon.getBackground()).setColor(iconBgColor);
        viewBinding.transactionCardIcon.setColorFilter(iconColor);
        viewBinding.transactionCardIcon.setTag(iconResId);
        // TODO: Is this the best condition to use?
        if (!mItem.transaction.isConfirmed())
            viewBinding.transactionCardAlertIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_transaction_card;
    }

    private void setAmountText(FragmentTransactionCardBinding viewBinding)
            throws GnomyCurrencyException {
        // TODO: Best format for incomes and expenses?
        //  Options are:
        //      a) + for incomes, - for expenses (current implementation)
        //      b) nothing for incomes, () for expenses
        //      c) rising/falling arrows (drawables)
        BigDecimal amount = mItem.transaction.getCalculatedValue();
        if (mItem.transaction.getType() == MoneyTransaction.INCOME) {
            viewBinding.transactionCardAmount.setTextColor(
                    viewBinding.getRoot().getResources().getColor(R.color.colorIncomesDark));
        } else if (mItem.transaction.getType() == MoneyTransaction.EXPENSE) {
            amount = amount.negate();
            viewBinding.transactionCardAmount.setTextColor(
                    viewBinding.getRoot().getResources().getColor(R.color.colorExpensesDark));
        } else {
            viewBinding.transactionCardAmount.setTextColor(
                    viewBinding.getRoot().getResources().getColor(R.color.colorTextSecondary));
        }
        viewBinding.transactionCardAmount.setText(
                CurrencyUtil.format(amount, mItem.transaction.getCurrency()));
    }

    public int getTransactionId() {
        return mItem.transaction.getId();
    }

    public int getTransactionType() {
        return mItem.transaction.getType();
    }
}
