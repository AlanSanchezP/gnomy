package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.Section;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.ui.ConfirmationDialogFragment;
import io.github.alansanchezp.gnomy.ui.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.viewmodel.transaction.TransactionsListViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TransactionsFragment extends MainNavigationFragment
    implements ConfirmationDialogFragment.OnConfirmationDialogListener {

    private static final String TAG_DELETE_TRANSACTION_DIALOG = "TransactionsFragment.DeleteTransactionDialog";
    // Not sure as to what else to do to avoid this warning
    @SuppressWarnings("rawtypes")
    private GroupAdapter mAdapter;
    private TransactionsListViewModel mViewModel;
    private boolean mAllowClicks = true;

    public TransactionsFragment(MainNavigationInteractionInterface _interface) {
        super(_interface);
        // Required empty public constructor
    }

    private GnomyFragmentFactory getFragmentFactory() {
        return new GnomyFragmentFactory()
                .addMapElement(ConfirmationDialogFragment.class, this);
    }

    /* ANDROID LIFECYCLE METHODS */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //noinspection rawtypes
        mAdapter = new GroupAdapter();
        mAdapter.setOnItemClickListener(this::onItemClickListener);
        // TODO: Is this the best way to present a delete option to the user?
        mAdapter.setOnItemLongClickListener(this::onItemLongClickListener);
        // TODO: Is alert icon gonna do any action? It doesn't seem like we can
        //  set a listener to it, so that might be a problem
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getChildFragmentManager().setFragmentFactory(getFragmentFactory());
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

    @Override
    public void onResume() {
        super.onResume();
        mAllowClicks = true;
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

    private void onTransactionsMapChanged(TreeMap<Integer, List<TransactionDisplayData>> map) {
        mAdapter.clear();
        for (List<TransactionDisplayData> list : map.values()) {
            Section daySection = new Section();
            BigDecimal dayTotal = BigDecimalUtil.ZERO;
            String dayName = DateUtil.getDayString(list.get(0).transaction.getDate());
            for (TransactionDisplayData item : list) {
                // TODO: Use global currency during total calculation as default
                daySection.add(new TransactionItem(item));
                if (item.transaction.getType() == MoneyTransaction.INCOME)
                    dayTotal = dayTotal.add(item.transaction.getCalculatedValue());
                else if (item.transaction.getType() == MoneyTransaction.EXPENSE)
                    dayTotal = dayTotal.subtract(item.transaction.getCalculatedValue());
            }
            daySection.setHeader(new TransactionGroupHeader(dayName, dayTotal));
            mAdapter.add(daySection);
        }
    }

    private void effectiveDeleteTransaction(int transactionId) {
        mCompositeDisposable.add(
                mViewModel.delete(transactionId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                integer ->
                                        mViewModel.setTargetIdToDelete(0),
                                throwable ->
                                        Toast.makeText(getContext(), R.string.generic_data_error, Toast.LENGTH_LONG).show()));
    }

    /* INTERFACE METHODS */

    private void onItemClickListener(@SuppressWarnings("rawtypes") Item item, View view) {
        if (!mAllowClicks) return;
        // Headers trigger this too, so better to be safe
        if (view.getId() == R.id.transaction_card) {
            mAllowClicks = false;
            TransactionItem _item = (TransactionItem) item;
            int transactionId = _item.getTransactionId();
            int transactionType = _item.getTransactionType();
            Intent updateTransactionIntent = new Intent(getActivity(), AddEditTransactionActivity.class);
            updateTransactionIntent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_ID, transactionId);
            updateTransactionIntent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, transactionType);
            requireActivity().startActivity(updateTransactionIntent);
        }
    }

    private boolean onItemLongClickListener(@SuppressWarnings("rawtypes") Item item, View view) {
        if (!mAllowClicks) return false;
        // Headers trigger this too, so better to be safe
        if (view.getId() == R.id.transaction_card) {
            FragmentManager fm = getChildFragmentManager();
            if (fm.findFragmentByTag(TAG_DELETE_TRANSACTION_DIALOG) != null) return false;

            TransactionItem _item = (TransactionItem) item;
            mAllowClicks = false;
            mViewModel.setTargetIdToDelete(_item.getTransactionId());
            ConfirmationDialogFragment dialog = new ConfirmationDialogFragment(this);
            Bundle args = new Bundle();
            args.putString(ConfirmationDialogFragment.ARG_TITLE, getString(R.string.transaction_card_delete));
            args.putString(ConfirmationDialogFragment.ARG_MESSAGE, getString(R.string.account_card_delete_warning));
            dialog.setArguments(args);
            dialog.show(fm, TAG_DELETE_TRANSACTION_DIALOG);
            return true;
        }
        return false;
    }

    @Override
    public void onConfirmationDialogYes(DialogInterface dialog, String dialogTag, int which) {
        if (dialogTag.equals(TAG_DELETE_TRANSACTION_DIALOG)) {
            int idToDelete = mViewModel.getTargetIdToDelete();
            if (idToDelete == 0)  {
                Log.wtf("TransactionsFragment", "onConfirmationDialogYes: Trying to delete null object.");
                return;
            }
            effectiveDeleteTransaction(idToDelete);
        }
    }

    @Override
    public void onConfirmationDialogNo(DialogInterface dialog, String dialogTag, int which) {
    }

    @Override
    public void onConfirmationDialogCancel(DialogInterface dialog, String dialogTag) {
    }

    @Override
    public void onConfirmationDialogDismiss(DialogInterface dialog, String dialogTag) {
        mAllowClicks = true;
    }
}