package xyz.supercoder.locksbenchmark;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.*;
import java.util.stream.Collectors;

public enum Counter {

    Raw() {
        private long value = 0;

        @Override
        public void reset() {
            this.value = 0;
        }

        @Override
        public long get() {
            return this.value;
        }

        @Override
        public void increment() {
            this.value++;
        }
    },

    Volatile() {
        private volatile long value = 0;

        @Override
        public void reset() {
            this.value = 0;
        }

        @Override
        public long get() {
            return this.value;
        }

        @Override
        public void increment() {
            this.value++;
        }
    },

    AtomicLong() {
        private final AtomicLong value = new AtomicLong(0);

        @Override
        public void reset() {
            this.value.set(0);
        }

        @Override
        public long get() {
            return this.value.get();
        }

        @Override
        public void increment() {
            this.value.incrementAndGet();
        }
    },

    LongAdder() {
        private final LongAdder value = new LongAdder();

        @Override
        public void reset() {
            this.value.reset();
        }

        @Override
        public long get() {
            return this.value.longValue();
        }

        @Override
        public void increment() {
            this.value.increment();
        }
    },

    StampedLock() {
        private final StampedLock stampedLock = new StampedLock();
        private long value = 0;

        @Override
        public void reset() {
            this.value = 0;
        }

        @Override
        public long get() {
            long stamp = stampedLock.readLock();
            try {
                return this.value;
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }

        @Override
        public void increment() {
            long stamp = stampedLock.writeLock();
            try {
                this.value++;
            } finally {
                stampedLock.unlockWrite(stamp);
            }
        }
    },

    OptimisticStampedLock() {
        private final StampedLock stampedLock = new StampedLock();
        private long value = 0;

        @Override
        public void reset() {
            this.value = 0;
        }

        @Override
        public long get() {
            long stamp = stampedLock.tryOptimisticRead();
            long result = this.value;
            if (!stampedLock.validate(stamp)) {
                stamp = stampedLock.readLock();
                try {
                    result = this.value;
                } finally {
                    stampedLock.unlockRead(stamp);
                }
            }

            return result;
        }

        @Override
        public void increment() {
            long stamp = stampedLock.writeLock();
            try {
                this.value++;
            } finally {
                stampedLock.unlockWrite(stamp);
            }
        }
    },

    RWLock() {
        private final transient ReadWriteLock rwlock = new ReentrantReadWriteLock();
        private final transient Lock rlock = rwlock.readLock();
        private final transient Lock wlock = rwlock.writeLock();

        private long value = 0;

        @Override
        public void reset() {
            this.value = 0;
        }

        @Override
        public long get() {
            rlock.lock();
            try {
                return this.value;
            } finally {
                rlock.unlock();
            }
        }

        @Override
        public void increment() {
            wlock.lock();
            try {
                this.value++;
            } finally {
                wlock.unlock();
            }
        }
    },

    Synchronized() {
        private final transient Object lock = new Object();

        private long value = 0;

        @Override
        public void reset() {
            this.value = 0;
        }

        @Override
        public long get() {
            synchronized (lock) {
                return this.value;
            }
        }

        @Override
        public void increment() {
            synchronized (lock) {
                this.value++;
            }
        }
    },

    FairReentrantLock() {
        private final ReentrantLock lock = new ReentrantLock(true);
        private long value = 0;

        @Override
        public void reset() {
            this.value = 0;
        }

        @Override
        public long get() {
            lock.lock();
            try {
                return this.value;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void increment() {
            lock.lock();
            try {
                this.value++;
            } finally {
                lock.unlock();
            }
        }
    },

    NonfairReentrantLock() {
        private final ReentrantLock lock = new ReentrantLock(false);
        private long value = 0;

        @Override
        public void reset() {
            this.value = 0;
        }

        @Override
        public long get() {
            lock.lock();
            try {
                return this.value;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void increment() {
            lock.lock();
            try {
                this.value++;
            } finally {
                lock.unlock();
            }
        }
    };

    public abstract void reset();  // maybe not thread safe
    public abstract long get();
    public abstract void increment();

    public long benchmark(Strategy strategy) {
        System.out.println("Testing synchronization mechanism: " + this.name());

        int rounds = strategy.getRounds();
        long[] results = new long[rounds];
        for (int round = 0; round < rounds; round++) {
            this.reset();

            // The main purpose of this barrier is to wait for all threads to start working,
            // and then record the start time.
            final long[] startTime = new long[1];
            CyclicBarrier startWorkingBarrier = new CyclicBarrier(strategy.getTotalThreads(),
                    () -> startTime[0] = System.currentTimeMillis());

            CountDownLatch stopWorkingLatch = new CountDownLatch(1);

            ExecutorService executorService = Executors.newFixedThreadPool(strategy.getTotalThreads());
            for (int i = 0; i < strategy.getReaderThreads(); i++) {
                executorService.submit(new Reader(this, startWorkingBarrier, stopWorkingLatch,
                        strategy.getTargetValue()));
            }

            for (int i = 0; i < strategy.getWriterThreads(); i++) {
                executorService.submit(new Writer(this, startWorkingBarrier, stopWorkingLatch));
            }

            try {
                // waiting for the fastest thread to finish working
                stopWorkingLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            results[round] = System.currentTimeMillis() - startTime[0];

            // shutdown thread pool and spin to wait
            executorService.shutdownNow();
            while (!executorService.isShutdown()) {
                Thread.yield();
            }

            // update progress
            ProgressBar.show((round + 1) * 100 / rounds);
        }

        // remove the min and max value, and calculate the average
        long avg = Arrays.stream(results).boxed()
                .sorted().skip(1)                           // remove min value
                .sorted(Comparator.reverseOrder()).skip(1)  // remove max value
                .collect(Collectors.averagingLong(Long::valueOf)).longValue();

        System.out.println(String.format("average(ms): %d, details(ms): %s", avg, Arrays.toString(results)));
        return avg;
    }
}
