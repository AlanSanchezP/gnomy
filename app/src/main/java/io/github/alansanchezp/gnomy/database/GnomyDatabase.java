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
import io.github.alansanchezp.gnomy.database.category.Category;
import io.github.alansanchezp.gnomy.database.category.CategoryDAO;
import io.github.alansanchezp.gnomy.database.recurrentTransaction.RecurrentTransaction;
import io.github.alansanchezp.gnomy.database.recurrentTransaction.RecurrentTransactionDAO;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransaction;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionDAO;
import io.github.alansanchezp.gnomy.util.ColorUtil;
import io.reactivex.Single;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SupportFactory;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

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
            // DO NOT IMPLEMENT THIS CLASS IN MAIN SOURCE SET
            // IT EXISTS ONLY FOR TESTING PURPOSES
            Class.forName("io.github.alansanchezp.gnomy.database.MockRepositoryBuilder");
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
                    // TODO: Determine final categories
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
