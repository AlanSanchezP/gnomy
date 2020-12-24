package io.github.alansanchezp.gnomy.ui.transaction;

import android.util.Log;
import android.view.View;

import com.xwray.groupie.viewbinding.BindableItem;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.databinding.LayoutTransactionGroupHeaderBinding;
import io.github.alansanchezp.gnomy.util.CurrencyUtil;
import io.github.alansanchezp.gnomy.util.GnomyCurrencyException;

public class TransactionGroupHeader
        extends BindableItem<LayoutTransactionGroupHeaderBinding> {
    private final String mTitle;
    private final BigDecimal mSum;

    public TransactionGroupHeader(String title, BigDecimal sum) {
        mTitle = title;
        mSum = sum;
    }

    @NonNull
    @Override
    protected LayoutTransactionGroupHeaderBinding initializeViewBinding(@NonNull View view) {
        return LayoutTransactionGroupHeaderBinding.bind(view);
    }

    @Override
    public void bind(@NonNull LayoutTransactionGroupHeaderBinding viewBinding, int position) {
        viewBinding.transactionGroupHeaderText.setText(mTitle);
        try {
            setSumText(viewBinding);
        } catch (GnomyCurrencyException e) {
            Log.wtf("TransactionGroupHeader", "bind: ", e);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.layout_transaction_group_header;
    }

    private void setSumText(LayoutTransactionGroupHeaderBinding viewBinding)
            throws GnomyCurrencyException {
        switch (mSum.compareTo(BigDecimal.ZERO)) {
            case -1:
                viewBinding.transactionGroupHeaderSum.setTextColor(
                        viewBinding.getRoot().getResources().getColor(R.color.colorExpensesDark));
                break;
            case 0:
                viewBinding.transactionGroupHeaderSum.setVisibility(View.INVISIBLE);
                break;
            case 1:
                viewBinding.transactionGroupHeaderSum.setTextColor(
                        viewBinding.getRoot().getResources().getColor(R.color.colorIncomesDark));
                break;
        }
        // TODO: Implement global default currency
        viewBinding.transactionGroupHeaderSum.setText("\u2211 = ".concat(
                CurrencyUtil.format(mSum, "USD")));
    }
}
