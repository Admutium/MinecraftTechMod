package dev.admutium.techmod.client;

import dev.admutium.techmod.TechMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;

public final class TechModClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        for(var menu:dev.admutium.techmod.machine.Machines.MENUS.values())net.minecraft.client.gui.screens.MenuScreens.register(menu,dev.admutium.techmod.client.machine.MachineScreen::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(dev.admutium.techmod.machine.Machines.ENTITY,dev.admutium.techmod.client.machine.MachineRenderer::new);
        CustomUnbakedBlockStateModel.register(TechMod.id("cable"), CableModel.Unbaked.CODEC);
    }
}
