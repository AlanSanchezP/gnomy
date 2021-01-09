package io.github.alansanchezp.gnomy.data;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.EmptyResultSetException;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import io.github.alansanchezp.gnomy.data.account.Account;
import io.github.alansanchezp.gnomy.data.account.AccountDAO;
import io.github.alansanchezp.gnomy.data.account.MonthlyBalance;
import io.github.alansanchezp.gnomy.data.category.Category;
import io.github.alansanchezp.gnomy.data.category.CategoryDAO;
import io.github.alansanchezp.gnomy.data.recurrentTransaction.RecurrentTransaction;
import io.github.alansanchezp.gnomy.data.recurrentTransaction.RecurrentTransactionDAO;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransactionDAO;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.reactivex.Single;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SupportFactory;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * Application's database class.
 */
@Database(entities = {
    Category.class,
    Account.class,
    MoneyTransaction.class,
    MonthlyBalance.class,
    RecurrentTransaction.class
}, version = 1, exportSchema = false)
// TODO: Implement schemaLocation when app (and therefore db model) is
//  (at least) beta-release ready
@TypeConverters({GnomyTypeConverters.class})
public abstract class GnomyDatabase extends RoomDatabase {
    private static GnomyDatabase INSTANCE;

    /**
     * Retrieves a singleton database instance.
     *
     * @param context   Context of the application.
     * @param userEnteredPassphrase Passphrase to decrypt the database.
     * @return      Database singleton.
     */
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

    /**
     * Builds a database instance.
     *
     * @param context   Context of the application.
     * @param userEnteredPassphrase     Passphrase to encrypt the database. If null or empty,
     *                                  the database will not be encrypted.
     * @return          Database instance.
     */
    private static GnomyDatabase buildDatabaseInstance(Context context, String userEnteredPassphrase) {
        byte[] passphrase = SQLiteDatabase.getBytes(userEnteredPassphrase.toCharArray());
        SupportFactory factory = new SupportFactory(passphrase);
        Builder<GnomyDatabase> builder;

        try {
            // DO NOT IMPLEMENT THIS CLASS IN MAIN SOURCE SET
            // IT EXISTS ONLY FOR TESTING PURPOSES
            Class.forName("io.github.alansanchezp.gnomy.data.MockRepositoryBuilder");
            Log.d("GnomyDatabase", "buildDatabaseInstance: Getting test database.");
            builder = Room.inMemoryDatabaseBuilder(context,
                    GnomyDatabase.class)
                    .setTransactionExecutor(Executors.newSingleThreadExecutor())
                    .allowMainThreadQueries();
        } catch (ClassNotFoundException e) {
            Log.d("GnomyDatabase", "buildDatabaseInstance: Getting persistent database.");
            builder = Room.databaseBuilder(context,
                    GnomyDatabase.class,
                    "gnomy.db");
        }

        builder = builder
                .openHelperFactory(factory)
                // TODO: Create migrations and then remove this method call
                .fallbackToDestructiveMigration()
                .addCallback(PREPOPULATE_CATEGORIES_CALLBACK);

        return builder.build();
    }

    /**
     * Pre-populates app-defined categories.
     */
    private static final RoomDatabase.Callback PREPOPULATE_CATEGORIES_CALLBACK = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            db.execSQL("INSERT INTO categories (category_name, category_icon, category_type, can_delete, bg_color) " +
                    "VALUES " +
                        "(" +
                            "'Transfer', " +
                            "'ic_transfer_arrows_black_24', "+
                            "4, " + // Cannot access HIDDEN_CATEGORY value here
                            "0, " +
                            "-4278890), " + // Hardcoding transfers color
                        "(" +
                            "'Orphan outgoing transfer', " +
                            "'ic_orphan_outgoing_transfer_arrow_black_24', "+
                            "4, " +
                            "0, " +
                            "-1293008), " + // Hardcoding error color
                        "(" +
                            "'Orphan incoming transfer', " +
                            "'ic_orphan_incoming_transfer_arrow_black_24', "+
                            "4, " +
                            "0, " +
                            "-1293008), " + // Hardcoding error color
                    // TODO: Determine final pre-populated categories
                        "(" +
                            "'Dummy incomes category', " +
                            "'ic_color_lens_black_24dp', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() + "), " +
                        "(" +
                            "'Dummy expenses category', " +
                            "'ic_calculate_24', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Dummy both category', " +
                            "'ic_fitness_center_black_24dp', "+
                            Category.BOTH_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +")");
        }
    };

    /**
     * Helper method to wrap synchronous queries into {@link Single} observable
     * objects without losing transaction safety.
     *
     * @param callable  Series of operations to wrap.
     * @param <T>       Type of data that the Single object will emit.
     * @return          Single object.
     */
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

    public abstract CategoryDAO categoryDAO();

    public abstract AccountDAO accountDAO();

    public abstract MoneyTransactionDAO transactionDAO();

    public abstract RecurrentTransactionDAO _recurrentTransactionDAO();

    public static void cleanUp(){
        INSTANCE = null;
    }
}
