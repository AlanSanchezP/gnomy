package io.github.alansanchezp.gnomy.ui.transaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.Section;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import io.github.alansanchezp.gnomy.R;
import io.github.alansanchezp.gnomy.data.account.Account;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransactionFilters;
import io.github.alansanchezp.gnomy.data.transaction.TransactionDisplayData;
import io.github.alansanchezp.gnomy.databinding.FragmentTransactionsBinding;
import io.github.alansanchezp.gnomy.ui.ConfirmationDialogFragment;
import io.github.alansanchezp.gnomy.androidUtil.GnomyFragmentFactory;
import io.github.alansanchezp.gnomy.ui.MainNavigationFragment;
import io.github.alansanchezp.gnomy.ui.category.CategoriesActivity;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.github.alansanchezp.gnomy.util.DateUtil;
import io.github.alansanchezp.gnomy.androidUtil.SingleClickViewHolder;
import io.github.alansanchezp.gnomy.viewmodel.transaction.TransactionsListViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TransactionsFragment
        extends MainNavigationFragment<FragmentTransactionsBinding>
        implements  ConfirmationDialogFragment.OnConfirmationDialogListener,
                    TransactionFiltersDialogFragment.TransactionFiltersDialogInterface {

    public static final String ARG_DEFAULT_FILTER_ACCOUNT = "TransactionsFragment.DefaultFilterAccount";
    private static final String TAG_DELETE_TRANSACTION_DIALOG = "TransactionsFragment.DeleteTransactionDialog";
    private static final String TAG_FILTERS_DIALOG = "TransactionsFragment.FiltersDialog";
    private GroupAdapter<?> mAdapter;
    private TransactionsListViewModel mViewModel;
    private boolean mAllowClicks = true;
    private int mMainColor;

    public TransactionsFragment() {
        super(null,
                R.menu.transactions_fragment_toolbar,
                true,
                FragmentTransactionsBinding::inflate);
    }

    private GnomyFragmentFactory getFragmentFactory() {
        return new GnomyFragmentFactory()
                .addMapElement(ConfirmationDialogFragment.class, this)
                .addMapElement(TransactionFiltersDialogFragment.class, this);
    }

    /* ANDROID LIFECYCLE METHODS */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //noinspection rawtypes
        mAdapter = new GroupAdapter();
        mAdapter.setOnItemClickListener(this::onItemClickListener);
        // XXX: [#52] Is this the best way to present a delete option to the user?
        mAdapter.setOnItemLongClickListener(this::onItemLongClickListener);
        // XXX: [#52] Is alert icon gonna do any action? It doesn't seem like it is possible
        //  to set a listener to it, so that might be a problem
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getChildFragmentManager().setFragmentFactory(getFragmentFactory());
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this)
                .get(TransactionsListViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = $.itemsList;
        recyclerView.setAdapter(mAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        mViewModel.bindMonth(mSharedViewModel.activeMonth);

        // XXX: [#55] IS THERE A BETTER WAY TO HANDLE THE INITIAL ACCOUNT?
        // Handle "See transactions" from accounts fragment
        Bundle args = getArguments();
        if (args != null && args.size() != 0) {
            int initialAccount = args.getInt(ARG_DEFAULT_FILTER_ACCOUNT, 0);
            args.remove(ARG_DEFAULT_FILTER_ACCOUNT);
            if (initialAccount != 0)  mViewModel.initAccount(initialAccount);
        }

        mViewModel.getFilters().observe(getViewLifecycleOwner(), this::onFiltersChanged);
        mViewModel.getTransactionsList().observe(getViewLifecycleOwner(), this::onTransactionsListChanged);
        SingleClickViewHolder<Button> seeFiltersVH = new SingleClickViewHolder<>($.customFilterBannerEdit);
        seeFiltersVH.setOnClickListener(this::openFiltersDialog);
        SingleClickViewHolder<Button> clearFiltersVH = new SingleClickViewHolder<>($.customFilterBannerClear);
        clearFiltersVH.setOnClickListener(v -> mViewModel.clearFilters());
    }

    @Override
    public void onResume() {
        super.onResume();
        mAllowClicks = true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter_all:
                mViewModel.setTransactionsType(MoneyTransactionFilters.ALL_TRANSACTION_TYPES);
                break;
            case R.id.action_filter_incomes:
                mViewModel.setTransactionsType(MoneyTransaction.INCOME);
                break;
            case R.id.action_filter_expenses:
                mViewModel.setTransactionsType(MoneyTransaction.EXPENSE);
                break;
            case R.id.action_filter_transfers:
                mViewModel.setTransactionsType(MoneyTransaction.TRANSFER);
                break;
            case R.id.action_filter_more:
                openFiltersDialog(null);
                break;
            case R.id.action_categories:
                Intent categoriesIntent = new Intent(getActivity(), CategoriesActivity.class);
                requireActivity().startActivity(categoriesIntent);
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    /* CONCRETE METHODS INHERITED FROM ABSTRACT CLASS */

    @Override
    protected void tintMenuIcons() {
        if (mMenu == null) return;
        if (mMainColor == 0) mMainColor = getResources().getColor(R.color.colorPrimary);
        int textColor = ColorUtil.getTextColor(mMainColor);

        mMenu.findItem(R.id.action_search)
                .getIcon()
                .setTint(textColor);
        mMenu.findItem(R.id.action_filter)
                .getIcon()
                .setTint(textColor);
    }

    @Override
    public void onFABClick(View v) {
        int transactionType = mViewModel.getCurrentFilters().getTransactionType();
        // TODO: [#32] Replace PopupMenu with an expandable FAB
        if (transactionType == MoneyTransactionFilters.ALL_TRANSACTION_TYPES) {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.transactions_fab_menu);
            popup.setOnMenuItemClickListener(item -> {
                if (mAllowClicks) {
                    mAllowClicks = false;
                    Intent newTransactionIntent = new Intent(getActivity(), AddEditTransactionActivity.class);
                    if (item.getItemId() == R.id.action_new_empty_expense)
                        newTransactionIntent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, MoneyTransaction.EXPENSE);
                    else if (item.getItemId() == R.id.action_new_empty_income)
                        newTransactionIntent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, MoneyTransaction.INCOME);
                    else if (item.getItemId() == R.id.action_new_empty_transfer)
                        newTransactionIntent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, MoneyTransaction.TRANSFER);
                    requireActivity().startActivity(newTransactionIntent);
                    return true;
                }
                return false;
            });
            popup.show();
        } else {
            Intent newTransactionIntent = new Intent(getActivity(), AddEditTransactionActivity.class);
            newTransactionIntent.putExtra(AddEditTransactionActivity.EXTRA_TRANSACTION_TYPE, transactionType);
            requireActivity().startActivity(newTransactionIntent);
        }
    }

    @Override
    public void onMonthChanged(YearMonth month) {
    }

    /* CUSTOM METHODS */

    private void openFiltersDialog(View v) {
        TransactionFiltersDialogFragment d = new TransactionFiltersDialogFragment(this);
        d.show(getChildFragmentManager(), TAG_FILTERS_DIALOG);
    }

    private void onFiltersChanged(MoneyTransactionFilters filters) {
        String title;
        if (filters.getTransactionType() == MoneyTransactionFilters.ALL_TRANSACTION_TYPES) {
            mMainColor = getResources().getColor(R.color.colorPrimary);
            title = getResources().getString(R.string.title_transactions);
        } else if (filters.getTransactionType() == MoneyTransaction.INCOME) {
            mMainColor = getResources().getColor(R.color.colorIncomes);
            title = getResources().getString(R.string.action_filter_incomes);
        } else if (filters.getTransactionType() == MoneyTransaction.EXPENSE) {
            mMainColor = getResources().getColor(R.color.colorExpenses);
            title = getResources().getString(R.string.action_filter_expenses);
        } else {
            mMainColor = getResources().getColor(R.color.colorTransfers);
            title = getResources().getString(R.string.action_filter_transfers);
        }

        if (filters.isSimpleFilterWithMonth(mSharedViewModel.activeMonth.getValue())) {
            mSharedViewModel.toggleOptionalNavigationElements(true);
            $.customFilterBanner.setVisibility(View.GONE);
        } else {
            mSharedViewModel.toggleOptionalNavigationElements(false);
            $.customFilterBanner.setVisibility(View.VISIBLE);
            // VISIBILITY VISIBLE
        }

        mSharedViewModel.changeThemeColor(mMainColor);
        mSharedViewModel.changeTitle(title);

        if (mMenu == null) return;
        // For some reason, items are null if navigating through back button
        if (mMenu.findItem(R.id.action_filter) == null) return;
        tintMenuIcons();
    }

    private void onTransactionsListChanged(final List<TransactionDisplayData> transactions) {
        mAdapter.clear();
        // Both getCurrentFilters() and getActiveMonth() should never return null
        // as the arrival of a new set of items implies those two LiveData objects
        // already have some value on them.
        boolean isSimpleFilter = mViewModel.getCurrentFilters().isSimpleFilterWithMonth(
                mSharedViewModel.activeMonth.getValue());

        // XXX: [#52] Is it worth keeping Groupie for the few uses it has in the app?
        //  Maybe a simple local class to handle items and
        //  headers in a recyclerview would be better
        if (!transactions.isEmpty()) {
            // Init data holders
            int daySectionId = DateUtil.getDayId(transactions.get(0).transaction.getDate());
            Section daySection = new Section();
            BigDecimal dayTotal = BigDecimalUtil.ZERO;
            String daySectionName = getDayGroupName(transactions.get(0).transaction.getDate(), isSimpleFilter);

            for (TransactionDisplayData item : transactions) {
                // Retrieve current item day id
                int itemDayId = DateUtil.getDayId(item.transaction.getDate());

                // If current item's day isn't the same as last item, add section to adapter
                //  and reset data holders
                if (itemDayId != daySectionId) {
                    daySection.setHeader(new TransactionGroupHeader(daySectionName, dayTotal));
                    mAdapter.add(daySection);

                    daySectionId = itemDayId;
                    daySection = new Section();
                    dayTotal = BigDecimalUtil.ZERO;
                    daySectionName = getDayGroupName(item.transaction.getDate(), isSimpleFilter);
                }

                // Add the current item to the day's group
                daySection.add(new TransactionItem(item, true));
                if (item.transaction.getType() == MoneyTransaction.INCOME)
                    dayTotal = dayTotal.add(item.transaction.getCalculatedValue());
                else if (item.transaction.getType() == MoneyTransaction.EXPENSE)
                    dayTotal = dayTotal.subtract(item.transaction.getCalculatedValue());
            }
            // Insert last generated section (it never entered inner if statement)
            daySection.setHeader(new TransactionGroupHeader(daySectionName, dayTotal));
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

    /**
     * Generates the header title for a group of transactions.
     * Transactions are grouped by day.
     *
     * @param groupDate             OffsetDateTime representing the day to group.
     * @param isLimitedToOneMonth   If true, the generated String will use a friendly
     *                              format with the value of dayOfMonth from groupDate.
     *                              If false, will use a YY/MM/DD string.
     * @return                      Generated string.
     */
    private String getDayGroupName(OffsetDateTime groupDate, boolean isLimitedToOneMonth) {
        if (isLimitedToOneMonth)
            return DateUtil.getDayString(groupDate);
        else
            return DateUtil.getOffsetDateTimeString(groupDate, false);
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

    @Override
    public void applyFilters(@NonNull MoneyTransactionFilters filters) {
        mViewModel.applyFilters(filters);
    }

    @NonNull
    @Override
    public MoneyTransactionFilters getInitialFilters() {
        return mViewModel.getCurrentFilters();
    }

    @NonNull
    @Override
    public LiveData<List<Category>> getCategoriesLiveData(LiveData<Integer> transactionType) {
        return mViewModel.getCategories(transactionType);
    }

    @NonNull
    @Override
    public LiveData<List<Account>> getAccountsLiveData() {
        return mViewModel.getAccounts();
    }
}