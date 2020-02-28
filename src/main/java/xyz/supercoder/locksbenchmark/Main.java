package xyz.supercoder.locksbenchmark;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {
	public static void main(String[] args) {
        System.out.println(String.format(
                "CPUs: %d, Arch: %s, Vendor: %s, JRE version: %s",
                Runtime.getRuntime().availableProcessors(),
                System.getProperty("os.arch"),
                System.getProperty("java.vendor"),
                System.getProperty("java.version")));

        Strategy strategy = new Strategy();
        System.out.println(strategy);

        Map<String, Long> resultMap = new HashMap<>();
		for (Counter counter : Counter.values()) {
		    resultMap.put(counter.name(), testCounter(counter, strategy));
		}

		String result = resultMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> String.format("%s(%d)", e.getKey(), e.getValue()))
                .collect(Collectors.joining(" > "));
        System.out.println(result);
	}

	private static long testCounter(Counter counter, Strategy strategy) {
        System.out.println("Testing synchronization mechanism: " + counter.name());

        int rounds = strategy.getRounds();
        Long[] results = new Long[rounds];
        for (int round = 0; round < rounds; round++) {
            counter.reset();

            // The main purpose of this barrier is to wait for all threads to start working,
            // and then record the start time.
            final long[] startTime = new long[1];
            CyclicBarrier startWorkingBarrier = new CyclicBarrier(strategy.getTotalThreads(),
                    () -> startTime[0] = System.currentTimeMillis());

            CountDownLatch stopWorkingLatch = new CountDownLatch(1);

            ExecutorService executorService = Executors.newFixedThreadPool(strategy.getTotalThreads());
            for (int i = 0; i < strategy.getReaderThreads(); i++) {
                executorService.submit(new Reader(counter, startWorkingBarrier, stopWorkingLatch,
                        strategy.getTargetValue()));
            }

            for (int i = 0; i < strategy.getWriterThreads(); i++) {
                executorService.submit(new Writer(counter, startWorkingBarrier, stopWorkingLatch));
            }

            try {
                // waiting for the fastest thread to finish working
                stopWorkingLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        long avg = Arrays.stream(results).sorted().skip(1)  // remove min value
                .sorted(Comparator.reverseOrder()).skip(1)  // remove max value
                .collect(Collectors.averagingLong(Long::valueOf)).longValue();

        System.out.println(String.format("average(ms): %d, details(ms): %s", avg, Arrays.toString(results)));
        return avg;
    }
}
