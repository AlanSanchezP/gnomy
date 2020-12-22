package io.github.alansanchezp.gnomy.database;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;

import io.github.alansanchezp.gnomy.database.account.AccountRepository;
import io.github.alansanchezp.gnomy.database.category.CategoryRepository;
import io.github.alansanchezp.gnomy.database.transaction.MoneyTransactionRepository;

public class RepositoryBuilder {
    private static Class<?> MockRepositoryBuilder = null;
    private static boolean alreadyChecked = false;

    public static <C> C getRepository(Class<C> repositoryClass,
                                               Context context) {
        if (!repositoryClass.equals(AccountRepository.class) &&
                !repositoryClass.equals(MoneyTransactionRepository.class) &&
                !repositoryClass.equals(CategoryRepository.class))
            throw new RuntimeException("Invalid repository class.");

        initOrIgnoreMockBuilder();
        try {
            if (MockRepositoryBuilder == null) {
                return repositoryClass.cast(
                        repositoryClass.getConstructor(Context.class).newInstance(context));
            } else {
                return repositoryClass.cast(
                        MockRepositoryBuilder.getMethod("getMockRepository", Class.class)
                                .invoke(null, repositoryClass));
            }
        } catch (IllegalAccessException |
                InvocationTargetException |
                NoSuchMethodException |
                InstantiationException e) {
            throw new RuntimeException("An error occurred trying to retrieve the specified repository.", e);
        }
    }

    private static void initOrIgnoreMockBuilder() {
        if (alreadyChecked) return;
        try {
            // DO NOT IMPLEMENT THIS CLASS IN MAIN SOURCE SET
            // IT EXISTS ONLY FOR TESTING PURPOSES
            MockRepositoryBuilder = Class.forName("io.github.alansanchezp.gnomy.database.MockRepositoryBuilder");
        } catch (ClassNotFoundException e) {
            Log.d("GnomyDatabase", "getMockRepositoryBuilder: MockBuilder not found.");
            MockRepositoryBuilder = null;
        }
        alreadyChecked = true;
    }
}
