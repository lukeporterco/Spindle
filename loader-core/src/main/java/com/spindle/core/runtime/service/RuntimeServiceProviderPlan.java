package com.spindle.core.runtime.service;

public record RuntimeServiceProviderPlan(
    String id, String type, String implementation, String state, String reason) {}
