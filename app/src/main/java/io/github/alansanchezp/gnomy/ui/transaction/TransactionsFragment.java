package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Section;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.viewmodel.transaction.TransactionsListViewModel;

public class TransactionsFragment extends MainNavigationFragment {
    private GroupAdapter mAdapter;
    private TransactionsListViewModel mViewModel;

    public TransactionsFragment(MainNavigationInteractionInterface _interface) {
        super(_interface);
        // Required empty public constructor
    }

    /* ANDROID LIFECYCLE METHODS */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mAdapter = new GroupAdapter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this)
                .get(TransactionsListViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.items_list);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        mViewModel.bindMonth(mNavigationInterface.getActiveMonth());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getGroupsByDay().observe(getViewLifecycleOwner(), this::onTransactionsMapChanged);
    }

    /* CONCRETE METHODS INHERITED FROM ABSTRACT CLASS */

    @Override
    protected boolean hasAppbarActions() {
        return true;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.transactions_fragment_toolbar;
    }

    @Override
    protected boolean withOptionalNavigationElements() {
        return true;
    }

    @Override
    protected int getThemeColor() {
        return getResources().getColor(R.color.colorPrimary);
    }

    @Override
    protected String getTitle() {
        return  getResources().getString(R.string.title_transactions);
    }

    @Override
    protected void tintMenuIcons() {
        mMenu.findItem(R.id.action_search)
                .getIcon()
                .setTint(getResources().getColor(R.color.colorTextInverse));
        mMenu.findItem(R.id.action_filter)
                .getIcon()
                .setTint(getResources().getColor(R.color.colorTextInverse));
    }

    @Override
    public void onFABClick(View v) {
        Intent newTransactionIntent = new Intent(getActivity(), AddEditTransactionActivity.class);
        requireActivity().startActivity(newTransactionIntent);
    }

    @Override
    public void onMonthChanged(YearMonth month) {
    }

    private void onTransactionsMapChanged(TreeMap<Integer, List<MoneyTransaction>> map) {
        mAdapter.clear();
        for (List<MoneyTransaction> list : map.values()) {
            Section daySection = new Section();
            BigDecimal dayTotal = BigDecimalUtil.ZERO;
            String dayName = list.get(0).getDate().format(DateTimeFormatter.ofPattern("EEEE d"));
            for (MoneyTransaction item : list) {
                daySection.add(new TransactionItem(item));
                if (item.getType() == MoneyTransaction.INCOME)
                    dayTotal = dayTotal.add(item.getCalculatedValue());
                else if (item.getType() == MoneyTransaction.EXPENSE)
                    dayTotal = dayTotal.subtract(item.getCalculatedValue());
            }
            daySection.setHeader(new TransactionGroupHeader(dayName, dayTotal));
            mAdapter.add(daySection);
        }
    }
}