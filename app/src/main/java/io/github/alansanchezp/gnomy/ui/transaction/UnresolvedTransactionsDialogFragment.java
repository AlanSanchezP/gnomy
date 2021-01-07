package io.github.alansanchezp.gnomy.ui.transaction;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Section;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.databinding.DialogUnresolvedTransactionsBinding;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;

public class UnresolvedTransactionsDialogFragment
        extends DialogFragment {
    public static final String ARG_TARGET_ACCOUNT = "UnresolvedTransactionsDialogFragment.TargetAccount";
    private final UnresolvedTransactionsDialogInterface mListener;
    private int mAccountId;
    private GroupAdapter<?> mAdapter;

    public UnresolvedTransactionsDialogFragment() {
        throw new IllegalArgumentException("This class must be provided with an UnresolvedTransactionsDialogInterface instance.");
    }

    public UnresolvedTransactionsDialogFragment(UnresolvedTransactionsDialogInterface _listener) {
        mListener = _listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //noinspection rawtypes
        mAdapter = new GroupAdapter();
        mAdapter.setOnItemClickListener((item, view) -> {
            if (view.getId() == R.id.transaction_card) {
                TransactionItem _item = (TransactionItem) item;
                MoneyTransaction holder = new MoneyTransaction();
                holder.setId(_item.getTransactionId());
                holder.setType(_item.getTransactionType());
                mListener.onUnresolvedTransactionClick(holder);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        Bundle args = requireArguments(); // ARG_TARGET_ACCOUNT is expected to always be provided
        mAccountId = args.getInt(ARG_TARGET_ACCOUNT);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.unresolved_transactions));
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogUnresolvedTransactionsBinding $ = DialogUnresolvedTransactionsBinding.inflate(inflater, container, false);
        RecyclerView recyclerView = $.unresolvedItemsList;
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        mListener.getUnresolvedTransactionsList(mAccountId).observe(getViewLifecycleOwner(), this::onTransactionsListChanged);
        return $.getRoot();
    }

    private void onTransactionsListChanged(List<TransactionDisplayData> transactions) {
        mAdapter.clear();
        if (!transactions.isEmpty()) {
            // Init data holders
            YearMonth sectionYearMonth = YearMonth.from(transactions.get(0).transaction.getDate());
            Section monthSection = new Section();
            final BigDecimal SECTION_TOTAL = BigDecimalUtil.ZERO;
            String monthSectionName = DateUtil.getYearMonthString(sectionYearMonth);

            for (TransactionDisplayData item : transactions) {
                // Retrieve current item day id
                YearMonth iterationMonth = YearMonth.from(item.transaction.getDate());

                // If current item's day isn't the same as last item, add section to adapter
                //  and reset data holders
                if (!sectionYearMonth.equals(iterationMonth)) {
                    monthSection.setHeader(new TransactionGroupHeader(monthSectionName, SECTION_TOTAL));
                    mAdapter.add(monthSection);

                    sectionYearMonth = iterationMonth;
                    monthSection = new Section();
                    monthSectionName = DateUtil.getYearMonthString(iterationMonth);
                }

                // Add the current item to the day's group
                monthSection.add(new TransactionItem(item, false));
            }
            // Insert last generated section (it never entered inner if statement)
            monthSection.setHeader(new TransactionGroupHeader(monthSectionName, SECTION_TOTAL));
            mAdapter.add(monthSection);
        } else {
            onDismiss(requireDialog());
        }
    }

    public interface UnresolvedTransactionsDialogInterface {
        void onUnresolvedTransactionClick(@NonNull MoneyTransaction transaction);
        LiveData<List<TransactionDisplayData>> getUnresolvedTransactionsList(int accountId);
    }
}
