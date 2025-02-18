package com.taboola.async_profiler.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class ThreadUtils {

    public ThreadGroup getRootThreadGroup() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while (threadGroup.getParent() != null) {
            threadGroup = threadGroup.getParent();
        }

        return threadGroup;
    }

    public Collection<Thread> getAllThreads(Predicate<Thread> threadPredicate) {
        ThreadGroup group = getRootThreadGroup();
        int count = group.activeCount();
        Thread[] threads;
        do {
            threads = new Thread[count + (count / 2) + 1];
            count = group.enumerate(threads, true);
        } while (count >= threads.length);

        final List<Thread> result = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            if (threadPredicate.test(threads[i])) {
                result.add(threads[i]);
            }
        }

        return Collections.unmodifiableCollection(result);
    }

    public void sleep(long duration, TimeUnit timeUnit) throws InterruptedException {
        Thread.sleep(timeUnit.toMillis(duration));
    }

    public ExecutorService newDaemonsExecutorService(int corePoolSize, int maxPoolSize, int queueCapacity) {
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueCapacity), new ThreadFactory() {
            final AtomicInteger threadCnt = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("async-profiler-actuator-endpoint-" + threadCnt.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }
}