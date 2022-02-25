package com.taboola.async_profiler.utils;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class RecurringRunnableTest {

    @Test
    public void testRun_whenActive_shouldRunBaseRunnableInLoop() {
        Runnable baseRunnable = spy(new DummyRunnable());

        RecurringRunnable recurringRunnable = new RecurringRunnable(baseRunnable);

        try {
            recurringRunnable.run();
        } catch (RuntimeException e) {
        }

        verify(baseRunnable, times(5)).run();
    }

    @Test
    public void testRunWhenCancelled() {
        Runnable runnable = spy(new DummyRunnable());

        RecurringRunnable recurringRunnable = new RecurringRunnable(runnable);
        recurringRunnable.cancel();

        try {
            recurringRunnable.run();
        } catch (RuntimeException e) {
        }

        verifyZeroInteractions(runnable);
    }

    static class DummyRunnable implements Runnable {

        private AtomicInteger runCount = new AtomicInteger(1);

        @Override
        public void run() {
            if (runCount.getAndIncrement() < 5) {
                return;
            }

            //throwing to get out of the infinite loop after the 5th call
            throw new RuntimeException("dummy runnable for test");
        }
    }
}