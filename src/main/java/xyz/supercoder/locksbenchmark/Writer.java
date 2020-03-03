package xyz.supercoder.locksbenchmark;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Writer implements Runnable {

    private final Counter counter;

    private final CyclicBarrier startWorkingBarrier;

    private final CountDownLatch stopWorkingLatch;

    Writer(Counter counter,
           CyclicBarrier startWorkingBarrier,
           CountDownLatch stopWorkingLatch) {
        this.counter = counter;

        this.startWorkingBarrier = startWorkingBarrier;
        this.stopWorkingLatch = stopWorkingLatch;
    }

    public void run() {
        try {
            startWorkingBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        while (!Thread.interrupted()) {
            counter.increment();
        }

        stopWorkingLatch.countDown();
    }
}
