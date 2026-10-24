package simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvReaderTest {

    @TempDir
    Path tempDir;

    private Path write(String content) throws IOException {
        return Files.writeString(tempDir.resolve("pages.csv"), content);
    }

    @Test
    void readsValidFileInOrder() throws IOException {
        Path file = write("pagina\n1\n2\n3\n1\n");
        assertEquals(List.of(1, 2, 3, 1), CsvReader.read(file));
    }

    @Test
    void ignoresBlankLinesAndWindowsLineEndings() throws IOException {
        Path file = write("pagina\r\n1\r\n\r\n2\r\n");
        assertEquals(List.of(1, 2), CsvReader.read(file));
    }

    @Test
    void missingFileThrowsIOException() {
        Path missing = tempDir.resolve("does_not_exist.csv");
        assertThrows(IOException.class, () -> CsvReader.read(missing));
    }

    @Test
    void emptyFileIsRejected() throws IOException {
        Path file = write("");
        assertThrows(IllegalArgumentException.class, () -> CsvReader.read(file));
    }

    @Test
    void headerOnlyIsRejected() throws IOException {
        Path file = write("pagina\n");
        assertThrows(IllegalArgumentException.class, () -> CsvReader.read(file));
    }

    @Test
    void wrongHeaderIsRejected() throws IOException {
        Path file = write("page\n1\n");
        assertThrows(IllegalArgumentException.class, () -> CsvReader.read(file));
    }

    @Test
    void nonNumericValueReportsLineNumber() throws IOException {
        Path file = write("pagina\n1\nabc\n3\n");
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> CsvReader.read(file));
        assertTrue(e.getMessage().contains("line 3"), e.getMessage());
    }

    @Test
    void negativeValueIsRejected() throws IOException {
        Path file = write("pagina\n1\n-2\n");
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> CsvReader.read(file));
        assertTrue(e.getMessage().contains("line 3"), e.getMessage());
    }

    @Test
    void rowWithExtraColumnIsRejected() throws IOException {
        Path file = write("pagina\n1,2\n");
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> CsvReader.read(file));
        assertTrue(e.getMessage().contains("line 2"), e.getMessage());
    }
}