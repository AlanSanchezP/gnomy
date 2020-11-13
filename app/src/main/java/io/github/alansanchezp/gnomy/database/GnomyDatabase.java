package io.github.alansanchezp.gnomy.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.EmptyResultSetException;
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
import io.reactivex.Single;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SupportFactory;
import net.sqlcipher.database.SQLiteDatabase;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

@Database(entities = {
    Category.class,
    Account.class,
    MoneyTransaction.class,
    MonthlyBalance.class,
    RecurrentTransaction.class,
    Transfer.class
}, version = 1, exportSchema = false)
// TODO: Implement schemaLocation when app (and therefore db model) is
//  (at least) beta-release ready
@TypeConverters({GnomyTypeConverters.class})
public abstract class GnomyDatabase extends RoomDatabase {
    private static GnomyDatabase INSTANCE;
    private static Class<?> MockDatabaseOperationsUtil = null;

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
        MockDatabaseOperationsUtil = getMockDatabaseOperationsUtil();

        if (MockDatabaseOperationsUtil == null) {
            Log.d("GnomyDatabase", "buildDatabaseInstance: Getting persistent database.");
            builder = Room.databaseBuilder(context,
                    GnomyDatabase.class,
                    "gnomy.db");
        } else {
            Log.d("GnomyDatabase", "buildDatabaseInstance: Getting test database.");
            builder = Room.inMemoryDatabaseBuilder(context,
                    GnomyDatabase.class)
                    .setTransactionExecutor(Executors.newSingleThreadExecutor())
                    .allowMainThreadQueries();
        }

        builder = builder
                .openHelperFactory(factory)
                // TODO: Create migrations
                // TODO: Remove once migrations are implemented
                .fallbackToDestructiveMigration()
                .addCallback(PREPOPULATE_CATEGORIES_CALLBACK);

        return builder.build();
    }

    private static final RoomDatabase.Callback PREPOPULATE_CATEGORIES_CALLBACK = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // TODO: Prepopulate categories
        }
    };

    public <T> Single<T> toSingleInTransaction(@NonNull Callable<T> callable) {
        return Single.create(emitter -> {
            try {
                T result = GnomyDatabase.super.runInTransaction(callable);
                if (result != null) emitter.onSuccess(result);
                else throw new EmptyResultSetException("Composite transaction returned null object.");
            } catch (EmptyResultSetException |
                    GnomyIllegalQueryException e) {
                emitter.onError(e);
            }
        });
    }

    private static Class<?> getMockDatabaseOperationsUtil() {
        try {
            // DO NOT IMPLEMENT THIS CLASS IN MAIN SOURCE SET
            // IT EXISTS ONLY FOR TESTING PURPOSES
            return Class.forName("io.github.alansanchezp.gnomy.database.MockDatabaseOperationsUtil");
        } catch (ClassNotFoundException e) {
            Log.d("GnomyDatabase", "getMockDatabaseOperationsUtil: ", e);
            return null;
        }
    }

    private Object getMockDAO(String getDAOMethodName) {
        if (MockDatabaseOperationsUtil == null) return null;
        try {
            return MockDatabaseOperationsUtil
                    .getMethod(getDAOMethodName).invoke(null);
        } catch (InvocationTargetException e) {
            Log.w("GnomyDatabase", "getMockDAO: ", e);
            return null;
        } catch (IllegalAccessException |
                SecurityException |
                IllegalArgumentException |
                NullPointerException |
                ExceptionInInitializerError |
                NoSuchMethodException e) {
            throw new RuntimeException("An error occurred trying to retrieve the specified DAO.", e);
        }
    }

    // WRAPPER DAO methods
    public AccountDAO accountDAO() {
        AccountDAO mockDAO = (AccountDAO) getMockDAO("getAccountDAO");
        if (mockDAO != null) return mockDAO;

        return _accountDAO();
    }

    public CategoryDAO categoryDAO() {
        return _categoryDAO();
    }

    protected abstract CategoryDAO _categoryDAO();

    protected abstract AccountDAO _accountDAO();

    protected abstract MoneyTransactionDAO _transactionDAO();

    protected abstract RecurrentTransactionDAO _recurrentTransactionDAO();

    protected abstract TransferDAO _transferDAO();

    public static void cleanUp(){
        INSTANCE = null;
    }
}
