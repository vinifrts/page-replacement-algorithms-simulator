package simulator;

import java.util.ArrayList;
import java.util.List;


public class LRU implements PageReplacementAlgorithm {

    @Override
    public String getName() {
        return "LRU";
    }

    @Override
    public int run(List<Integer> pages, int frames) {
        PageReplacementAlgorithm.validate(pages, frames);

        
        List<Integer> memory = new ArrayList<>();
        int pageFaults = 0;

        for (int page : pages) {
            if (memory.contains(page)) {
                memory.remove(Integer.valueOf(page));
                memory.add(page);
                continue;
            }

            pageFaults++;
            if (memory.size() == frames) {
                memory.remove(0); 
            }
            memory.add(page);
        }
        return pageFaults;
    }
}