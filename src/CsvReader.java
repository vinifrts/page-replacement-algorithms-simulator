package simulator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class CsvReader {

    private static final String HEADER = "pagina";

    public static List<Integer> read(Path file) throws IOException {
        if (file == null || !Files.exists(file)) {
            throw new IOException("Input file not found: " + file);
        }
        if (Files.isDirectory(file)) {
            throw new IOException("Input path is a directory, not a file: " + file);
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        return parse(lines);
    }

    public static List<Integer> parse(List<String> lines) {
        int headerIndex = findFirstNonBlank(lines);
        if (headerIndex < 0) {
            throw new IllegalArgumentException("The CSV file is empty.");
        }

        // replace remove o BOM (\uFEFF) que alguns editores do Windows inserem.
        String header = lines.get(headerIndex).replace("\uFEFF", "").trim();
        if (!header.equalsIgnoreCase(HEADER)) {
            throw new IllegalArgumentException("Invalid header on line " + (headerIndex + 1)
                    + ": expected '" + HEADER + "' but found '" + header + "'.");
        }

        List<Integer> pages = new ArrayList<>();
        for (int i = headerIndex + 1; i < lines.size(); i++) {
            String value = lines.get(i).trim();
            int lineNumber = i + 1;

            if (value.isEmpty()) {
                continue;
            }
            if (value.contains(",") || value.contains(";")) {
                throw new IllegalArgumentException("Malformed row on line " + lineNumber
                        + ": expected a single column but found '" + value + "'.");
            }

            int page;
            try {
                page = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid value on line " + lineNumber
                        + ": '" + value + "' is not a valid page number.");
            }
            if (page < 0) {
                throw new IllegalArgumentException("Invalid value on line " + lineNumber
                        + ": page numbers cannot be negative (found " + page + ").");
            }
            pages.add(page);
        }

        if (pages.isEmpty()) {
            throw new IllegalArgumentException("The CSV file has a header but no page references.");
        }
        return pages;
    }

    private static int findFirstNonBlank(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).replace("\uFEFF", "").trim().isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}