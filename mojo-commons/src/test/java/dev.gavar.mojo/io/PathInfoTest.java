package dev.gavar.mojo.io;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathInfoTest {

    @ParameterizedTest
    @CsvFileSource(resources = "path-info-from-string.csv", numLinesToSkip = 1)
    public void fromString(String path, String directory, String fileName, String stem, String extension) {
        PathInfo actual = new PathInfo(path);
        assertEquals(directory, actual.getDirectory(), "directory");
        assertEquals(fileName, actual.getFileName(), "fileName");
        assertEquals(stem, actual.getStem(), "stem");
        assertEquals(extension, actual.getExtension(), "extension");
    }
}
