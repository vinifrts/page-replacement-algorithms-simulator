package simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageReplacementAlgorithmsTest {

    private final PageReplacementAlgorithm fifo = new FIFO();
    private final PageReplacementAlgorithm lru = new LRU();
    private final PageReplacementAlgorithm nfu = new NFU();
    private final PageReplacementAlgorithm optimal = new Optimal();

    private final List<PageReplacementAlgorithm> all = List.of(fifo, lru, nfu, optimal);

    private static List<Integer> refs(Integer... pages) {
        return Arrays.asList(pages);
    }

    private void assertAllEqual(int expected, List<Integer> pages, int frames) {
        for (PageReplacementAlgorithm algorithm : all) {
            assertEquals(expected, algorithm.run(pages, frames), algorithm.getName());
        }
    }


    private final List<Integer> known = refs(1, 2, 3, 1, 4, 2, 5, 1, 2, 3, 4, 5);

    @Test
    void knownCaseFifo() {
        assertEquals(10, fifo.run(known, 3));
    }

    @Test
    void knownCaseLru() {
        assertEquals(10, lru.run(known, 3));
    }

    @Test
    void knownCaseNfu() {
        assertEquals(9, nfu.run(known, 3));
    }

    @Test
    void knownCaseOptimal() {
        assertEquals(7, optimal.run(known, 3));
    }

    @Test
    void optimalIsNeverWorseThanOthers() {
        for (int frames = 1; frames <= 6; frames++) {
            int best = optimal.run(known, frames);
            assertTrue(best <= fifo.run(known, frames), "FIFO with " + frames + " frames");
            assertTrue(best <= lru.run(known, frames), "LRU with " + frames + " frames");
            assertTrue(best <= nfu.run(known, frames), "NFU with " + frames + " frames");
        }
    }

    // ---------- Casos de borda ----------

    @Test
    void emptySequenceHasNoFaults() {
        assertAllEqual(0, refs(), 3);
    }

    @Test
    void singlePageHasOneFault() {
        assertAllEqual(1, refs(5), 3);
    }

    @Test
    void repeatedReferencesFaultOnlyOnce() {
        assertAllEqual(1, refs(1, 1, 1, 1, 1), 3);
    }

    @Test
    void oneFrameFaultsOnEveryPageChange() {
        assertAllEqual(4, refs(1, 2, 1, 2), 1);
        assertAllEqual(3, refs(1, 1, 2, 2, 3), 1);
    }

    @Test
    void moreFramesThanDistinctPagesFaultsOncePerPage() {
        assertAllEqual(3, refs(1, 2, 3, 1, 2, 3, 1), 5);
    }

    @Test
    void invalidFramesAreRejected() {
        for (PageReplacementAlgorithm algorithm : all) {
            assertThrows(IllegalArgumentException.class, () -> algorithm.run(known, 0));
            assertThrows(IllegalArgumentException.class, () -> algorithm.run(known, -2));
            assertThrows(IllegalArgumentException.class, () -> algorithm.run(null, 3));
        }
    }

    @Test
    void algorithmsDoNotKeepStateBetweenRuns() {
        for (PageReplacementAlgorithm algorithm : all) {
            int first = algorithm.run(known, 3);
            int second = algorithm.run(known, 3);
            assertEquals(first, second, algorithm.getName());
        }
    }

    // ---------- NFU ----------

    @Test
    void nfuTieIsBrokenByOldestPageInMemory() {
        assertEquals(3, nfu.run(refs(1, 2, 3, 2), 2));
    }

    @Test
    void nfuKeepsFrequentlyUsedPage() {
        List<Integer> pages = refs(1, 1, 2, 3, 1);
        assertEquals(3, nfu.run(pages, 2));
        assertEquals(4, fifo.run(pages, 2));
        assertEquals(4, lru.run(pages, 2));
    }

    // ---------- Ótimo ----------

    @Test
    void optimalEvictsPageThatIsNeverUsedAgain() {
        List<Integer> pages = refs(1, 2, 3, 1);
        assertEquals(3, optimal.run(pages, 2));
        assertEquals(4, fifo.run(pages, 2));
    }
}