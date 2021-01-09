package io.github.alansanchezp.gnomy.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

public class DatabaseTriggersTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
    }

    @After
    public void cleanDatabase() {
    }

    // Tests Database trigger
    @Test
    public void prepopulates_database() {
        assert true;
    }
}
