package io.github.gavar.mojo.rc;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import static io.github.gavar.mojo.util.GenericUtils.arrify;

/** Write properties to a file. */
public class OutputFiles extends AbstractOutput {

    public static OutputFiles files(Path... paths) {
        return new OutputFiles(Arrays.stream(paths).map(Path::toFile).toArray(File[]::new));
    }

    public static OutputFiles files(String... paths) {
        return new OutputFiles(Arrays.stream(paths).map(File::new).toArray(File[]::new));
    }

    public OutputFiles() {}
    public OutputFiles(File... files) {
        this.files = files;
    }

    /** List of files where to write property values. */
    @Parameter
    private File[] files = {};
    public File[] getFiles() { return files; }
    public void setFile(File file) { this.files = arrify(file, File[]::new); }
    public void setFiles(File[] files) { this.files = files; }
}
