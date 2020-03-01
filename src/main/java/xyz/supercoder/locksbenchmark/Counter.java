package xyz.supercoder.locksbenchmark;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.*;

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

    public abstract void reset();  // maybe not thread safe
    public abstract long get();
    public abstract void increment();
}
