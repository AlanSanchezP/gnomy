package io.github.alansanchezp.gnomy.database.transaction;

import android.content.Context;

import java.math.BigDecimal;
import java.time.YearMonth;

import androidx.lifecycle.LiveData;
import io.github.alansanchezp.gnomy.database.GnomyDatabase;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
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

            BigDecimal newIncomes = BigDecimal.ZERO;
            BigDecimal newExpenses = BigDecimal.ZERO;
            BigDecimal newProjectedIncomes = BigDecimal.ZERO;
            BigDecimal newProjectedExpenses = BigDecimal.ZERO;

            if (isConfirmed) {
                if (type == MoneyTransaction.INCOME ||
                        type == MoneyTransaction.TRANSFERENCE_INCOME) {
                    newIncomes.add(transaction.getCalculatedValue());
                } else {
                    newExpenses.add(transaction.getCalculatedValue());
                }
            } else {
                if (type == MoneyTransaction.INCOME ||
                        type == MoneyTransaction.TRANSFERENCE_INCOME) {
                    newProjectedIncomes.add(transaction.getCalculatedValue());
                } else {
                    newProjectedExpenses.add(transaction.getCalculatedValue());
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
        dao._insertOrIgnoreBalance(monthlyBalance);
    }
}
