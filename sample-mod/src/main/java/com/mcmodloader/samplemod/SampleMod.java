package com.mcmodloader.samplemod;

import com.mcmodloader.api.ModInitializer;

public final class SampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("Sample mod initialized");
    }
}
