package com.mcmodloader.core.game;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class SampleGameProvider implements GameProvider {
    private final String version;

    public SampleGameProvider(String version) {
        this.version = version;
    }

    @Override
    public String id() {
        return "sample";
    }

    @Override
    public String displayName() {
        return "Sample Game";
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public void validate(LaunchContext context) throws LoaderException {
        if (context.gameMainClass() == null || context.gameMainClass().isBlank()) {
            throw new LoaderException("Game main class is required for sample game provider");
        }
    }

    @Override
    public void launch(LaunchContext context, ClassLoader classLoader) throws LoaderException {
        String gameMainClass = context.gameMainClass();
        try {
            Class<?> gameClass = Class.forName(gameMainClass, true, classLoader);
            Method mainMethod = gameClass.getMethod("main", String[].class);
            if (!Modifier.isStatic(mainMethod.getModifiers()) || mainMethod.getReturnType() != Void.TYPE) {
                throw new LoaderException("Game main method must be public static void main(String[] args): " + gameMainClass);
            }
            mainMethod.invoke(null, (Object) new String[0]);
        } catch (ClassNotFoundException exception) {
            throw new LoaderException("Game main class not found: " + gameMainClass, exception);
        } catch (NoSuchMethodException exception) {
            throw new LoaderException("Game main method not found: " + gameMainClass, exception);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new LoaderException("Failed to launch game main class: " + gameMainClass, exception);
        }
    }
}
