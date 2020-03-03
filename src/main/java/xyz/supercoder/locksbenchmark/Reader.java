package xyz.supercoder.locksbenchmark;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Reader implements Runnable {

    private final Counter counter;

    private final long targetValue;

    private final CyclicBarrier startWorkingBarrier;

    private final CountDownLatch stopWorkingLatch;

    Reader(Counter counter,
           CyclicBarrier startWorkingBarrier,
           CountDownLatch stopWorkingLatch,
           long targetValue) {
        this.counter = counter;
        this.targetValue = targetValue;

        this.startWorkingBarrier = startWorkingBarrier;
        this.stopWorkingLatch = stopWorkingLatch;
    }

    @Override
    public void run() {
        try {
            startWorkingBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        while (!Thread.interrupted()) {
            long value = counter.get();
            if (value > targetValue) {
                break;
            }
        }

        stopWorkingLatch.countDown();
    }
}
