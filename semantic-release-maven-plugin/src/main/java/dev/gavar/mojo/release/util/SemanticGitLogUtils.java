package dev.gavar.mojo.release.util;

import org.eclipse.jgit.revwalk.filter.MessageRevFilter;
import org.eclipse.jgit.revwalk.filter.OrRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;

public class SemanticGitLogUtils {
    public static final RevFilter CHORE_MESSAGE = MessageRevFilter.create("^chore(.*):");
    public static final RevFilter TEST_MESSAGE = MessageRevFilter.create("^test(.*):");
    public static final RevFilter AFFECTS_CODE = OrRevFilter.create(
        CHORE_MESSAGE,
        TEST_MESSAGE
    ).negate();
}
