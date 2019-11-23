package dev.gavar.mojo.util;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class MojoUtils {

    /**
     * Replaces all occurrences of <code>@{value}</code> with <code>${</code>
     * Workaround to escape '$' since '\$' doesn't work.
     */
    public static String unescape(String value) {
        return value.contains("@{")
                ? value.replace("@{", "${")
                : value;
    }

    /**
     * Evaluates expression using provided evaluator.
     * Applies {@link #unescape} before evaluation.
     * @param evaluator  - expression evaluator to use.
     * @param expression - expression to evaluate
     * @throws MojoExecutionException when evaluation fails.
     */
    public static String evaluate(ExpressionEvaluator evaluator, String expression) throws MojoExecutionException {
        try {
            expression = unescape(expression);
            return (String) evaluator.evaluate(expression);
        } catch (ExpressionEvaluationException e) {
            throw new MojoExecutionException("unable to evaluate expression: " + expression, e);
        }
    }

    /**
     * Evaluate path of the file using provided evaluator.
     * Applies {@link #unescape} before evaluation.
     * @param evaluator - expression evaluator to use.
     * @param file      - file containing path to evaluate.
     * @throws MojoExecutionException when evaluation fails.
     */
    public static File evaluate(ExpressionEvaluator evaluator, File file) throws MojoExecutionException {
        String path = evaluate(evaluator, file.getPath());
        if (!file.getPath().equals(path))
            file = new File(path);

        return file;
    }

    /**
     * Resolve directory of the root project withing the maven multi-module project.
     * @param projects - list of project participating in compilation.
     * @return common root path for all the projects including parent projects.
     */
    public static Path findProjectsRoot(Collection<MavenProject> projects) {
        // collect project paths
        List<Path> paths = new ArrayList<>();
        Deque<MavenProject> stack = new ArrayDeque<>(projects);
        while (!stack.isEmpty()) {
            MavenProject project = stack.pop();
            File basedir = project.getBasedir();
            // use only local projects
            if (basedir != null) {
                paths.add(basedir.toPath().toAbsolutePath().normalize());
                // schedule processing parent project
                MavenProject parent = project.getParent();
                if (parent != null) stack.push(parent);
            }
        }

        return PathUtils.findCommonPath(paths, false);
    }
}
