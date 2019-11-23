package dev.gavar.mojo.util;

import java.nio.file.Path;

import static com.google.common.collect.Iterables.getFirst;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class PathUtils {
    /**
     * Calculate path to a directory that is common root for every path in a list.
     * @param paths     - list of paths to calculate common root directory for.
     * @param normalize - whether paths should be normalized before processing.
     * @return common root directory path if such exists; null if paths can't have a common root.
     */
    public static Path findCommonPath(Iterable<Path> paths, boolean normalize) {
        if (normalize)
            paths = stream(paths.spliterator(), false)
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .collect(toList());

        Path common = getFirst(paths, null);
        while (common != null && !isCommonPath(common, paths))
            common = common.getParent();

        return common;
    }

    /**
     * Check whether provided path is a common directory for every path in a list.
     * @param common - candidate to check for being a common path.
     * @param paths  - list of path to check against.
     * @return true when provided path is parent for all of the paths in a list; false otherwise.
     */
    public static boolean isCommonPath(Path common, Iterable<Path> paths) {
        for (Path path : paths)
            if (!path.startsWith(common))
                return false;

        return true;
    }
}
