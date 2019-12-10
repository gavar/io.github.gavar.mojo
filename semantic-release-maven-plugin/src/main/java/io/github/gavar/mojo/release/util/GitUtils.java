package io.github.gavar.mojo.release.util;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.List;

public class GitUtils {

    public static RevCommit find(RevWalk walk, List<Ref> since, Ref head) throws IOException {
        walk.reset();
        walk.setRetainBody(false);
        walk.markStart(toRevCommit(walk, head));

        for (Ref ref : since)
            walk.markUninteresting(toRevCommit(walk, ref));

        return walk.next();
    }

    private static RevCommit toRevCommit(RevWalk walk, Ref ref) throws IOException {
        final ObjectId id = ref.getObjectId();
        return walk.parseCommit(id);
    }

    public static String refHash(Ref ref) {
        return refHash(ref, null);
    }

    public static String refHash(Ref ref, Integer length) {
        if (ref != null) {
            final String hash = ref.getObjectId().getName();
            return length != null ? hash.substring(0, length) : hash;
        }
        return null;
    }
}
