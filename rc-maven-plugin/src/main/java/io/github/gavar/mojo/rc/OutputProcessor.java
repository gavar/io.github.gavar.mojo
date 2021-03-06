package io.github.gavar.mojo.rc;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkState;
import static io.github.gavar.mojo.util.MojoUtils.evaluate;

public class OutputProcessor {

    private MavenSession session;
    public void setSession(MavenSession session) { this.session = session; }

    private ExpressionEvaluator evaluator;
    public void setEvaluator(ExpressionEvaluator evaluator) { this.evaluator = evaluator; }

    public void process(Properties source, OutputFiles[] outputs, OutputInjection[] injections) throws IOException, MojoExecutionException {
        checkState(session != null, "session is not set");
        checkState(evaluator != null, "evaluator is not set");

        // sort source
        source = sorted(source);

        // process outputs
        for (OutputFiles output : outputs) {
            Properties properties = sorted(output.filter(source));
            for (File file : output.getFiles())
                write(properties, evaluate(evaluator, file));
        }

        // process injections
        for (OutputInjection injection : injections) {
            Properties properties = sorted(injection.filter(source));
            for (Properties bean : injection.getBeans())
                write(properties, bean, injection.shouldRewrite(bean));
        }
    }

    private static Properties sorted(Properties properties) {
        return properties instanceof SortedProperties
            ? properties
            : new SortedProperties(properties);
    }

    private void write(Properties source, File file) throws IOException {
        file.getParentFile().mkdirs();
        try (OutputStream stream = new FileOutputStream(file)) {
            source.store(stream, "generated by io.github.gavar.mojo:rc-maven-plugin");
        }
    }

    private void write(Properties source, Properties target, boolean rewrite) {
        if (rewrite) {
            target.putAll(source);
        } else {
            for (Object key : source.keySet())
                target.putIfAbsent(key, source.get(key));
        }
    }

    private static class SortedProperties extends Properties {

        static int compare(Map.Entry<Object, Object> a, Map.Entry<Object, Object> b) {
            return compare(a.getKey(), b.getKey());
        }

        static int compare(Object a, Object b) {
            return ((String) a).compareTo((String) b);
        }

        public SortedProperties(Properties properties) {
            super(properties.size());
            this.putAll(properties);
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            TreeSet<Map.Entry<Object, Object>> set = new TreeSet<>(SortedProperties::compare);
            set.addAll(super.entrySet());
            return set;
        }
    }
}
