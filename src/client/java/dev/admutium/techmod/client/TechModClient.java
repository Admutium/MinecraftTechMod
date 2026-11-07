package dev.admutium.techmod.client;

import dev.admutium.techmod.TechMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;

public final class TechModClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        CustomUnbakedBlockStateModel.register(TechMod.id("cable"), CableModel.Unbaked.CODEC);
    }
}
