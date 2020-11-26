package io.github.alansanchezp.gnomy.database.transaction;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteConstraintException;

import java.math.BigDecimal;
import java.time.YearMonth;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;
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

    // TODO: Currency conversion for calculatedValue
    public Single<Long> insert(MoneyTransaction transaction) {
        return db.toSingleInTransaction(() -> {
            transaction.setCalculatedValue(transaction.getOriginalValue());
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
                if (type == MoneyTransaction.INCOME ||
                        type == MoneyTransaction.TRANSFERENCE_INCOME) {
                    newIncomes = newIncomes.add(transaction.getCalculatedValue());
                } else {
                    newExpenses = newExpenses.add(transaction.getCalculatedValue());
                }
            } else {
                if (type == MoneyTransaction.INCOME ||
                        type == MoneyTransaction.TRANSFERENCE_INCOME) {
                    newProjectedIncomes = newProjectedIncomes.add(transaction.getCalculatedValue());
                } else {
                    newProjectedExpenses = newProjectedExpenses.add(transaction.getCalculatedValue());
                }
            }
            dao._adjustBalance(accountId, month, newIncomes, newExpenses, newProjectedIncomes, newProjectedExpenses);
            return dao._insert(transaction);
        });
    }

    private void insertOrIgnoreBalance(int accountId, YearMonth month) {
        MonthlyBalance monthlyBalance = new MonthlyBalance();
        monthlyBalance.setDate(month);
        monthlyBalance.setAccountId(accountId);
        try {
            dao._insertOrIgnoreBalance(monthlyBalance);
        } catch (SQLiteConstraintException ignored) {
            // Ignoring. For some reason, SQLcipher is not following the IGNORE
            // conflict strategy.
        }
    }
}
