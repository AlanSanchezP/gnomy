package io.github.alansanchezp.gnomy.database.transaction;

import android.content.Context;

import java.math.BigDecimal;
import java.time.YearMonth;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;
import io.github.alansanchezp.gnomy.database.GnomyIllegalQueryException;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.util.BigDecimalUtil;
import io.reactivex.Single;

public class MoneyTransactionRepository {
    private final GnomyDatabase db;
    private final MoneyTransactionDAO dao;

    public MoneyTransactionRepository(Context context) {
        db = GnomyDatabase.getInstance(context, "");
        dao = db.transactionDAO();
    }

    public LiveData<MoneyTransaction> find(int id) {
        return dao.find(id);
    }

    // TODO: Update AccountRepositoryTest with actual transactions
    // TODO: Validate both insert and update methods as an additional integrity check
    /**
     * Inserts a given  {@link MoneyTransaction} row from some input.
     *
     * Currency calculations are applied when needed, and mirrored
     * transfers are automatically generated.
     *
     * @param transaction   Transaction to be inserted
     * @return              Single object that can be observed on main thread, wrapping
     *                      the inserted account's generated id.
     */
    public Single<Long> insert(MoneyTransaction transaction) {
        return db.toSingleInTransaction(() -> {
            // TODO: Currency conversion (this will prevent use of deprecated method)
            //noinspection deprecation
            transaction.setCalculatedValue(transaction.getOriginalValue());
            addTransactionAmountToBalance(transaction);
            if (transaction.getType() == MoneyTransaction.TRANSFER) {
                MoneyTransaction mirror = transaction.getMirrorTransfer();
                //noinspection deprecation
                mirror.setCalculatedValue(mirror.getOriginalValue());
                addTransactionAmountToBalance(mirror);
                dao._insert(mirror);
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
            MoneyTransaction original = dao._find(transaction.getId());
            try {
                if (original.equals(transaction)) return 0;
                if (original.getType() != transaction.getType())
                    throw new GnomyIllegalQueryException("It is not allowed to change a transaction's type.");
                if (transaction.getType() == MoneyTransaction.TRANSFER_MIRROR)
                    throw new GnomyIllegalQueryException("Cannot update mirrored transactions directly.");
                MoneyTransaction originalMirror = null;
                MoneyTransaction newMirror = null;
                if (transaction.getType() == MoneyTransaction.TRANSFER) {
                    originalMirror = dao._findMirrorTransfer(transaction.getDate(),
                            transaction.getAccount(),
                            transaction.getTransferDestinationAccount());
                    if (originalMirror == null)
                        throw new GnomyIllegalQueryException("Transaction doesn't seem to have a mirror!!!");
                    newMirror = transaction.getMirrorTransfer();
                    newMirror.setId(originalMirror.getId());
                }

                if (!original.getOriginalValue().equals(
                        transaction.getOriginalValue())) {
                    // TODO: Currency conversion (this will prevent use of deprecated method)
                    //  add if() clause in case currency OR date change too
                    //noinspection deprecation
                    transaction.setCalculatedValue(transaction.getOriginalValue());
                    if (newMirror != null)
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
