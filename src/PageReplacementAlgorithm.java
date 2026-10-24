package simulator;

import java.util.List;

public interface PageReplacementAlgorithm {

    String getName();
    int run(List<Integer> pages, int frames);
    static void validate(List<Integer> pages, int frames) {
        if (pages == null) {
            throw new IllegalArgumentException("The page reference list must not be null.");
        }
        if (frames <= 0) {
            throw new IllegalArgumentException("The number of frames must be greater than zero.");
        }
    }
}