package io.github.alansanchezp.gnomy.database.account;

import java.math.BigDecimal;
import java.time.YearMonth;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Ignore;
import io.github.alansanchezp.gnomy.util.DateUtil;

/**
 * Composite class meant to be used on graphical representations
 * of the given {@link Account} across its balance history,
 * specifically during a target month.
 *
 * This is just a helper class for Room DAO operations, avoid
 * instantiating it directly as it can potentially throw
 * RuntimeException or its subclasses.
 *
 * Any DAO query that returns objects of this class MUST guarantee
 * that account (and its attributes) and targetMonth are non-null.
 * All other attributes can be null depending on the context.
 *
 * Avoid accessing attributes (with the exception of targetMonth)
 * directly, as their management can get messy if not treated carefully.
 * Use public helper methods instead.
 */
public class AccountWithAccumulated {
    @Embedded
    public Account account;
    @ColumnInfo(name="target_month")
    public YearMonth targetMonth;

    @ColumnInfo(name="confirmed_before_month")
    protected BigDecimal confirmedBefore;
    @ColumnInfo(name="pending_incomes_before_month")
    protected BigDecimal pendingIncomesBefore;
    @ColumnInfo(name="pending_expenses_before_month")
    protected BigDecimal pendingExpensesBefore;

    @ColumnInfo(name="confirmed_incomes_at_month")
    protected BigDecimal confirmedIncomesAtMonth;
    @ColumnInfo(name="confirmed_expenses_at_month")
    protected BigDecimal confirmedExpensesAtMonth;
    @ColumnInfo(name="pending_incomes_at_month")
    protected BigDecimal pendingIncomesAtMonth;
    @ColumnInfo(name="pending_expenses_at_month")
    protected BigDecimal pendingExpensesAtMonth;

    /**
     * Calculates the total balance conformed by CONFIRMED transactions that
     * took place before and during the target month (including the
     * initial value of the account).
     *
     * @return  Sum of confirmed transactions at the target month.
     *          BigDecimal.ZERO for months previous to account's creation date
     */
    public BigDecimal getConfirmedAccumulatedBalanceAtMonth() {
        BigDecimal initialValue = BigDecimal.ZERO;
        if (!targetMonth.isBefore(YearMonth.from(account.getCreatedAt())))
            initialValue = account.getInitialValue();

        return initialValue
                .add(originalOrZero(confirmedBefore))
                .add(getConfirmedIncomesAtMonth())
                .subtract(getConfirmedExpensesAtMonth());
    }

    /**
     * Calculates the total balance that should be displayed on
     * screen for the target month. "End of month balance" means
     * 2 different things depending on the context. If the target
     * month is a past one, this method returns getConfirmedAccumulatedBalanceAtMonth().
     * Otherwise, it represents the PROJECTED state of the account
     * by the end of the target month (present or future).
     *
     * @return  Balance at the end of the target month
     */
    @Ignore
    public BigDecimal getBalanceAtEndOfMonth() {
        // FUTURE AND PRESENT MONTHS
        //  return sum of initial value + all confirmed + all pending
        //  (confirmedAtMonth SHOULD be 0 for future months)
        //  and ensuring that is programmer's responsibility
        if (!targetMonth.isBefore(DateUtil.now())) {
            return account.getInitialValue() //104
                    .add(originalOrZero(confirmedBefore))  // + 10
                    .add(originalOrZero(pendingIncomesBefore)) // +20
                    .subtract(originalOrZero(pendingExpensesBefore)) // - 0
                    .add(getConfirmedIncomesAtMonth()) // + 0
                    .subtract(getConfirmedExpensesAtMonth()) // - 0
                    .add(getPendingIncomesAtMonth()) // +0
                    .subtract(getPendingExpensesAtMonth()); // -10
        }
        // PAST MONTHS:
        return getConfirmedAccumulatedBalanceAtMonth();
    }

    /**
     * Returns the numerical money value of
     * {@link io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction} elements
     * that were left unconfirmed before the target month (and in the
     * target month itself, if it is a past one).
     *
     * Note that this method doesn't  perform an {@code incomes - expenses}
     * operation, as it's not meant to represent a balance,
     * but an amount of money. As such, it performs {@code incomes + expenses}
     * (money + money) as both types of transaction are identical when
     * it comes to their resolved/unresolved status.
     *
     * Future months always return null as it is impossible to tell
     * if the user will have resolved these transactions by that moment.
     *
     * Current month (today) returns the amount of money contained in
     * PAST unresolved transactions, as present ones are EXPECTED
     * to be handled by the end of the month.
     *
     * @return  Numerical amount of money that has been left unresolved.
     *          A warning to the user must be displayed only for
     *          non-zero values.
     */
    @Ignore
    public BigDecimal getUnresolvedTransactions() {
        // Future months
        if (targetMonth.isAfter(DateUtil.now())) return BigDecimal.ZERO;

        // Technically both of them are null or not null at the same time
        //  but who knows what can happen...
        //  pendingExpensesBefore and pendingIncomesBefore SHOULD
        //  be guaranteed to be non-null for all months but the one
        //  that corresponds to the account creation date, as there will
        //  always be a non-null carried value corresponding to the first
        //  auto-generated monthly balance
        if (pendingExpensesAtMonth == null && pendingIncomesAtMonth == null)
            return originalOrZero(pendingExpensesBefore)
                    .add(originalOrZero(pendingIncomesBefore));

        // Current month, ignore atMonth values and return only
        //  previous accumulated values
        if (targetMonth.equals(DateUtil.now()))
            return originalOrZero(pendingExpensesBefore)
                    .add(originalOrZero(pendingIncomesBefore));

        // This should only happen if target month equals account.createdAt
        //  as there are no monthly balances for such account before that
        if (confirmedBefore == null)
            return pendingExpensesAtMonth.add(getPendingIncomesAtMonth());

        // Any other month
        return pendingExpensesBefore
                .add(pendingIncomesBefore)
                .add(getPendingExpensesAtMonth())
                .add(getPendingIncomesAtMonth());
    }

    /**
     * Prevents NullPointerException on BigDecimal operations
     * by turning null objects into 0 numbers, while leaving
     * non-null objects intact.
     *
     * @param original  Original object that might be zero
     * @return          BigDecimal.ZERO for null objects
     *                  or @param original.
     */
    private BigDecimal originalOrZero(BigDecimal original) {
        if (original != null) return original;
        return BigDecimal.ZERO;
    }

    // Public getters for AccountBalanceHistoryActivity

    public BigDecimal getConfirmedIncomesAtMonth() {
        return originalOrZero(confirmedIncomesAtMonth);
    }

    public BigDecimal getConfirmedExpensesAtMonth() {
        return originalOrZero(confirmedExpensesAtMonth);
    }

    public BigDecimal getPendingIncomesAtMonth() {
        return originalOrZero(pendingIncomesAtMonth);
    }

    public BigDecimal getPendingExpensesAtMonth() {
        return originalOrZero(pendingExpensesAtMonth);
    }
}
