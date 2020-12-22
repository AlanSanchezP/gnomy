package io.github.alansanchezp.gnomy.database;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class MockRepositoryBuilder {
    private static Map<Class<?>, Object> mockRepositoriesMap = new HashMap<>();

    public static <C> C initMockRepository(Class<C> repositoryClass) {
        C mockRepository = mock(repositoryClass);
        mockRepositoriesMap.put(repositoryClass, mockRepository);
        return mockRepository;
    }

    public static Object getMockRepository(Class<?> repositoryClass) {
        return mockRepositoriesMap.get(repositoryClass);
    }
}
