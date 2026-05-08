package com.mcmodloader.core.classpath;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.nio.file.Path;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public final class ModClassLoader extends URLClassLoader {
    private ModClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public static ModClassLoader create(RuntimeClasspathPlan classpathPlan, ClassLoader parent) throws LoaderException {
        try {
            List<Path> modJars = classpathPlan.modJars();
            URL[] urls = new URL[modJars.size()];
            for (int index = 0; index < modJars.size(); index++) {
                urls[index] = modJars.get(index).toUri().toURL();
            }
            return new ModClassLoader(urls, parent);
        } catch (MalformedURLException exception) {
            throw new LoaderException("Failed to build mod class loader", exception);
        }
    }
}
