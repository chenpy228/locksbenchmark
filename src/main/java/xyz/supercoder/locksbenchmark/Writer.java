package xyz.supercoder.locksbenchmark;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Writer implements Runnable {

	private final Counter counter;

	private final CyclicBarrier startWorkBarrier;

	private final CountDownLatch stopWorkLatch;

	public Writer(Counter counter,
				  CyclicBarrier startWorkBarrier,
				  CountDownLatch stopWorkLatch) {
		this.counter = counter;
		this.startWorkBarrier = startWorkBarrier;
		this.stopWorkLatch = stopWorkLatch;
	}
	
	public void run() {
		try {
			startWorkBarrier.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}

		while (!Thread.interrupted()) {
			counter.increment();
		}

		stopWorkLatch.countDown();
	}
}
