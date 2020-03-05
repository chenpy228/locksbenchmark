package xyz.supercoder.locksbenchmark;

import org.apache.commons.cli.*;

import java.util.Optional;

public class Strategy {
    private static final long MIN_TARGET_VALUE = 10000L;
    private static final long MAX_TARGET_VALUE = 100000000000000L;
    private static final long DEFAULT_TARGET_VALUE = 1000000L;

    private static final int MIN_READER_THREADS = 1;
    private static final int MAX_READER_THREADS = 1000000;
    private static final int DEFAULT_READER_THREADS = 1;

    private static final int MIN_WRITER_THREADS = 1;
    private static final int MAX_WRITER_THREADS = 1000000;
    private static final int DEFAULT_WRITER_THREADS = 1;

    private static final int MIN_ROUNDS = 5;
    private static final int MAX_ROUNDS = 1000000;
    private static final int DEFAULT_ROUNDS = 5;

    private static String targetValueDesc;
    private static String readerThreadsDesc;
    private static String writerThreadsDesc;
    private static String roundsDesc;

    private long targetValue;
    private int readerThreads;
    private int writerThreads;
    private int rounds;

    private static Options options = new Options();

    static {
        options.addOption(Option.builder("h").longOpt("help").build());

        readerThreadsDesc = makeDesc("The number of reader threads",
                MIN_READER_THREADS, MAX_READER_THREADS, DEFAULT_READER_THREADS);
        options.addOption(Option.builder("r").longOpt("readers").desc(readerThreadsDesc)
                .hasArg(true).type(Integer.class).build());

        writerThreadsDesc = makeDesc("The number of writer threads",
                MIN_WRITER_THREADS, MAX_WRITER_THREADS, DEFAULT_WRITER_THREADS);
        options.addOption(Option.builder("w").longOpt("writers").desc(writerThreadsDesc)
                .hasArg(true).type(Integer.class).build());

        roundsDesc = makeDesc("The rounds of testing",
                MIN_ROUNDS, MAX_ROUNDS, DEFAULT_ROUNDS);
        options.addOption(Option.builder("R").longOpt("rounds").desc(roundsDesc)
                .hasArg(true).type(Integer.class).build());

        targetValueDesc = makeDesc("The target value",
                MIN_TARGET_VALUE, MAX_TARGET_VALUE, DEFAULT_TARGET_VALUE);
        options.addOption(Option.builder("t").longOpt("target").desc(targetValueDesc)
                .hasArg(true).type(Long.class).build());
    }

    private static String makeDesc(String shortDesc, long minValue, long maxValue, long defaultValue) {
        return String.format("%s, MUST between [%d, %d], default is %d.",
                shortDesc, minValue, maxValue, defaultValue);
    }

    private Strategy() {
        this.targetValue = DEFAULT_TARGET_VALUE;
        this.readerThreads = DEFAULT_READER_THREADS;
        this.writerThreads = DEFAULT_WRITER_THREADS;
        this.rounds = DEFAULT_ROUNDS;
    }

    public static Optional<Strategy> parseStrategy(String[] args) {
        Strategy strategy = new Strategy();
        try {
            CommandLine commandLine = new DefaultParser().parse(options, args);
            if (commandLine.hasOption("h")) {
                printHelpString();
                return Optional.empty();
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
        } catch (ParseException | IllegalArgumentException e) {
            e.printStackTrace();
            printHelpString();
            return Optional.empty();
        }

        return Optional.of(strategy);
    }

    private static void printHelpString() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar locksbenchmark-1.0.jar", options);
    }

    public long getTargetValue() {
        return targetValue;
    }

    private void setTargetValue(long targetValue) {
        if ((targetValue < MIN_TARGET_VALUE) || (targetValue > MAX_TARGET_VALUE)) {
            throw new IllegalArgumentException(targetValueDesc);
        }

        this.targetValue = targetValue;
    }

    public int getReaderThreads() {
        return readerThreads;
    }

    private void setReaderThreads(int readerThreads) {
        if ((readerThreads < MIN_READER_THREADS) || (readerThreads > MAX_READER_THREADS)) {
            throw new IllegalArgumentException(readerThreadsDesc);
        }

        this.readerThreads = readerThreads;
    }

    public int getWriterThreads() {
        return writerThreads;
    }

    private void setWriterThreads(int writerThreads) {
        if ((writerThreads < MIN_WRITER_THREADS) || (writerThreads > MAX_WRITER_THREADS)) {
            throw new IllegalArgumentException(writerThreadsDesc);
        }

        this.writerThreads = writerThreads;
    }

    public int getTotalThreads() {
        return this.readerThreads + this.writerThreads;
    }

    public int getRounds() {
        return rounds;
    }

    private void setRounds(int rounds) {
        if ((rounds < MIN_ROUNDS) || (rounds > MAX_ROUNDS)) {
            throw new IllegalArgumentException(roundsDesc);
        }

        this.rounds = rounds;
    }

    @Override
    public String toString() {
        return String.format(
                "Reader threads: %d, writer threads: %d, rounds: %d, target value: %d",
                this.readerThreads,
                this.writerThreads,
                this.rounds,
                this.targetValue
        );
    }
}
