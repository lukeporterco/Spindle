package com.spindle.core.minecraft.hook.install;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public final class MinecraftHookRuntimeBridge {
  public MinecraftHookInstallationResult installAndInvoke(
      MinecraftHookInstallationPlan plan,
      MinecraftModExecutionPlan executionPlan,
      ClassLoader runtimeClassLoader)
      throws LoaderException, HookInstallationException {
    Validation validation = validate(plan, executionPlan);
    if (!validation.failures().isEmpty()) {
      throw new HookInstallationException(
          failureResult(plan, executionPlan, validation.failures()));
    }

    MinecraftInstalledHookInvocation installedHook =
        new MinecraftInstalledHookInvocation(
            MinecraftHookInstallationPlanner.SUPPORTED_HOOK_ID,
            MinecraftHookInstallationPlanner.SUPPORTED_SOURCE_CONTRACT_ID,
            MinecraftHookInstallationPlanner.SUPPORTED_OWNER_INTERNAL_NAME,
            MinecraftHookInstallationPlanner.SUPPORTED_MEMBER_NAME,
            MinecraftHookInstallationPlanner.SUPPORTED_DESCRIPTOR,
            MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER,
            true,
            false,
            null);
    try {
      Class<?> mainClass =
          Class.forName(executionPlan.minecraftMainClass(), true, runtimeClassLoader);
      Method mainMethod = mainClass.getMethod("main", String[].class);
      int modifiers = mainMethod.getModifiers();
      if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
        throw new HookInstallationException(
            failureResult(
                plan,
                executionPlan,
                List.of("Minecraft main method is not public static."),
                true,
                false,
                1,
                0,
                1,
                List.of(
                    new MinecraftInstalledHookInvocation(
                        installedHook.id(),
                        installedHook.sourceContractId(),
                        installedHook.ownerInternalName(),
                        installedHook.memberName(),
                        installedHook.descriptor(),
                        installedHook.mode(),
                        true,
                        false,
                        "Minecraft main method is not public static."))));
      }
      mainMethod.invoke(null, (Object) executionPlan.minecraftMainArgs().toArray(String[]::new));
      return new MinecraftHookInstallationResult(
          1,
          MinecraftHookInstallationPlanner.MILESTONE_NAME,
          "minecraft",
          executionPlan.resolvedMinecraftVersion(),
          executionPlan.side(),
          executionPlan.minecraftMainClass(),
          MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER,
          true,
          true,
          true,
          true,
          1,
          1,
          0,
          MinecraftHookInstallationStatus.SUCCESS,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          List.of(
              new MinecraftInstalledHookInvocation(
                  installedHook.id(),
                  installedHook.sourceContractId(),
                  installedHook.ownerInternalName(),
                  installedHook.memberName(),
                  installedHook.descriptor(),
                  installedHook.mode(),
                  true,
                  true,
                  null)),
          null,
          null,
          List.of());
    } catch (InvocationTargetException exception) {
      throw new HookInstallationException(
          failureResult(
              plan,
              executionPlan,
              List.of(
                  "Minecraft main invocation failed: "
                      + messageOrType(exception.getTargetException())),
              true,
              true,
              1,
              1,
              1,
              List.of(
                  new MinecraftInstalledHookInvocation(
                      installedHook.id(),
                      installedHook.sourceContractId(),
                      installedHook.ownerInternalName(),
                      installedHook.memberName(),
                      installedHook.descriptor(),
                      installedHook.mode(),
                      true,
                      true,
                      messageOrType(exception.getTargetException())))),
          exception.getTargetException());
    } catch (ReflectiveOperationException exception) {
      throw new HookInstallationException(
          failureResult(
              plan,
              executionPlan,
              List.of("Failed to resolve Minecraft main: " + messageOrType(exception)),
              true,
              false,
              1,
              0,
              1,
              List.of(
                  new MinecraftInstalledHookInvocation(
                      installedHook.id(),
                      installedHook.sourceContractId(),
                      installedHook.ownerInternalName(),
                      installedHook.memberName(),
                      installedHook.descriptor(),
                      installedHook.mode(),
                      true,
                      false,
                      messageOrType(exception)))),
          exception);
    }
  }

  private Validation validate(
      MinecraftHookInstallationPlan plan, MinecraftModExecutionPlan executionPlan) {
    List<String> failures = new java.util.ArrayList<>();
    if (plan == null) {
      failures.add("Minecraft hook installation plan is missing.");
      return new Validation(List.copyOf(failures));
    }
    if (plan.schema() != 1) {
      failures.add("Minecraft hook installation plan schema mismatch.");
    }
    if (!MinecraftHookInstallationPlanner.MILESTONE_NAME.equals(plan.milestoneName())) {
      failures.add("Minecraft hook installation milestone mismatch.");
    }
    if (!plan.gatePassed() || !plan.installationPlanned()) {
      failures.add("Minecraft hook installation plan gate did not pass.");
    }
    if (plan.installationMode() != MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER) {
      failures.add("Minecraft hook installation mode mismatch.");
    }
    if (plan.plannedHooks().size() != 1) {
      failures.add("Minecraft hook installation plan must contain exactly one planned hook.");
      return new Validation(List.copyOf(failures));
    }
    MinecraftPlannedHookInstallation hook = plan.plannedHooks().getFirst();
    if (!MinecraftHookInstallationPlanner.SUPPORTED_HOOK_ID.equals(hook.id())) {
      failures.add("Minecraft hook id mismatch.");
    }
    if (!MinecraftHookInstallationPlanner.SUPPORTED_SOURCE_CONTRACT_ID.equals(
        hook.sourceContractId())) {
      failures.add("Minecraft hook source contract id mismatch.");
    }
    if (!MinecraftHookInstallationPlanner.SUPPORTED_CATALOG_ID.equals(hook.catalogId())) {
      failures.add("Minecraft hook catalog id mismatch.");
    }
    if (!MinecraftHookInstallationPlanner.SUPPORTED_KIND.equals(hook.kind())) {
      failures.add("Minecraft hook kind mismatch.");
    }
    if (!MinecraftHookInstallationPlanner.SUPPORTED_OWNER_INTERNAL_NAME.equals(
        hook.ownerInternalName())) {
      failures.add("Minecraft hook owner mismatch.");
    }
    if (!MinecraftHookInstallationPlanner.SUPPORTED_MEMBER_NAME.equals(hook.memberName())) {
      failures.add("Minecraft hook member mismatch.");
    }
    if (!MinecraftHookInstallationPlanner.SUPPORTED_DESCRIPTOR.equals(hook.descriptor())) {
      failures.add("Minecraft hook descriptor mismatch.");
    }
    if (!hook.required()) {
      failures.add("Minecraft hook requirement mismatch.");
    }
    if (hook.mode() != MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER) {
      failures.add("Minecraft hook mode mismatch.");
    }
    if (executionPlan == null) {
      failures.add("Minecraft execution plan is missing.");
      return new Validation(List.copyOf(failures));
    }
    if (!executionPlan.minecraftMainClass().equals(plan.minecraftMainClass())) {
      failures.add("Minecraft hook installation plan main class mismatch.");
    }
    return new Validation(List.copyOf(failures));
  }

  private MinecraftHookInstallationResult failureResult(
      MinecraftHookInstallationPlan plan,
      MinecraftModExecutionPlan executionPlan,
      List<String> failures) {
    return failureResult(
        plan, executionPlan, failures, false, false, 0, 0, failures.isEmpty() ? 0 : 1, List.of());
  }

  private MinecraftHookInstallationResult failureResult(
      MinecraftHookInstallationPlan plan,
      MinecraftModExecutionPlan executionPlan,
      List<String> failures,
      boolean minecraftMainClassLoaded,
      boolean minecraftMainInvoked,
      int installedHookCount,
      int invokedHookCount,
      int failedHookCount,
      List<MinecraftInstalledHookInvocation> installedHooks) {
    return new MinecraftHookInstallationResult(
        1,
        MinecraftHookInstallationPlanner.MILESTONE_NAME,
        "minecraft",
        executionPlan == null
            ? (plan == null ? null : plan.minecraftVersion())
            : executionPlan.resolvedMinecraftVersion(),
        executionPlan == null ? (plan == null ? null : plan.side()) : executionPlan.side(),
        executionPlan == null
            ? (plan == null ? null : plan.minecraftMainClass())
            : executionPlan.minecraftMainClass(),
        MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER,
        installedHookCount > 0,
        invokedHookCount > 0,
        minecraftMainClassLoaded,
        minecraftMainInvoked,
        installedHookCount,
        invokedHookCount,
        failedHookCount,
        MinecraftHookInstallationStatus.FAILED,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        installedHooks,
        "hook-installation-failure",
        failures.isEmpty() ? null : failures.getFirst(),
        failures);
  }

  private String messageOrType(Throwable throwable) {
    return throwable.getMessage() == null || throwable.getMessage().isBlank()
        ? throwable.getClass().getName()
        : throwable.getMessage();
  }

  private record Validation(List<String> failures) {}

  public static final class HookInstallationException extends Exception {
    private final MinecraftHookInstallationResult result;

    public HookInstallationException(MinecraftHookInstallationResult result) {
      super(result.failureMessage());
      this.result = result;
    }

    public HookInstallationException(MinecraftHookInstallationResult result, Throwable cause) {
      super(result.failureMessage(), cause);
      this.result = result;
    }

    public MinecraftHookInstallationResult result() {
      return result;
    }
  }
}
