package io.github.alansanchezp.gnomy.database;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Helper class to mock repositories during instrumented tests.
 */
public class MockRepositoryBuilder {
    private static Map<Class<?>, Object> mockRepositoriesMap = new HashMap<>();

    /**
     * Creates a new mock object for the desired repository class.
     *
     * @param repositoryClass   Repository class.
     * @param <C>               Repository class type to be returned.
     * @return                  Created mock object.
     */
    public static <C> C initMockRepository(Class<C> repositoryClass) {
        C mockRepository = mock(repositoryClass);
        mockRepositoriesMap.put(repositoryClass, mockRepository);
        return mockRepository;
    }

    /**
     * Retrieves the stored mock object for the given class.
     *
     * @param repositoryClass   Repository class.
     * @return                  Mock object.
     */
    public static Object getMockRepository(Class<?> repositoryClass) {
        return mockRepositoriesMap.get(repositoryClass);
    }
}
