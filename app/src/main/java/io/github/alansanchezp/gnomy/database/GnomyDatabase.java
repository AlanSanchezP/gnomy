package io.github.alansanchezp.gnomy.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountDAO;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalanceDAO;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.category.CategoryDAO;
import io.github.alansanchezp.gnomy.database.currency.Currency;
import io.github.alansanchezp.gnomy.database.currency.CurrencyDAO;
import io.github.alansanchezp.gnomy.database.recurrentTransaction.RecurrentTransaction;
import io.github.alansanchezp.gnomy.database.recurrentTransaction.RecurrentTransactionDAO;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionDAO;
import io.github.alansanchezp.gnomy.database.transfer.Transfer;
import io.github.alansanchezp.gnomy.database.transfer.TransferDAO;

import android.content.Context;

import net.sqlcipher.database.SupportFactory;
import net.sqlcipher.database.SQLiteDatabase;

@Database(entities = {
    Currency.class,
    Category.class,
    Account.class,
    MoneyTransaction.class,
    MonthlyBalance.class,
    RecurrentTransaction.class,
    Transfer.class
}, version = 1)
@TypeConverters({GnomyTypeConverters.class})
public abstract class GnomyDatabase extends RoomDatabase {
    private static GnomyDatabase gnomyDB;

    public static GnomyDatabase getInstance(Context context, String userEnteredPassphrase) {
        if (null == gnomyDB) {
            gnomyDB = buildDatabaseInstance(context, userEnteredPassphrase);
        }
        return gnomyDB;
    }

    private static GnomyDatabase buildDatabaseInstance(Context context, String userEnteredPassphrase) {
        byte[] passphrase = SQLiteDatabase.getBytes(userEnteredPassphrase.toCharArray());
        SupportFactory factory = new SupportFactory(passphrase);

        return Room.databaseBuilder(context,
                GnomyDatabase.class,
                "gnomy.db")
                .allowMainThreadQueries()
                .openHelperFactory(factory)
                // TODO generate complete set of currencies
                // consider limitations of free services for conversion rates
                .createFromAsset("gnomy.db")
                .build();
    }

    // DAO declarations
    public abstract CurrencyDAO currencyDAO();

    public abstract CategoryDAO categoryDAO();

    public abstract AccountDAO accountDAO();

    public abstract MoneyTransactionDAO transactionDAO();

    public abstract MonthlyBalanceDAO monthlyBalanceDAO();

    public abstract RecurrentTransactionDAO recurrentTransactionDAO();

    public abstract TransferDAO transferDAO();

    public static void cleanUp(){
        gnomyDB = null;
    }
}
