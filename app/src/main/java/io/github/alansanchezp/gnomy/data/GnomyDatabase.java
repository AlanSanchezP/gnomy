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
import android.database.SQLException;
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
                        "(" +
                            "'Salary', " +
                            "'category_art_salary_1', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() + "), " +
                        "(" +
                            "'Sale', " +
                            "'category_art_sale_1', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Funding', " +
                            "'category_art_funding_1', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Government support', " +
                            "'category_art_govsupport_1', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Award', " +
                            "'category_art_award_1', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Gift', " +
                            "'category_art_gift_1', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Refund', " +
                            "'category_art_refund_1', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Others', " +
                            "'category_art_misc_1', "+
                            Category.INCOME_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Housing', " +
                            "'category_art_house_2', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Transportation', " +
                            "'category_art_transport_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Food', " +
                            "'category_art_food_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Utilities', " +
                            "'category_art_services_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Clothing', " +
                            "'category_art_clothes_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Healthcare', " +
                            "'category_art_health_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Gifts/Donations', " +
                            "'category_art_gift_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Household items', " +
                            "'category_art_household_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Recreation', " +
                            "'category_art_activities_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Travelling', " +
                            "'category_art_travels_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Gadgets', " +
                            "'category_art_tech_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Others', " +
                            "'category_art_misc_1', "+
                            Category.EXPENSE_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Loan', " +
                            "'category_art_getloan_1', "+
                            Category.SHARED_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Loan payment', " +
                            "'category_art_payloan_1', "+
                            Category.SHARED_CATEGORY + ", " +
                            "0, " +
                            ColorUtil.getRandomColor() +"), " +
                        "(" +
                            "'Others', " +
                            "'category_art_misc_1', "+
                            Category.SHARED_CATEGORY + ", " +
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
                    GnomyIllegalQueryException |
                    SQLException |
                    net.sqlcipher.database.SQLiteException e) {
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
