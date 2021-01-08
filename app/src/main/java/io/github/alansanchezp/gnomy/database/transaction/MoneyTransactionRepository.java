package io.github.alansanchezp.gnomy.database.transaction;

import android.content.Context;
import android.util.Log;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;
import io.github.alansanchezp.gnomy.database.GnomyIllegalQueryException;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.reactivex.Single;

import static io.github.alansanchezp.gnomy.database.GnomyTypeConverters.dateToTimestamp;
import static io.github.alansanchezp.gnomy.database.GnomyTypeConverters.decimalToLong;
import static io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionDAO.BASE_JOIN_FOR_QUERIES;

public class MoneyTransactionRepository {
    private final GnomyDatabase db;
    private final MoneyTransactionDAO dao;

    public MoneyTransactionRepository(Context context) {
        db = GnomyDatabase.getInstance(context, "");
        dao = db.transactionDAO();
    }

    /**
     * Returns an observable List of {@link TransactionDisplayData} objects,
     * filtered according to the contents of a {@link MoneyTransactionFilters} object.
     *
     * This method generates a dynamic query that is later passed to Room.
     *
     * @param filters    MoneyTransactionFilter instance to work with.
     * @return          Filtered set of transactions.
     */
    public LiveData<List<TransactionDisplayData>> getByFilters(MoneyTransactionFilters filters) {
        // Checking special cases first
        if (filters.getTransferDestinationAccountId() != 0 &&
                filters.getTransactionType() != MoneyTransaction.TRANSFER)
            throw new RuntimeException("Only transfers can be filtered by destination account.");
        if (filters.getAccountId() != 0 &&
                filters.getTransferDestinationAccountId() == filters.getAccountId())
            throw new RuntimeException("Origin and destination account cannot be the same.");
        if (filters.getTransactionStatus() == MoneyTransactionFilters.NO_STATUS) {
            Log.w("MoneyTransactionRepo", "getByFilters: Why is the UI allowing the user to filter using NO_STATUS ?");
            return new MutableLiveData<>(new ArrayList<>());
        }

        // Initialize SimpleSQLiteQuery arguments
        String queryString = BASE_JOIN_FOR_QUERIES + "AND ";
        List<Object> queryArgs = new ArrayList<>();

        // Scan filter settings
        if (filters.getTransactionType() == MoneyTransactionFilters.ALL_TRANSACTION_TYPES) {
            queryString += "transactions.transaction_type != 4 ";
        } else {
            queryString += "transactions.transaction_type = ? ";
            queryArgs.add(filters.getTransactionType());
        }
        if (filters.getTransactionStatus() != MoneyTransactionFilters.ANY_STATUS) {
            queryString += "AND transactions.is_confirmed = ? ";
            boolean status = (filters.getTransactionStatus() == MoneyTransactionFilters.CONFIRMED_STATUS);
            queryArgs.add(status);
        }
        // TODO: Support multiple accounts and categories when filtering
        if (filters.getAccountId() != 0) {
            queryString += "AND transactions.account_id = ? ";
            queryArgs.add(filters.getAccountId());
        }
        if (filters.getTransferDestinationAccountId() != 0) {
            queryString += "AND transactions.transfer_destination_account_id = ? ";
            queryArgs.add(filters.getTransferDestinationAccountId());
        }
        if (filters.getCategoryId() != 0) {
            queryString += "AND transactions.category_id = ? ";
            queryArgs.add(filters.getCategoryId());
        }
        if (filters.getStartDate() != null) {
            queryString += "AND transactions.transaction_date >= ? ";
            queryArgs.add(dateToTimestamp(filters.getStartDate()));
        }
        if (filters.getEndDate() != null) {
            queryString += "AND transactions.transaction_date <= ? ";
            queryArgs.add(dateToTimestamp(filters.getEndDate()));
        }
        if (filters.getMinAmount() != null) {
            queryString += "AND transactions.original_value >= ? ";
            queryArgs.add(decimalToLong(filters.getMinAmount()));
        }
        if (filters.getMaxAmount() != null) {
            queryString += "AND transactions.original_value <= ? ";
            queryArgs.add(decimalToLong(filters.getMaxAmount()));
        }
        if (filters.getSortingMethod() == MoneyTransactionFilters.MOST_RECENT) {
            queryString += "ORDER BY transactions.transaction_date DESC;";
        } else {
            queryString += "ORDER BY transactions.transaction_date ASC;";
        }
        SupportSQLiteQuery query = new SimpleSQLiteQuery(queryString, queryArgs.toArray());
        return dao.getWithRawQuery(query);
    }

    public LiveData<MoneyTransaction> find(int id) {
        return dao.find(id);
    }

    /**
     * Inserts a given  {@link MoneyTransaction} row from some input.
     *
     * Currency calculations are applied when needed, and mirrored
     * transfers are automatically generated.
     *
     * @param transaction   Transaction to be inserted
     * @return              Single object that can be observed on main thread, wrapping
     *                      the inserted transaction's generated id.
     */
    public Single<Long> insert(MoneyTransaction transaction) {
        return db.toSingleInTransaction(() -> {
            GnomyIllegalQueryException validationError = transaction.getIsolatedValidationError();
            if (validationError != null)
                throw validationError;
            // TODO: Currency conversion (this will prevent use of deprecated method)
            //noinspection deprecation
            transaction.setCalculatedValue(transaction.getOriginalValue());
            addTransactionAmountToBalance(transaction);
            if (transaction.getType() == MoneyTransaction.TRANSFER) {
                MoneyTransaction mirror;
                mirror = transaction.getMirrorTransfer();
                //noinspection deprecation
                mirror.setCalculatedValue(mirror.getOriginalValue());
                addTransactionAmountToBalance(mirror);
                dao._insert(mirror);
            } else if (transaction.getTransferDestinationAccount() != null) {
                throw new GnomyIllegalQueryException("Non-transfers cannot have two linked accounts.");
            }
            return dao._insert(transaction);
        });
    }

    /**
     * Updates a given {@link MoneyTransaction} row from some input.
     *
     * It contemplates the next scenarios:
     *  1) The only field that changes is transaction's amount.
     *  2) Transaction's status changes (from confirmed to unconfirmed or vice-versa)
     *  3) Transaction's date changes (doesn't include currency exchange rates)
     *  4) Transaction's account changes
     *  5) Transaction's currency changes
     *  6) Any combination of the above
     *
     * @param transaction                   New transaction value to be used.
     * @return                              Single object that can be observed on main thread
     * @throws GnomyIllegalQueryException   If the previously-stored type doesn't match the new one
     *                                      or if there is no existing row at all.
     *                                      In the case of transfers, method also throws if a direct
     *                                      update to mirror transfers is attempted or if, for some
     *                                      reason, the transfer doesn't have a mirrored version.
     */
    public Single<Integer> update(MoneyTransaction transaction) {
        return db.toSingleInTransaction(() -> {
            GnomyIllegalQueryException validationError = transaction.getIsolatedValidationError();
            if (validationError != null)
                throw validationError;
            MoneyTransaction original = dao._find(transaction.getId());
            try {
                if (original.equals(transaction)) return 0;
                if (original.getType() != transaction.getType())
                    throw new GnomyIllegalQueryException("It is not allowed to change a transaction's type.");
                MoneyTransaction originalMirror = null;
                MoneyTransaction newMirror = null;
                if (transaction.getType() == MoneyTransaction.TRANSFER) {
                    originalMirror = dao._findMirrorTransfer(original.getDate(),
                            original.getAccount(),
                            original.getTransferDestinationAccount());
                    if (originalMirror == null)
                        throw new GnomyIllegalQueryException("!!! Transfer doesn't seem to have a mirror !!!");
                    newMirror = transaction.getMirrorTransfer();
                    newMirror.setId(originalMirror.getId());
                }

                //noinspection IfStatementWithIdenticalBranches
                if (!original.getOriginalValue().equals(
                        transaction.getOriginalValue())) {
                    // TODO: Currency conversion (this will prevent use of deprecated method)
                    //  add if() clause in case currency OR date change too
                    //noinspection deprecation
                    transaction.setCalculatedValue(transaction.getOriginalValue());
                    if (newMirror != null)
                        // Right now it looks like duplicated code, but when currency support
                        //  is implemented, it will no longer be the same
                        //noinspection deprecation
                        newMirror.setCalculatedValue(newMirror.getOriginalValue());
                } else if (newMirror != null) {
                    //noinspection deprecation
                    newMirror.setCalculatedValue(newMirror.getOriginalValue());
                }

                // Subtract previous transaction amount from original MonthlyBalance
                addTransactionAmountToBalance(original.getInverse());
                // Add new transaction amount to corresponding MonthlyBalance
                addTransactionAmountToBalance(transaction);

                if (newMirror != null) {
                    // Subtract previous transaction amount from original MonthlyBalance
                    addTransactionAmountToBalance(originalMirror.getInverse());
                    // Add new transaction amount to corresponding MonthlyBalance
                    addTransactionAmountToBalance(newMirror);
                    dao._update(newMirror);
                }
                return dao._update(transaction);
            } catch (NullPointerException e) {
                throw new GnomyIllegalQueryException("Trying to update non-existent transaction.", e);
            }
        });
    }

    /**
     * Deletes a {@link MoneyTransaction} and updates affected MonthlyBalance (s).
     * In the case of transfers, it also deletes mirrored transfer in
     * the recipient account.
     *
     * @param transactionId Id of the Transaction row to be deleted
     * @return              Single object that can be observed in main thread
     */
    public Single<Integer> delete(int transactionId) {
        return db.toSingleInTransaction(() -> {
            // Have to retrieve the original transaction to prevent
            //  faulty balance alterations. Additionally it prevents
            //  access to mirrored transfer objects.
            MoneyTransaction original = dao._find(transactionId);
            if (original == null)
                throw new GnomyIllegalQueryException("Trying to delete non-existent transaction.");
            if (original.getType() == MoneyTransaction.TRANSFER_MIRROR)
                throw new GnomyIllegalQueryException("Direct manipulation of mirror transfers is not allowed.");

            if (original.getType() == MoneyTransaction.TRANSFER) {
                MoneyTransaction mirrorTransfer = dao._findMirrorTransfer(original.getDate(),
                        original.getAccount(),
                        original.getTransferDestinationAccount());
                if (mirrorTransfer == null)
                    throw new GnomyIllegalQueryException("!!! Transfer doesn't seem to have a mirror !!!");

                addTransactionAmountToBalance(mirrorTransfer.getInverse());
                dao._delete(mirrorTransfer);
            }
            // Not checking if MonthlyBalance exists because it MUST exist
            addTransactionAmountToBalance(original.getInverse());
            return dao._delete(original);
        });
    }

    /**
     * Inserts a new {@link MonthlyBalance} row if necessary,
     * does nothing if Primary Key already exists.
     * This is needed for automatic creation of balances
     * on previously-empty months.
     *
     * Has to be wrapped inside an async operation,
     * otherwise it will throw an Exception (unless main thread
     * operations are enabled, which shouldn't happen)
     *
     * @param accountId Associated account id
     * @param month     Associated month
     */
    private void insertOrIgnoreBalance(int accountId, YearMonth month) {
        MonthlyBalance monthlyBalance = new MonthlyBalance();
        monthlyBalance.setDate(month);
        monthlyBalance.setAccountId(accountId);
        dao._insertOrIgnoreBalance(monthlyBalance);
    }

    /**
     * Updates a {@link MonthlyBalance} row using the given
     * {@link MoneyTransaction} object's calculatedValue, so that
     * it matches the balance's currency. It uses transaction's type
     * to determine if the value should be added or subtracted from
     * existing balance sum, and confirmation status to determine
     * if projected or total fields should be updated.
     *
     * Has to be wrapped inside an async operation,
     * otherwise it will throw an Exception (unless main thread
     * operations are enabled, which shouldn't happen)
     *
     * @param transaction   Transaction which amount value will be used
     *                      for calculations.
     */
    private void addTransactionAmountToBalance(MoneyTransaction transaction) {
        int accountId = transaction.getAccount();
        YearMonth month = YearMonth.from(transaction.getDate());
        int type = transaction.getType();
        boolean isConfirmed = transaction.isConfirmed();
        insertOrIgnoreBalance(accountId, month);

        BigDecimal newIncomes = BigDecimalUtil.ZERO;
        BigDecimal newExpenses = BigDecimalUtil.ZERO;
        BigDecimal newProjectedIncomes = BigDecimalUtil.ZERO;
        BigDecimal newProjectedExpenses = BigDecimalUtil.ZERO;

        if (isConfirmed) {
            if (type == MoneyTransaction.INCOME
                    || type == MoneyTransaction.TRANSFER_MIRROR) {
                newIncomes = newIncomes.add(transaction.getCalculatedValue());
            } else {
                newExpenses = newExpenses.add(transaction.getCalculatedValue());
            }
        } else {
            if (type == MoneyTransaction.TRANSFER || type == MoneyTransaction.TRANSFER_MIRROR)
                throw new GnomyIllegalQueryException("Transfers must have a confirmed status.");
            if (type == MoneyTransaction.INCOME) {
                newProjectedIncomes = newProjectedIncomes.add(transaction.getCalculatedValue());
            } else {
                newProjectedExpenses = newProjectedExpenses.add(transaction.getCalculatedValue());
            }
        }

        dao._adjustBalance(accountId, month, newIncomes, newExpenses, newProjectedIncomes, newProjectedExpenses);
    }

    // TODO: This method is used only in tests so far, evaluate deleting it later
    public LiveData<MonthlyBalance> getBalanceFromMonth(int accountId, YearMonth month) {
        return dao.findBalance(accountId, month);
    }
}
