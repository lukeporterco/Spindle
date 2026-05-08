package com.mcmodloader.core.classpath;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public final class ModClassLoader extends URLClassLoader {
    private ModClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public static ModClassLoader create(ResolvedModSet resolvedModSet, ClassLoader parent) throws LoaderException {
        try {
            URL[] urls = new URL[resolvedModSet.mods().size()];
            for (int index = 0; index < resolvedModSet.mods().size(); index++) {
                urls[index] = resolvedModSet.mods().get(index).jarPath().toUri().toURL();
            }
            return new ModClassLoader(urls, parent);
        } catch (MalformedURLException exception) {
            throw new LoaderException("Failed to build mod class loader", exception);
        }
    }
}
