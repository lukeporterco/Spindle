package com.spindle.core.minecraft.flow;

import com.spindle.core.minecraft.MinecraftBoundarySeverity;
import com.spindle.core.minecraft.MinecraftBoundaryViolation;
import com.spindle.core.minecraft.MinecraftModIntegrationPlan;
import com.spindle.core.minecraft.MinecraftModRejection;
import com.spindle.core.minecraft.MinecraftPreflightResult;
import com.spindle.core.minecraft.MinecraftRuntimeBoundary;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftPreflightFlow {
  public MinecraftPreflightResult buildResult(
      String minecraftVersion,
      List<String> reportsWritten,
      MinecraftRuntimeBoundary runtimeBoundary,
      MinecraftModIntegrationPlan integrationPlan) {
    List<MinecraftBoundaryViolation> issues = new ArrayList<>(runtimeBoundary.violations());
    if (integrationPlan != null) {
      issues.addAll(integrationPlan.issues());
    }
    List<MinecraftModRejection> rejectedMods =
        integrationPlan == null ? List.of() : integrationPlan.rejectedMods();
    int warningCount =
        (int)
            issues.stream()
                .filter(issue -> issue.severity() == MinecraftBoundarySeverity.WARNING)
                .count();
    int fatalCount =
        (int)
            issues.stream()
                .filter(
                    issue ->
                        issue.fatalNow() || issue.severity() == MinecraftBoundarySeverity.FATAL)
                .count();
    List<String> failureReasons = new ArrayList<>();
    for (MinecraftBoundaryViolation issue : issues) {
      if (issue.fatalNow() || issue.severity() == MinecraftBoundarySeverity.FATAL) {
        failureReasons.add(issue.type() + ": " + issue.reason());
      }
    }
    for (MinecraftModRejection rejection : rejectedMods) {
      failureReasons.add("rejected-mod " + rejection.candidate() + ": " + rejection.reason());
    }
    boolean preflightSucceeded = failureReasons.isEmpty();
    return new MinecraftPreflightResult(
        1,
        "Mega-Milestone 7",
        minecraftVersion,
        true,
        runtimeBoundary != null,
        integrationPlan != null,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        List.copyOf(reportsWritten),
        rejectedMods,
        issues,
        integrationPlan == null ? 0 : integrationPlan.acceptedMods().size(),
        rejectedMods.size(),
        warningCount,
        fatalCount,
        failureReasons,
        preflightSucceeded);
  }
}
