package com.shopelia.android.app.tracking;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

class SerialExecutor implements Executor {
    final Queue<Runnable> tasks = new ConcurrentLinkedQueue<Runnable>();
    final Executor executor;
    Runnable active;

    SerialExecutor() {
        this.executor = new ThreadPoolExecutor(1, 1, 60, java.util.concurrent.TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(16));
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
    }

    @Override
    public synchronized void execute(final Runnable r) {
        tasks.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (active == null) {
            scheduleNext();
        }
    }
}
