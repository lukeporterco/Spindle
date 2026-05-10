package com.spindle.core.runtime.service;

public record RuntimeServiceConsumerPlan(
    String id, String type, boolean required, String state, String providerModId, String reason) {}
