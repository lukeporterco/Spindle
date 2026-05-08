package com.mcmodloader.core.entrypoint;

import com.mcmodloader.api.ModInitializer;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.lang.reflect.InvocationTargetException;

public final class EntrypointInvoker {
    public void invoke(ResolvedModSet resolvedModSet, ClassLoader classLoader) throws LoaderException {
        for (ResolvedModSet.ResolvedMod mod : resolvedModSet.mods()) {
            for (String entrypointClassName : mod.entrypoints()) {
                invokeEntrypoint(mod.id(), entrypointClassName, classLoader);
            }
        }
    }

    private void invokeEntrypoint(String modId, String entrypointClassName, ClassLoader classLoader) throws LoaderException {
        try {
            Class<?> entrypointClass = Class.forName(entrypointClassName, true, classLoader);
            if (!ModInitializer.class.isAssignableFrom(entrypointClass)) {
                throw new LoaderException(
                    "Entrypoint " + entrypointClassName + " for mod " + modId + " does not implement com.mcmodloader.api.ModInitializer"
                );
            }

            ModInitializer initializer =
                ModInitializer.class.cast(entrypointClass.getConstructor().newInstance());
            initializer.onInitialize();
        } catch (ClassNotFoundException exception) {
            throw new LoaderException("Entrypoint class not found for mod " + modId + ": " + entrypointClassName, exception);
        } catch (NoSuchMethodException exception) {
            throw new LoaderException(
                "Entrypoint " + entrypointClassName + " for mod " + modId + " must have a public no-arg constructor",
                exception
            );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new LoaderException("Failed to invoke entrypoint " + entrypointClassName + " for mod " + modId, exception);
        }
    }
}
