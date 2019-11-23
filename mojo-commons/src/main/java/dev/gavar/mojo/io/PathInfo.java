package dev.gavar.mojo.io;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Object whose properties represent significant elements of the path.
 * @see <a href="https://nodejs.org/api/path.html#path_path_parse_path">Nodejs Path</a>
 * @see <a href="https://en.cppreference.com/w/cpp/experimental/fs/path#Decomposition">C++ Path Decomposition</a>
 */
public class PathInfo {
    public static final char DOT = '.';
    public static final char BACK_SLASH = '\\';
    public static final char FRONT_SLASH = '/';
    public static final String SEPARATORS = "/\\";
    public static final String FRONT_SLASH_STRING = "/";

    public static boolean hasExtension(String path) {
        // if there is dot as after first character after last separator
        int s = lastIndexOfSeparator(path);
        return path.indexOf(DOT, s + 1) > 0;
    }

    private Path path;
    private String pathName;

    /**
     * Full directory path such as '/home/user/dir'.
     * @see <a href="https://en.wikipedia.org/wiki/dirname">UNIX dirname</a>
     */
    private String directory = "";

    /**
     * File name including extension (if any) such as 'file.txt'.
     * @see <a href="https://en.wikipedia.org/wiki/basename">UNIX basename</a>
     */
    private String fileName = "";

    /**
     * File name without extension (if any) such as 'file'.
     * @see <a href="https://en.cppreference.com/w/cpp/experimental/fs/path/stem">Path Stem</a>
     */
    private String stem = "";

    /** The file extension (if any) with leading dot such as '.txt'. */
    private String extension = "";

    public PathInfo() { }
    public PathInfo(String path) {
        this.setPath(path);
    }
    public PathInfo(Path path) {
        this.setPath(path);
    }
    public PathInfo(String directory, String fileName, String stem, String extension) {
        if (directory != null) this.directory = directory;
        if (fileName != null) this.fileName = fileName;
        if (stem != null) this.stem = stem;
        if (extension != null) this.extension = extension;
    }
    public PathInfo(PathInfo source) {
        this.directory = source.directory;
        this.fileName = source.fileName;
        this.stem = source.stem;
        this.extension = source.extension;
    }

    public void setPath(Path path) {
        this.setPath(path.toString());
        this.path = path;
    }

    public void setPath(String path) {
        invalidatePath();
        this.pathName = path;
        this.directory = this.fileName = this.stem = this.extension = "";
        int s = lastIndexOfSeparator(path);
        if (s >= 0) setDirectory(path.substring(0, s));
        setFileName(path.substring(s + 1));
    }

    public Path getPath() {
        if (this.path == null)
            this.path = Path.of(getPathName());
        return this.path;
    }

    public String getPathName() {
        if (this.pathName == null)
            this.pathName = String.join(FRONT_SLASH_STRING, this.directory, this.fileName);
        return this.pathName;
    }

    public String getDirectory() { return directory; }
    public void setDirectory(Path directory) { setDirectory(slash(directory)); }
    public void setDirectory(String directory) {
        invalidatePath();
        this.directory = directory;
    }

    public String getFileName() { return fileName; }
    public void setFileName(Path fileName) { setFileName(fileName.toString()); }
    public void setFileName(String fileName) {
        checkArgument(!containsAny(fileName, SEPARATORS), "fileName should not contain separator");

        invalidatePath();
        this.stem = fileName;
        this.extension = "";
        this.fileName = fileName;

        int dot = fileName != null ? fileName.lastIndexOf(DOT) : -1;
        if (dot > 0) {
            stem = fileName.substring(0, dot);
            extension = fileName.substring(dot);
        }
    }

    public String getStem() { return stem; }
    public void setStem(String stem) {
        checkArgument(!containsAny(stem, SEPARATORS), "stem should not contain separator");
        invalidatePath();
        this.stem = stem;
        this.fileName = stem + extension;
    }

    public String getExtension() {
        return extension;
    }
    public void setExtension(String extension) {
        checkArgument(isEmptyOrStartsWith(extension, DOT), "extension should start with dot ('.')");
        invalidatePath();
        this.extension = extension;
        this.fileName = stem + extension;
    }

    private void invalidatePath() {
        this.path = null;
        this.pathName = null;
    }

    public boolean hasExtension() {
        return isNotEmpty(extension);
    }

    public void copy(PathInfo other) {
        this.path = other.path;
        this.pathName = other.pathName;
        this.directory = other.directory;
        this.fileName = other.fileName;
        this.stem = other.stem;
        this.extension = other.extension;
    }

    private static boolean isEmptyOrStartsWith(String value, char c) {
        return isEmpty(value) || value.charAt(0) == c;
    }

    private static boolean containsAny(String value, CharSequence chars) {
        if (isNotEmpty(value))
            for (int i = 0; i < chars.length(); i++)
                if (value.indexOf(chars.charAt(i)) >= 0)
                    return true;
        return false;
    }

    private static int lastIndexOfSeparator(String path) {
        int size = path != null ? path.length() : 0;

        // omit trailing separator
        if (size > 0)
            switch (path.charAt(size - 1)) {
                case BACK_SLASH:
                case FRONT_SLASH:
                    return size - 1;
            }

        if (size > 0) {
            int s1 = path.lastIndexOf(FRONT_SLASH, size);
            int s2 = path.lastIndexOf(BACK_SLASH, size);
            return Math.max(s1, s2);
        }

        return -1;
    }

    private static String slash(Path path) {
        return path != null
                ? path.toString().replace(BACK_SLASH, FRONT_SLASH)
                : null;
    }
}
