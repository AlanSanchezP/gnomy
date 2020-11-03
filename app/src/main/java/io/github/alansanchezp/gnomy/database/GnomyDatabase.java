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
import android.util.Log;

import net.sqlcipher.database.SupportFactory;
import net.sqlcipher.database.SQLiteDatabase;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;

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
    private static Class<?> MockRepositoryUtility = null;

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
        Builder<GnomyDatabase> builder;

        try {
            // Database for testing purposes
            // DO NOT IMPLEMENT THIS CLASS IN MAIN SOURCE SET
            // IT EXISTS ONLY FOR TESTING PURPOSES
            MockRepositoryUtility = Class.forName("io.github.alansanchezp.gnomy.database.MockRepositoryUtility");
            builder = Room.inMemoryDatabaseBuilder(context,
                    GnomyDatabase.class)
                    .setTransactionExecutor(Executors.newSingleThreadExecutor())
                    .allowMainThreadQueries();
        } catch (ClassNotFoundException cnfe) {
            builder = Room.databaseBuilder(context,
                    GnomyDatabase.class,
                    "gnomy.db");
        }

        builder = builder
                .openHelperFactory(factory)
                // TODO: Create migrations
                // TODO: Remove once migrations are implemented
                .fallbackToDestructiveMigration()
                // TODO: Prepopulate categories
                // TODO: Evaluate if balance adjustment should be done using triggers or as part of repository code
                .addCallback(TRIGGERS_CALLBACK);

        return builder.build();
    }

    private static RoomDatabase.Callback triggersCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // TODO: Maybe rename trigger to 'create_first_monthly_balance'?
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

    private Object getMockDAO(String getDAO_methodName, Class<?> DAO_class) {
        try {
            return DAO_class.cast( MockRepositoryUtility
                    .getMethod(getDAO_methodName).invoke(null));
        } catch (IllegalAccessException |
                InvocationTargetException |
                NoSuchMethodException |
                NullPointerException |
                ClassCastException |
                IllegalStateException e) {
            if (e instanceof IllegalStateException) {
                Log.w("GnomyDatabase", "accountDAO: ", e);
            } else if (e instanceof ClassCastException) {
                Log.e("GnomyDatabase", "accountDAO: ", e);
            }
            return null;
        }
    }

    // WRAPPER DAO methods
    public AccountDAO accountDAO() {
        AccountDAO mockDAO = (AccountDAO) getMockDAO(
                "getAccountDAO", AccountDAO.class);
        if (mockDAO != null) return mockDAO;

        return _accountDAO();
    }

    public MonthlyBalanceDAO monthlyBalanceDAO() {
        MonthlyBalanceDAO mockDAO = (MonthlyBalanceDAO) getMockDAO(
                "getBalanceDAO", MonthlyBalanceDAO.class);
        if (mockDAO != null) return mockDAO;

        return _monthlyBalanceDAO();
    }

    public CategoryDAO categoryDAO() {
        return _categoryDAO();
    }

    protected abstract CategoryDAO _categoryDAO();

    protected abstract AccountDAO _accountDAO();

    protected abstract MoneyTransactionDAO _transactionDAO();

    protected abstract MonthlyBalanceDAO _monthlyBalanceDAO();

    protected abstract RecurrentTransactionDAO _recurrentTransactionDAO();

    protected abstract TransferDAO _transferDAO();

    // TODO: Research why did we need this? Where should we be using it?
    public static void cleanUp(){
        INSTANCE = null;
    }
}
