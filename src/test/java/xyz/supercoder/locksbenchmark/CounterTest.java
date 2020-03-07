package xyz.supercoder.locksbenchmark;

import org.junit.Assert;
import org.junit.Test;

public class CounterTest {
    @Test
    public void testReset() {
        for (Counter counter : Counter.values()) {
            counter.reset();
            Assert.assertEquals(0, counter.get());
        }
    }

    @Test
    public void testIncrement() {
        for (Counter counter : Counter.values()) {
            counter.reset();
            counter.increment();
            Assert.assertEquals(1, counter.get());
        }
    }

    @Test
    public void testBenchmark() {
        for (Counter counter : Counter.values()) {
            long result = counter.benchmark(new Strategy());
            Assert.assertTrue(result > 0);
            break; // just test if the mechanism can work
        }
    }
}
