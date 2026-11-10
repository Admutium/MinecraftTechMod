package dev.admutium.techmod;

import dev.admutium.techmod.cable.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class TechMod implements ModInitializer {
    public static final String ID = "techmod";
    public static final CableBlock CABLE = new CableBlock(BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, id("cable")))
            .strength(.25f).noOcclusion().noCollision().dynamicShape().randomTicks());
    public static final BlockEntityType<CableBlockEntity> CABLE_ENTITY = FabricBlockEntityTypeBuilder
            .create(CableBlockEntity::new, CABLE).build();
    public static final Item COPPER_CABLE = new CableItem(properties("copper_cable"), false);
    public static final Item CABLE_HOLDER = new CableItem(properties("cable_holder"), true);
    public static final Item WIRE_CUTTERS = new WireCuttersItem(properties("wire_cutters").stacksTo(1));

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ID, path); }
    private static Item.Properties properties(String path) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(path)));
    }

    @Override
    public void onInitialize() {
        dev.admutium.techmod.machine.Machines.init();
        dev.admutium.techmod.suit.Suits.init();
        dev.admutium.techmod.conveyor.Conveyors.init();
        dev.admutium.techmod.material.Materials.init();
        dev.admutium.techmod.storage.ReinforcedStorage.init();
        Registry.register(BuiltInRegistries.BLOCK, id("cable"), CABLE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("cable"), CABLE_ENTITY);
        Registry.register(BuiltInRegistries.ITEM, id("copper_cable"), COPPER_CABLE);
        Registry.register(BuiltInRegistries.ITEM, id("cable_holder"), CABLE_HOLDER);
        Registry.register(BuiltInRegistries.ITEM, id("wire_cutters"), WIRE_CUTTERS);
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("main"), FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.techmod"))
                .icon(() -> new ItemStack(COPPER_CABLE))
                .displayItems((context, output) -> {
                    var entries=new java.util.LinkedHashSet<Item>();
                    entries.add(dev.admutium.techmod.suit.Suits.ITEM);entries.add(dev.admutium.techmod.suit.Suits.NUTRIENT);entries.add(dev.admutium.techmod.suit.Suits.EMPTY_CAN);
                    entries.add(COPPER_CABLE);entries.add(CABLE_HOLDER);
                    entries.add(dev.admutium.techmod.machine.Machines.BATTERY);
                    entries.addAll(dev.admutium.techmod.machine.Machines.ITEMS.values());
                    entries.addAll(dev.admutium.techmod.machine.Machines.CRUSHED.values());
                    entries.addAll(dev.admutium.techmod.machine.Machines.PLATES.values());
                    entries.addAll(dev.admutium.techmod.conveyor.Conveyors.ITEMS.values());
                    entries.addAll(dev.admutium.techmod.material.Materials.CREATIVE);
                    entries.add(dev.admutium.techmod.storage.ReinforcedStorage.ITEM);
                    entries.forEach(output::accept);
                }).build());
        // TODO(crafting): add balanced recipes for cables, holders, batteries and all machines after material progression is designed.
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CableItem.clearSelections());
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server) -> {CableItem.clearSelection(handler.player.getUUID());dev.admutium.techmod.conveyor.ConveyorItem.clearServerPlayer(handler.player.getUUID());});
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, entity) -> {
            if (state.is(CABLE) && entity instanceof CableBlockEntity cable) {
                CableWorld.removeAll(level, pos, cable.snapshot(), !player.isCreative());
                if (state.getValue(CableBlock.WATERLOGGED) && level.getBlockState(pos).isAir())
                    level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                else if(state.getValue(CableBlock.SNOW)>0 && level.getBlockState(pos).isAir()){
                    var snow=Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS,state.getValue(CableBlock.SNOW));
                    if(snow.canSurvive(level,pos))level.setBlock(pos,snow,Block.UPDATE_ALL);
                }
            }
        });
    }
}
