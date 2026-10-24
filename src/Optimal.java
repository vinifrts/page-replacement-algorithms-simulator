package simulator;

import java.util.ArrayList;
import java.util.List;

public class Optimal implements PageReplacementAlgorithm {

    @Override
    public String getName() {
        return "Optimal";
    }

    @Override
    public int run(List<Integer> pages, int frames) {
        PageReplacementAlgorithm.validate(pages, frames);

        List<Integer> memory = new ArrayList<>();
        int pageFaults = 0;

        for (int i = 0; i < pages.size(); i++) {
            int page = pages.get(i);

            if (memory.contains(page)) {
                continue; 
            }

            pageFaults++;
            if (memory.size() < frames) {
                memory.add(page); 
                continue;
            }

            int victimIndex = 0;
            int farthestUse = -1;
            for (int k = 0; k < memory.size(); k++) {
                int nextUse = nextUseIndex(pages, memory.get(k), i);
                if (nextUse > farthestUse) { 
                    farthestUse = nextUse;
                    victimIndex = k;
                }
            }
            memory.set(victimIndex, page);
        }
        return pageFaults;
    }

    
    private int nextUseIndex(List<Integer> pages, int page, int currentIndex) {
        for (int j = currentIndex + 1; j < pages.size(); j++) {
            if (pages.get(j) == page) {
                return j;
            }
        }
        return Integer.MAX_VALUE;
    }
}