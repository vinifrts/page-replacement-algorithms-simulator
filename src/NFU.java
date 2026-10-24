package simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NFU implements PageReplacementAlgorithm {

    @Override
    public String getName() {
        return "NFU";
    }

    @Override
    public int run(List<Integer> pages, int frames) {
        PageReplacementAlgorithm.validate(pages, frames);

        List<Integer> memory = new ArrayList<>();
        Map<Integer, Integer> counters = new HashMap<>();
        int pageFaults = 0;

        for (int page : pages) {
            if (memory.contains(page)) {
                counters.put(page, counters.get(page) + 1); // acerto
                continue;
            }

            pageFaults++;
            if (memory.size() == frames) {
                
                int victim = memory.get(0);
                int minCount = counters.get(victim);
                for (int candidate : memory) {
                    int count = counters.get(candidate);
                    if (count < minCount) {
                        victim = candidate;
                        minCount = count;
                    }
                }
                memory.remove(Integer.valueOf(victim));
                counters.remove(victim);
            }
            memory.add(page);
            counters.put(page, 1);
        }
        return pageFaults;
    }
}