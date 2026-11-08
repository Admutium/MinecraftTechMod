package dev.admutium.techmod.client;

import dev.admutium.techmod.TechMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;

public final class TechModClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        dev.admutium.techmod.client.conveyor.ConveyorPreview.init();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->dev.admutium.techmod.conveyor.ConveyorItem.clearClient());
        net.minecraft.client.gui.screens.MenuScreens.register(dev.admutium.techmod.storage.ReinforcedStorage.MENU,dev.admutium.techmod.client.storage.ChestScreen::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(dev.admutium.techmod.storage.ReinforcedStorage.ENTITY,dev.admutium.techmod.client.storage.ChestRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(dev.admutium.techmod.conveyor.Conveyors.MENU,dev.admutium.techmod.client.conveyor.ConveyorScreen::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(dev.admutium.techmod.conveyor.Conveyors.ENTITY,dev.admutium.techmod.client.conveyor.ConveyorRenderer::new);
        dev.admutium.techmod.client.sound.MachineAudio.init();
        dev.admutium.techmod.client.machine.EnergyTooltips.init();
        dev.admutium.techmod.client.machine.MaterialTooltips.init();
        for(var menu:dev.admutium.techmod.machine.Machines.MENUS.values())net.minecraft.client.gui.screens.MenuScreens.register(menu,dev.admutium.techmod.client.machine.MachineScreen::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(dev.admutium.techmod.machine.Machines.ENTITY,dev.admutium.techmod.client.machine.MachineRenderer::new);
        CustomUnbakedBlockStateModel.register(TechMod.id("solar"),SolarModel.Unbaked.CODEC);
        CustomUnbakedBlockStateModel.register(TechMod.id("cable"), CableModel.Unbaked.CODEC);
    }
}
