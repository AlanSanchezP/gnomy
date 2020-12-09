package io.github.alansanchezp.gnomy.ui.transaction;

import android.util.Log;
import android.view.View;

import com.xwray.groupie.viewbinding.BindableItem;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.databinding.FragmentTransactionCardBinding;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

public class TransactionItem
        extends BindableItem<FragmentTransactionCardBinding> {
    private final MoneyTransaction mItem;

    public TransactionItem(MoneyTransaction transaction) {
        mItem = transaction;
    }

    @NonNull
    @Override
    protected FragmentTransactionCardBinding initializeViewBinding(@NonNull View view) {
        return FragmentTransactionCardBinding.bind(view);
    }

    @Override
    public void bind(@NonNull FragmentTransactionCardBinding viewBinding, int position) {
        viewBinding.transactionCardConcept.setText(mItem.getConcept());
        try {
            setAmountText(viewBinding);
        } catch (GnomyCurrencyException e) {
            Log.wtf("TransactionItem", "bind: ", e);
        }
        viewBinding.transactionCardAccount.setText("" + mItem.getAccount());
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_transaction_card;
    }

    private void setAmountText(FragmentTransactionCardBinding viewBinding)
            throws GnomyCurrencyException {
        BigDecimal amount = mItem.getCalculatedValue();
        if (mItem.getType() == MoneyTransaction.INCOME) {
            viewBinding.transactionCardAmount.setTextColor(
                    viewBinding.getRoot().getResources().getColor(R.color.colorIncomesDark));
        } else if (mItem.getType() == MoneyTransaction.EXPENSE) {
            amount = amount.negate();
            viewBinding.transactionCardAmount.setTextColor(
                    viewBinding.getRoot().getResources().getColor(R.color.colorExpensesDark));
        }
        viewBinding.transactionCardAmount.setText(
                CurrencyUtil.format(amount, mItem.getCurrency()));
    }
}
