package com.mcmodloader.core.minecraft;

import java.net.URL;
import java.net.URLClassLoader;

public final class MinecraftRuntimeClassLoader extends URLClassLoader {
    private final String loaderId;
    private final MinecraftClassLoadingAudit audit;

    public MinecraftRuntimeClassLoader(String loaderId, URL[] urls, ClassLoader parent, MinecraftClassLoadingAudit audit) {
        super(urls, parent);
        this.loaderId = loaderId;
        this.audit = audit;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        audit.recordAttempt(loaderId);
        Class<?> loadedClass = super.loadClass(name, resolve);
        if (loadedClass.getClassLoader() == this) {
            audit.recordDefined(loaderId);
        }
        return loadedClass;
    }
}
