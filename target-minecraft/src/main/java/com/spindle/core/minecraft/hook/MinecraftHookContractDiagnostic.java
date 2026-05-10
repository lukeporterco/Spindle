package com.spindle.core.minecraft.hook;

public record MinecraftHookContractDiagnostic(
    String id,
    MinecraftHookDiagnosticSeverity severity,
    String status,
    String contractId,
    String code,
    String message,
    String ownerInternalName,
    String memberName,
    String descriptor) {}
