package simulator;

import java.util.ArrayList;
import java.util.List;


public class FIFO implements PageReplacementAlgorithm {

    @Override
    public String getName() {
        return "FIFO";
    }

    @Override
    public int run(List<Integer> pages, int frames) {
        PageReplacementAlgorithm.validate(pages, frames);

        List<Integer> memory = new ArrayList<>();
        int pageFaults = 0;

        for (int page : pages) {
            if (memory.contains(page)) {
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