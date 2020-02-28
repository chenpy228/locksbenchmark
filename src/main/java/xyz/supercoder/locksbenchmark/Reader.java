package xyz.supercoder.locksbenchmark;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Reader implements Runnable {

    private final Counter counter;

    private final long maxValue;

    private final CyclicBarrier startWorkBarrier;

    private final CountDownLatch stopWorkLatch;

    Reader(Counter counter,
           CyclicBarrier startWorkBarrier,
           CountDownLatch stopWorkLatch,
           long maxValue) {
        this.counter = counter;
        this.maxValue = maxValue;

        this.startWorkBarrier = startWorkBarrier;
        this.stopWorkLatch = stopWorkLatch;
    }

    @Override
    public void run()
    {
        try {
            startWorkBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        while (!Thread.interrupted()) {
            long value = counter.get();
            if (value> maxValue) {
                break;
            }
        }

        stopWorkLatch.countDown();
    }
}
