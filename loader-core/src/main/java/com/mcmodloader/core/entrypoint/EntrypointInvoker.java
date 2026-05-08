package com.mcmodloader.core.entrypoint;

import com.mcmodloader.api.ModInitializer;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.ownership.ClassOwnershipIndex;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class EntrypointInvoker {
    public List<EntrypointInvocation> invoke(
        ResolvedModSet resolvedModSet,
        ClassLoader classLoader,
        ClassOwnershipIndex ownershipIndex
    ) throws LoaderException {
        List<EntrypointInvocation> invocations = new ArrayList<>();
        for (ResolvedModSet.ResolvedMod mod : resolvedModSet.mods()) {
            for (String entrypointClassName : mod.entrypoints()) {
                invocations.add(invokeEntrypoint(mod.id(), entrypointClassName, classLoader, ownershipIndex));
            }
        }
        return List.copyOf(invocations);
    }

    private EntrypointInvocation invokeEntrypoint(
        String modId,
        String entrypointClassName,
        ClassLoader classLoader,
        ClassOwnershipIndex ownershipIndex
    ) throws LoaderException {
        String expectedOwnerModId = ownershipIndex.ownerOfClass(entrypointClassName).orElse(modId);
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
            return new EntrypointInvocation(modId, expectedOwnerModId, entrypointClassName);
        } catch (ClassNotFoundException exception) {
            throw new LoaderException(
                "Entrypoint class not found for mod " +
                modId +
                ": " +
                entrypointClassName +
                " (expected owner " +
                expectedOwnerModId +
                ")",
                exception
            );
        } catch (NoSuchMethodException exception) {
            throw new LoaderException(
                "Entrypoint " + entrypointClassName + " for mod " + modId + " must have a public no-arg constructor",
                exception
            );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new LoaderException("Failed to invoke entrypoint " + entrypointClassName + " for mod " + modId, exception);
        }
    }

    public record EntrypointInvocation(String declaredModId, String ownerModId, String entrypointClassName) {
    }
}
