package xyz.supercoder.locksbenchmark;

public class ProgressBar {

    private static final int BAR_TOTAL_LEN = 10;

    public static void show(int percentage) {
        if (percentage < 0 || percentage > 100) {
            return;
        }

        // back to the head of line
        reset();

        draw(percentage);

        // clear progress info when task completed
        if (percentage == 100) {
            reset();
        }
    }

    private static void draw(int percentage) {
        System.out.print("Progress: ");

        int barLength = percentage * BAR_TOTAL_LEN / 100;
        for (int i = 0; i < barLength; i++) {
            System.out.print("=");
        }

        System.out.print(">");

        for (int i = 0; i < BAR_TOTAL_LEN - barLength; i++) {
            System.out.print(" ");
        }

        System.out.print(String.format("| %d%%", percentage));
    }

    private static void reset() {
        System.out.print('\r');
    }
}
