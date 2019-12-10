package io.github.gavar.mojo.release.util;

import org.apache.maven.shared.release.ReleaseManager;

import java.lang.reflect.Field;
import java.util.List;

public class ReflectionUtils {

    @SuppressWarnings("unchecked")
    public static List<String> phasesOf(ReleaseManager releaseManager, String name) throws ReflectiveOperationException {
        final Class type = releaseManager.getClass();
        final Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        final Object value = field.get(releaseManager);
        return (List<String>) value;
    }
}
