package com.spindle.core.lifecycle;

import com.spindle.api.ModContext;
import com.spindle.api.ModInitializer;
import com.spindle.core.diagnostics.LoaderException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class LifecycleHandlerValidator {
  public List<ValidatedLifecycleHandler> validateAll(
      List<LifecycleHandlerDeclaration> declarations, ClassLoader classLoader)
      throws LoaderException {
    List<ValidatedLifecycleHandler> validated = new ArrayList<>(declarations.size());
    for (LifecycleHandlerDeclaration declaration : declarations) {
      validated.add(validate(declaration, classLoader));
    }
    return List.copyOf(validated);
  }

  private ValidatedLifecycleHandler validate(
      LifecycleHandlerDeclaration declaration, ClassLoader classLoader) throws LoaderException {
    try {
      Class<?> handlerClass = Class.forName(declaration.className(), true, classLoader);
      if (LifecycleHandlerDeclaration.KIND_LEGACY_MOD_INITIALIZER.equals(declaration.kind())) {
        return validateLegacyInitializer(declaration, handlerClass);
      }
      return validateStaticMethod(declaration, handlerClass);
    } catch (ClassNotFoundException exception) {
      throw new LoaderException(
          "Lifecycle handler class not found for mod `"
              + declaration.modId()
              + "`: "
              + declaration.className()
              + " (phase "
              + declaration.phase()
              + ", expected owner "
              + declaration.ownerModId()
              + ")",
          exception);
    }
  }

  @SuppressWarnings("unchecked")
  private ValidatedLifecycleHandler validateLegacyInitializer(
      LifecycleHandlerDeclaration declaration, Class<?> handlerClass) throws LoaderException {
    if (!ModInitializer.class.isAssignableFrom(handlerClass)) {
      throw new LoaderException(
          "Legacy BOOTSTRAP entrypoint `"
              + declaration.className()
              + "` for mod `"
              + declaration.modId()
              + "` does not implement "
              + ModInitializer.class.getName());
    }
    try {
      Constructor<? extends ModInitializer> constructor =
          (Constructor<? extends ModInitializer>) handlerClass.getConstructor();
      return ValidatedLifecycleHandler.forLegacyInitializer(declaration, constructor);
    } catch (NoSuchMethodException exception) {
      throw new LoaderException(
          "Legacy BOOTSTRAP entrypoint `"
              + declaration.className()
              + "` for mod `"
              + declaration.modId()
              + "` must have a public no-arg constructor",
          exception);
    }
  }

  private ValidatedLifecycleHandler validateStaticMethod(
      LifecycleHandlerDeclaration declaration, Class<?> handlerClass) throws LoaderException {
    try {
      Method method = handlerClass.getDeclaredMethod(declaration.methodName(), ModContext.class);
      int modifiers = method.getModifiers();
      if (!Modifier.isPublic(modifiers)
          || !Modifier.isStatic(modifiers)
          || method.getReturnType() != Void.TYPE) {
        throw invalidSignature(declaration);
      }
      return ValidatedLifecycleHandler.forStaticMethod(declaration, method);
    } catch (NoSuchMethodException exception) {
      throw invalidSignature(declaration);
    }
  }

  private LoaderException invalidSignature(LifecycleHandlerDeclaration declaration) {
    return new LoaderException(
        "Mod `"
            + declaration.modId()
            + "` declares lifecycle handler `"
            + declaration.className()
            + "::"
            + declaration.methodName()
            + "` for phase `"
            + declaration.phase()
            + "`, but Runtime-1 requires `public static void method("
            + ModContext.class.getName()
            + ")`.");
  }

  public record ValidatedLifecycleHandler(
      LifecycleHandlerDeclaration declaration,
      Method method,
      Constructor<? extends ModInitializer> legacyConstructor) {
    static ValidatedLifecycleHandler forStaticMethod(
        LifecycleHandlerDeclaration declaration, Method method) {
      return new ValidatedLifecycleHandler(declaration, method, null);
    }

    static ValidatedLifecycleHandler forLegacyInitializer(
        LifecycleHandlerDeclaration declaration,
        Constructor<? extends ModInitializer> constructor) {
      return new ValidatedLifecycleHandler(declaration, null, constructor);
    }

    public boolean isLegacyInitializer() {
      return legacyConstructor != null;
    }
  }
}
