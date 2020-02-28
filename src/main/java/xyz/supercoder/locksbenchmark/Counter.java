package xyz.supercoder.locksbenchmark;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

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
    };

    public abstract void reset();
    public abstract long get();
    public abstract void increment();
}
