package xyz.supercoder.locksbenchmark;

import org.apache.commons.cli.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {
    private static Options options = new Options();

    public static void main(String[] args) {
        Strategy strategy = new Strategy();
        parseStrategy(args, strategy);
        System.out.println(strategy);

        System.out.println(String.format(
                "CPUs: %d, Arch: %s, Vendor: %s, JRE version: %s",
                Runtime.getRuntime().availableProcessors(),
                System.getProperty("os.arch"),
                System.getProperty("java.vendor"),
                System.getProperty("java.version")));

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

    private static void parseStrategy(String[] args, Strategy strategy) {
        options.addOption(Option.builder("h").longOpt("help").build());
        options.addOption(Option.builder("r").longOpt("readers")
                .desc("The number of reader threads, MUST bigger than 0, default is 1.")
                .hasArg(true).type(Long.class).build());
        options.addOption(Option.builder("w").longOpt("writers")
                .desc("The number of writer threads, MUST bigger than 0, default is 1.")
                .hasArg(true).type(Long.class).build());
        options.addOption(Option.builder("R").longOpt("rounds")
                .desc("The rounds of testing, MUST bigger than 5, default is 10.")
                .hasArg(true).type(Long.class).build());
        options.addOption(Option.builder("t").longOpt("target")
                .desc("The target value, MUST bigger than 0, default is 1000000.")
                .hasArg(true).type(Long.class).build());

        try {
            CommandLine commandLine = new DefaultParser().parse(options, args);
            if (commandLine.hasOption("h")) {
                System.out.println(getHelpString());
                System.exit(0);
            }

            if (commandLine.hasOption("r")) {
                strategy.setReaderThreads(Integer.parseInt(commandLine.getOptionValue("r")));
            }

            if (commandLine.hasOption("w")) {
                strategy.setWriterThreads(Integer.parseInt(commandLine.getOptionValue("w")));
            }

            if (commandLine.hasOption("R")) {
                strategy.setRounds(Integer.parseInt(commandLine.getOptionValue("R")));
            }

            if (commandLine.hasOption("t")) {
                strategy.setTargetValue(Long.parseLong(commandLine.getOptionValue("t")));
            }
        } catch (ParseException e) {
            System.out.println(getHelpString());
            System.exit(1);
        }
    }

    private static String getHelpString() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(printWriter, HelpFormatter.DEFAULT_WIDTH,
                "java -jar locksbenchmark-1.0.jar", null,
                options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);

        printWriter.flush();
        String helpString = new String(byteArrayOutputStream.toByteArray());
        printWriter.close();

        return helpString;
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
