package io.github.alansanchezp.gnomy.data;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;

import io.github.alansanchezp.gnomy.data.account.AccountRepository;
import io.github.alansanchezp.gnomy.data.category.CategoryRepository;
import io.github.alansanchezp.gnomy.data.transaction.MoneyTransactionRepository;

/**
 * Helper class to create repository objects. Use instead
 * of direct calls to repository constructors so that instrumented tests
 * can be performed by mocking repositories.
 */
public class RepositoryBuilder {
    private static Class<?> MockRepositoryBuilder = null;
    private static boolean alreadyChecked = false;

    /**
     * Retrieves the desired repository. If mocks are available (should only happen
     * during instrumented tests), pre-established mock instances are returned.
     *
     * @param repositoryClass   Repository class.
     * @param context           Application context.
     * @param <C>               Repository class type to return.
     * @return                  Repository instance.
     */
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

    /**
     * Checks if the MockRepositoryBuilder class exists. It should only exist
     * in instrumented test builds. If it exists,
     * retrieves it to use its methods with reflection.
     * If the check has already been done, the method does nothing.
     */
    private static void initOrIgnoreMockBuilder() {
        if (alreadyChecked) return;
        try {
            // DO NOT IMPLEMENT THIS CLASS IN MAIN SOURCE SET
            // IT EXISTS ONLY FOR TESTING PURPOSES
            MockRepositoryBuilder = Class.forName("io.github.alansanchezp.gnomy.data.MockRepositoryBuilder");
        } catch (ClassNotFoundException e) {
            Log.d("GnomyDatabase", "getMockRepositoryBuilder: MockBuilder not found.");
            MockRepositoryBuilder = null;
        }
        alreadyChecked = true;
    }
}
