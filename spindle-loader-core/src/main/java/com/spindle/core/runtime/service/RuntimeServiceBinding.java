package com.spindle.core.runtime.service;

public record RuntimeServiceBinding(
    String id,
    String consumerModId,
    String providerModId,
    String type,
    String implementation,
    boolean required,
    String state) {}
