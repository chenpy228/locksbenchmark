package xyz.supercoder.locksbenchmark;

public class ProgressBar {

    private static final int BAR_TOTAL_LENGTH = 10;

    public static void show(int percentage) {
        if (percentage < 0 || percentage > 100) {
            return;
        }

        // Back the cursor to the head of line, and show the progress.
        // We don't want to see the bars, when the percentage is 100.
        reset();
        if (percentage != 100) {
            draw(percentage);
        }
    }

    /**
     * The format is as follows:
     * Progress: =====>      | 50%
     *
     * @param percentage This value should between [0, 100]
     */
    private static void draw(int percentage) {
        StringBuilder progress = new StringBuilder("Progress: ");

        int barLength = percentage * BAR_TOTAL_LENGTH / 100;
        for (int i = 0; i < barLength; i++) {
            progress.append('=');
        }

        progress.append('>');

        for (int i = 0; i < BAR_TOTAL_LENGTH - barLength; i++) {
            progress.append(' ');
        }

        progress.append(String.format("| %d%%", percentage));
        System.out.print(progress);
    }

    /**
     * Back to the head of line.
     */
    private static void reset() {
        System.out.print('\r');
    }
}
