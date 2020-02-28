package xyz.supercoder.locksbenchmark;

public class Strategy {
    public static long TARGET_VALUE = 1000000L;
    public static int READER_THREADS = 1; // must bigger than 1
    public static int WRITER_THREADS = 1; // must bigger than 1
    public static int ROUNDS = 10;        // must bigger than 5

    private long targetValue;
    private int readerThreads;
    private int writerThreads;
    private int rounds;

    public Strategy() {
        this.targetValue = TARGET_VALUE;
        this.readerThreads = READER_THREADS;
        this.writerThreads = WRITER_THREADS;
        this.rounds = ROUNDS;
    }

    public long getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(long targetValue) {
        this.targetValue = targetValue;
    }

    public int getReaderThreads() {
        return readerThreads;
    }

    public void setReaderThreads(int readerThreads) {
        if (readerThreads < 1) {
            throw new IllegalArgumentException("The reader threads MUST bigger than 1.");
        }

        this.readerThreads = readerThreads;
    }

    public int getWriterThreads() {
        return writerThreads;
    }

    public void setWriterThreads(int writerThreads) {
        if (writerThreads < 1) {
            throw new IllegalArgumentException("The writer threads MUST bigger than 1.");
        }

        this.writerThreads = writerThreads;
    }

    public int getTotalThreads() {
        return this.readerThreads + this.writerThreads;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        if (rounds < 5) {
            throw new IllegalArgumentException("The rounds MUST bigger than 5.");
        }

        this.rounds = rounds;
    }

    @Override
    public String toString() {
        return String.format(
                "Reader thread number: %d, writer thread number: %d, rounds: %d, target value: %d",
                this.readerThreads,
                this.writerThreads,
                this.rounds,
                this.targetValue
        );
    }
}
