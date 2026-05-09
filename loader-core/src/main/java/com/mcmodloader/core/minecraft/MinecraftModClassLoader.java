package com.mcmodloader.core.minecraft;

import java.net.URL;
import java.net.URLClassLoader;

public final class MinecraftModClassLoader extends URLClassLoader {
    private final String loaderId;
    private final MinecraftClassLoaderPolicy policy;
    private final MinecraftProtectedPackagePolicy protectedPackagePolicy;
    private final MinecraftClassLoadingAudit audit;

    public MinecraftModClassLoader(
        String loaderId,
        URL[] urls,
        ClassLoader parent,
        MinecraftClassLoaderPolicy policy,
        MinecraftProtectedPackagePolicy protectedPackagePolicy,
        MinecraftClassLoadingAudit audit
    ) {
        super(urls, parent);
        this.loaderId = loaderId;
        this.policy = policy;
        this.protectedPackagePolicy = protectedPackagePolicy;
        this.audit = audit;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        audit.recordAttempt(loaderId);
        if (policy.denyLoaderInternals() && protectedPackagePolicy.isDeniedLoadClass(name)) {
            audit.recordDenied(loaderId, name);
            throw new ClassNotFoundException("Denied mod access to protected class " + name);
        }
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null && shouldLoadFromMod(name)) {
            try {
                loadedClass = findClass(name);
                audit.recordDefined(loaderId);
            } catch (ClassNotFoundException ignored) {
                loadedClass = null;
            }
        }
        if (loadedClass == null) {
            loadedClass = getParent().loadClass(name);
        }
        if (resolve) {
            resolveClass(loadedClass);
        }
        return loadedClass;
    }

    private boolean shouldLoadFromMod(String className) {
        if (className.startsWith("java.")) {
            return false;
        }
        if (protectedPackagePolicy.isAllowedApiClass(className)) {
            return false;
        }
        return !protectedPackagePolicy.isDeniedLoadClass(className);
    }
}
