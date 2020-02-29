package xyz.supercoder.locksbenchmark;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

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
                stampedLock.unlock(stamp);
            }
        }

        @Override
        public void increment() {
            long stamp = stampedLock.writeLock();
            try {
                this.value++;
            } finally {
                stampedLock.unlock(stamp);
            }
        }
    },

    RWLock() {
        private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
        private final Lock rlock = rwlock.readLock();
        private final Lock wlock = rwlock.writeLock();

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
        private final Object lock = new Object();

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
    };

    public abstract void reset();
    public abstract long get();
    public abstract void increment();
}
