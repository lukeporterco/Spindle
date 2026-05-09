package com.spindle.core.lifecycle;

import com.spindle.api.ModContext;
import com.spindle.api.ModInitializer;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.runtime.CompiledModpackProfile;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LifecycleExecutor {
  private final LifecycleHandlerValidator validator = new LifecycleHandlerValidator();

  public LifecycleExecutionReport execute(
      CompiledModpackProfile profile, ClassLoader classLoader, Map<String, ModContext> contexts)
      throws LoaderException {
    List<LifecycleHandlerValidator.ValidatedLifecycleHandler> validatedHandlers =
        validator.validateAll(toDeclarations(profile.lifecycle().handlers()), classLoader);
    List<LifecycleExecutionReport.HandlerAttempt> attemptedHandlers = new ArrayList<>();
    List<LifecycleExecutionReport.HandlerAttempt> successfulHandlers = new ArrayList<>();
    List<LifecycleExecutionReport.FailedHandler> failedHandlers = new ArrayList<>();

    for (LifecycleHandlerValidator.ValidatedLifecycleHandler validatedHandler : validatedHandlers) {
      LifecycleHandlerDeclaration declaration = validatedHandler.declaration();
      LifecycleExecutionReport.HandlerAttempt attempt =
          new LifecycleExecutionReport.HandlerAttempt(
              declaration.phase(),
              declaration.modId(),
              declaration.className(),
              declaration.methodName());
      attemptedHandlers.add(attempt);
      try {
        if (validatedHandler.isLegacyInitializer()) {
          ModInitializer initializer = validatedHandler.legacyConstructor().newInstance();
          initializer.onInitialize();
        } else {
          ModContext context = contexts.get(declaration.modId());
          if (context == null) {
            throw new LoaderException(
                "Missing planned ModContext for lifecycle handler mod `"
                    + declaration.modId()
                    + "`.");
          }
          validatedHandler.method().invoke(null, context);
        }
        successfulHandlers.add(attempt);
      } catch (InvocationTargetException exception) {
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        failedHandlers.add(
            new LifecycleExecutionReport.FailedHandler(
                declaration.phase(),
                declaration.modId(),
                declaration.className(),
                declaration.methodName(),
                cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage()));
        throw new LoaderException(
            "Lifecycle handler failed for mod `"
                + declaration.modId()
                + "` at "
                + declaration.className()
                + "::"
                + declaration.methodName(),
            cause);
      } catch (InstantiationException | IllegalAccessException exception) {
        failedHandlers.add(
            new LifecycleExecutionReport.FailedHandler(
                declaration.phase(),
                declaration.modId(),
                declaration.className(),
                declaration.methodName(),
                exception.getMessage()));
        throw new LoaderException(
            "Failed to invoke lifecycle handler `"
                + declaration.className()
                + "::"
                + declaration.methodName()
                + "` for mod `"
                + declaration.modId()
                + "`",
            exception);
      }
    }

    return new LifecycleExecutionReport(
        profile.fingerprint(),
        profile.inputFingerprint(),
        profile.runtimePolicyFingerprint(),
        profile.cache().status(),
        profile.cache().reason(),
        profile.lifecycle().phaseOrder(),
        toDeclarations(profile.lifecycle().handlers()),
        attemptedHandlers,
        successfulHandlers,
        failedHandlers,
        contextDirectories(profile));
  }

  public LifecycleExecutionReport plannedOnly(CompiledModpackProfile profile) {
    return new LifecycleExecutionReport(
        profile.fingerprint(),
        profile.inputFingerprint(),
        profile.runtimePolicyFingerprint(),
        profile.cache().status(),
        profile.cache().reason(),
        profile.lifecycle().phaseOrder(),
        toDeclarations(profile.lifecycle().handlers()),
        List.of(),
        List.of(),
        List.of(),
        contextDirectories(profile));
  }

  private List<LifecycleHandlerDeclaration> toDeclarations(
      List<CompiledModpackProfile.LifecycleHandler> handlers) {
    return handlers.stream()
        .map(
            handler ->
                new LifecycleHandlerDeclaration(
                    handler.phase(),
                    handler.modId(),
                    handler.ownerModId(),
                    handler.kind(),
                    handler.className(),
                    handler.methodName(),
                    handler.interfaceName(),
                    handler.jarPath(),
                    handler.jarHash()))
        .toList();
  }

  private List<LifecycleExecutionReport.ContextDirectory> contextDirectories(
      CompiledModpackProfile profile) {
    return profile.contexts().mods().stream()
        .map(
            context ->
                new LifecycleExecutionReport.ContextDirectory(
                    context.modId(),
                    context.configDirectory(),
                    context.dataDirectory(),
                    context.cacheDirectory(),
                    context.generatedDirectory()))
        .toList();
  }
}
