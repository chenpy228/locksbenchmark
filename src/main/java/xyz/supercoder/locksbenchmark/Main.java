package xyz.supercoder.locksbenchmark;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        Strategy strategy;
        Optional<Strategy> optionalStrategy = Strategy.parseStrategy(args);
        if (optionalStrategy.isPresent()) {
            strategy = optionalStrategy.get();
        } else {
            return;
        }

        printBasicInfo(strategy);

        Map<String, Long> resultMap = new HashMap<>();
        for (Counter counter : Counter.values()) {
            resultMap.put(counter.name(), testCounter(counter, strategy));
        }

        // sort the results and print it as follows:
        // AtomicLong(1ms) > LongAdder(2ms) > Volatile(3ms) > ...
        String result = resultMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> String.format("%s(%dms)", e.getKey(), e.getValue()))
                .collect(Collectors.joining(" > "));
        System.out.println(result);
    }

    private static void printBasicInfo(Strategy strategy) {
         System.out.println(String.format(
                "CPUs: %d, Arch: %s, Vendor: %s, JRE version: %s",
                Runtime.getRuntime().availableProcessors(),
                System.getProperty("os.arch"),
                System.getProperty("java.vendor"),
                System.getProperty("java.version")));

         System.out.println(strategy);
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
        long avg = Arrays.stream(results).sorted().skip(1)  // remove min value
                .sorted(Comparator.reverseOrder()).skip(1)  // remove max value
                .collect(Collectors.averagingLong(Long::valueOf)).longValue();

        System.out.println(String.format("average(ms): %d, details(ms): %s", avg, Arrays.toString(results)));
        return avg;
    }
}
