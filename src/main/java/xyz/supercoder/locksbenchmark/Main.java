package xyz.supercoder.locksbenchmark;

import java.util.*;
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
            resultMap.put(counter.name(), counter.benchmark(strategy));
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
}
