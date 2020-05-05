package io.github.alansanchezp.gnomy.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import io.github.alansanchezp.gnomy.database.account.Account;
import io.github.alansanchezp.gnomy.database.account.AccountDAO;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.database.account.MonthlyBalanceDAO;
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.category.CategoryDAO;
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
    Category.class,
    Account.class,
    MoneyTransaction.class,
    MonthlyBalance.class,
    RecurrentTransaction.class,
    Transfer.class
}, version = 1)
@TypeConverters({GnomyTypeConverters.class})
public abstract class GnomyDatabase extends RoomDatabase {
    private static GnomyDatabase INSTANCE;

    public static GnomyDatabase getInstance(Context context, String userEnteredPassphrase) {
        if (INSTANCE == null) {
            synchronized (GnomyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabaseInstance(context, userEnteredPassphrase);
                }
            }
        }
        return INSTANCE;
    }

    private static GnomyDatabase buildDatabaseInstance(Context context, String userEnteredPassphrase) {
        byte[] passphrase = SQLiteDatabase.getBytes(userEnteredPassphrase.toCharArray());
        SupportFactory factory = new SupportFactory(passphrase);

        return Room.databaseBuilder(context,
                GnomyDatabase.class,
                "gnomy.db")
                .openHelperFactory(factory)
                // TODO: create migrations
                .fallbackToDestructiveMigration()
                // TODO: prepopulate categories
                // TODO: evaluate if balance adjustment should be done using triggers or as part of repository code
                .addCallback(triggersCallback)
                .build();
    }

    private static RoomDatabase.Callback triggersCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            db.execSQL(
                "CREATE TRIGGER on_account_created " +
                "AFTER INSERT ON accounts FOR EACH ROW " +
                "BEGIN " +
                    "INSERT INTO monthly_balances (account_id, balance_date) " +
                    "VALUES(NEW.account_id, strftime(\"%Y%m\", datetime(NEW.created_at/1000, 'unixepoch')));" +
                "END"
            );
        }
    };

    // DAO declarations
    public abstract CategoryDAO categoryDAO();

    public abstract AccountDAO accountDAO();

    public abstract MoneyTransactionDAO transactionDAO();

    public abstract MonthlyBalanceDAO monthlyBalanceDAO();

    public abstract RecurrentTransactionDAO recurrentTransactionDAO();

    public abstract TransferDAO transferDAO();

    public static void cleanUp(){
        INSTANCE = null;
    }
}
