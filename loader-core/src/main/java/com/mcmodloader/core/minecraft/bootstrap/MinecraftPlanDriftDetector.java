package com.mcmodloader.core.minecraft.bootstrap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class MinecraftPlanDriftDetector {
    public List<String> detect(
        JsonObject integrationPlan,
        JsonObject executionPlan
    ) {
        List<String> failures = new ArrayList<>();
        Set<String> integrationAccepted = stringSet(integrationPlan.getAsJsonArray("acceptedMods"), "modId");
        Set<String> integrationRejected = stringSet(integrationPlan.getAsJsonArray("rejectedMods"), "candidate");
        Set<String> executionAccepted = stringSet(executionPlan.getAsJsonArray("acceptedExecutableMods"), "modId");
        Set<String> executionRejected = stringSet(executionPlan.getAsJsonArray("rejectedMods"), "candidate");

        for (String modId : integrationAccepted) {
            if (!executionAccepted.contains(modId) && !executionRejected.contains(modId)) {
                failures.add("accepted mod missing from execution plan: " + modId);
            }
        }
        for (String modId : executionAccepted) {
            if (!integrationAccepted.contains(modId)) {
                failures.add("execution plan includes mod not accepted by integration plan: " + modId);
            }
        }
        for (String modId : integrationRejected) {
            if (executionAccepted.contains(modId)) {
                failures.add("execution plan marked rejected integration mod executable: " + modId);
            }
        }
        return failures;
    }

    private Set<String> stringSet(JsonArray array, String fieldName) {
        Set<String> values = new TreeSet<>();
        if (array == null) {
            return values;
        }
        array.forEach(element -> {
            JsonObject object = element.getAsJsonObject();
            if (object.has(fieldName)) {
                values.add(object.get(fieldName).getAsString());
            }
        });
        return values;
    }
}
