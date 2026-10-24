package simulator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public class Main {

    private static final Path INPUT_FILE = Path.of("data", "pages.csv");
    private static final Path RESULTS_DIR = Path.of("results");
    private static final Path RESULTS_FILE = RESULTS_DIR.resolve("results.csv");
    private static final Path BATCH_FILE = RESULTS_DIR.resolve("batch_results.csv");

    private static final int DEFAULT_MIN_FRAMES = 2;
    private static final int DEFAULT_MAX_FRAMES = 6;

    private static final String CSV_HEADER = "algorithm,frames,page_faults,total_references,page_hits";

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        if (args.length > 3 || args.length == 2) {
            throw new IllegalArgumentException(
                    "Usage: Main [frames] [minFrames maxFrames]");
        }

        int frames = readFrames(args);
        int minFrames = DEFAULT_MIN_FRAMES;
        int maxFrames = DEFAULT_MAX_FRAMES;
        if (args.length == 3) {
            minFrames = parsePositive(args[1], "Minimum frames");
            maxFrames = parsePositive(args[2], "Maximum frames");
            if (maxFrames < minFrames) {
                throw new IllegalArgumentException(
                        "Maximum frames must be greater than or equal to minimum frames.");
            }
        }

        List<Integer> pages = CsvReader.read(INPUT_FILE);

        List<PageReplacementAlgorithm> algorithms = List.of(
                new FIFO(), new LRU(), new NFU(), new Optimal());

        // Execução principal (quantidade de quadros escolhida pelo usuário)
        printResults(algorithms, pages, frames);
        List<String> mainLines = new ArrayList<>();
        mainLines.add(CSV_HEADER);
        for (PageReplacementAlgorithm algorithm : algorithms) {
            mainLines.add(csvLine(algorithm, pages, frames));
        }
        writeCsv(RESULTS_FILE, mainLines);

        // Execução em lote (todas as combinações algoritmo x quadros)
        List<String> batchLines = new ArrayList<>();
        batchLines.add(CSV_HEADER);
        for (int f = minFrames; f <= maxFrames; f++) {
            for (PageReplacementAlgorithm algorithm : algorithms) {
                batchLines.add(csvLine(algorithm, pages, f));
            }
        }
        writeCsv(BATCH_FILE, batchLines);

        System.out.println();
        System.out.println("Results saved to: " + RESULTS_FILE);
        System.out.println("Batch results (" + minFrames + " to " + maxFrames
                + " frames) saved to: " + BATCH_FILE);
    }

    /** lê a quantidade de quadros do argumento ou pergunta no terminal. */
    private static int readFrames(String[] args) {
        if (args.length >= 1) {
            return parsePositive(args[0], "Number of frames");
        }
        System.out.print("Enter the number of memory frames: ");
        Scanner scanner = new Scanner(System.in);
        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException("No number of frames was provided.");
        }
        return parsePositive(scanner.nextLine(), "Number of frames");
    }

    private static int parsePositive(String text, String name) {
        int value;
        try {
            value = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be an integer, but got: '" + text + "'.");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero.");
        }
        return value;
    }

    private static void printResults(List<PageReplacementAlgorithm> algorithms,
                                     List<Integer> pages, int frames) {
        String sequence = pages.stream().map(String::valueOf).collect(Collectors.joining(", "));

        System.out.println("=== Page Replacement Results ===");
        System.out.println("Reference string: " + sequence);
        System.out.println("Number of frames: " + frames);
        System.out.println();
        for (PageReplacementAlgorithm algorithm : algorithms) {
            int faults = algorithm.run(pages, frames);
            int hits = pages.size() - faults;
            System.out.println(algorithm.getName() + ": " + faults
                    + " page faults (" + hits + " page hits)");
        }
        System.out.println();
        System.out.println("Note: Optimal needs knowledge of future references, so it is a "
                + "theoretical benchmark, not a practical algorithm.");
    }

    private static String csvLine(PageReplacementAlgorithm algorithm,
                                  List<Integer> pages, int frames) {
        int faults = algorithm.run(pages, frames);
        int total = pages.size();
        return algorithm.getName() + "," + frames + "," + faults + "," + total + "," + (total - faults);
    }

    private static void writeCsv(Path file, List<String> lines) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}