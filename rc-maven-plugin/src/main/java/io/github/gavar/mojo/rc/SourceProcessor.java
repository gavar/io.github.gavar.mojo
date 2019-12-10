package io.github.gavar.mojo.rc;

import io.github.gavar.mojo.io.ConfigurableConfigurationLoader;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.maven.plugin.logging.Log;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.github.gavar.mojo.util.GenericUtils.valueOrDefault;

public class SourceProcessor {

    private Log log;
    public void setLog(Log log) { this.log = log; }

    private Path base;
    public void setBase(Path base) { this.base = base; }

    private Path root;
    public void setRoot(Path root) { this.root = root; }

    private String[] variants;
    public void setVariants(String[] variants) { this.variants = variants; }

    private String[] extensions;
    public void setExtensions(String[] extensions) { this.extensions = extensions; }

    private Boolean modular;
    public void setModular(Boolean modular) { this.modular = modular; }

    public Properties process(PropertyFileSet[] sources) throws ConfigurationException {
        checkState(log != null, "log is not set");
        checkState(root != null, "root is not set");
        checkState(base != null, "base is not set");
        checkState(modular != null, "modular is not set");
        checkState(variants != null, "extensions are not set");
        checkState(extensions != null, "extensions are not set");

        // configure
        ConfigurableConfigurationLoader loader = new ConfigurableConfigurationLoader();
        PropertiesVisitor visitor = new PropertiesVisitor(loader, log);

        List<Entry> items = new ArrayList<>();
        for (PropertyFileSet source : sources) {
            // collect items to visit
            items.clear();
            items = collect(items, source);
            visitEach(base, visitor, items);
            visitModularOnly(root, base.getParent(), visitor, items);
        }

        // merge properties
        return visitor.toProperties();
    }

    private List<Entry> collect(List<Entry> items, PropertyFileSet source) {
        return collect(items, source,
            valueOrDefault(source.variants, this.variants),
            valueOrDefault(source.extensions, this.extensions),
            valueOrDefault(source.modular, this.modular)
        );
    }

    private List<Entry> collect(List<Entry> items, PropertyFileSet source,
                                String[] variants, String[] extensions, Boolean modular) {
        for (PropertyFile file : source.files)
            items = collect(items, file,
                valueOrDefault(file.variants, variants),
                valueOrDefault(file.extensions, extensions),
                valueOrDefault(file.modular, modular)
            );
        return items;
    }

    private List<Entry> collect(List<Entry> items, PropertyFile file,
                                String[] variants, String[] extensions, Boolean modular) {
        Entry item = new Entry();

        item.file = file;
        item.variants = file.shouldTryVariants() ? variants : null;
        item.extensions = file.shouldTryExtensions() ? extensions : null;
        item.modular = file.isModular(modular);
        item.warn = file.getUrl() != null || file.isAbsolute();

        items.add(item);
        return items;
    }

    private void visitEach(Path base, PropertiesVisitor visitor, List<Entry> items) throws ConfigurationException {
        for (Entry item : items)
            visit(item, base, visitor);
    }

    private void visitModularOnly(Path root, Path base, PropertiesVisitor visitor, List<Entry> items) throws ConfigurationException {
        checkArgument(Files.isDirectory(root), "root should be a directory");
        checkArgument(Files.isDirectory(base), "base should be a directory");

        // visit every path until root
        for (; base.startsWith(root); base = base.getParent())
            for (Entry item : items) {
                if (item.modular) {
                    String rel = item.file.getPath();
                    Path absolute = base.resolve(rel);
                    visitor.visit(absolute, item.variants, item.extensions);
                }
            }
    }

    private void visit(Entry item, Path base, PropertiesVisitor visitor) throws ConfigurationException {
        final URL url = item.file.getUrl();
        final String path = item.file.getPath();
        final boolean absolute = item.file.isAbsolute();

        if (url != null) visit(url, visitor, item);
        else if (absolute) visit(path, visitor, item);
        else visit(base.resolve(path), visitor, item);
    }

    private void visit(String path, PropertiesVisitor visitor, Entry item) throws ConfigurationException {
        boolean success = visitor.visit(path, item.variants, item.extensions) > 0;
        track(path, success, item);
    }

    private void visit(Path path, PropertiesVisitor visitor, Entry item) throws ConfigurationException {
        boolean success = visitor.visit(path, item.variants, item.extensions) > 0;
        track(path, success, item);
    }

    private void visit(URL url, PropertiesVisitor visitor, Entry item) throws ConfigurationException {
        boolean success = visitor.visit(url, item.variants, item.extensions) > 0;
        track(url, success, item);
    }

    private void track(Object path, boolean ok, Entry item) {
        if (!ok && item.warn)
            log.info("[-] " + path);
    }

    private static class Entry {
        public PropertyFile file;
        public String[] variants;
        public String[] extensions;
        public boolean modular;
        public boolean warn;
    }
}
