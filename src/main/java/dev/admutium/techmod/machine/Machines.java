package dev.admutium.techmod.machine;
import com.mojang.serialization.Codec;
import dev.admutium.techmod.TechMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import java.util.EnumMap;
public final class Machines {
 public static final DataComponentType<Long> CHARGE=DataComponentType.<Long>builder().persistent(Codec.LONG).build();
 public static final DataComponentType<MachineState> STATE=DataComponentType.<MachineState>builder().persistent(MachineState.CODEC).build();
 public static final EnumMap<MachineKind,MachineBlock> BLOCKS=new EnumMap<>(MachineKind.class);
 public static final EnumMap<MachineKind,Item> ITEMS=new EnumMap<>(MachineKind.class);
 public static final EnumMap<MachineKind,MenuType<MachineMenu>> MENUS=new EnumMap<>(MachineKind.class);
 public static final java.util.Map<Item,Item> CRUSHED=new java.util.LinkedHashMap<>();
 public static final java.util.Map<Item,Item> SMELTED=new java.util.LinkedHashMap<>();
 public static BatteryItem BATTERY;
 public static BlockEntityType<MachineBlockEntity> ENTITY;
 public static void init(){
  Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,TechMod.id("charge"),CHARGE);
  Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,TechMod.id("machine_state"),STATE);
  BATTERY=new BatteryItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,TechMod.id("battery"))));
  Registry.register(BuiltInRegistries.ITEM,TechMod.id("battery"),BATTERY);
  Item[] raw={Items.RAW_IRON,Items.RAW_GOLD,Items.RAW_COPPER};Item[] ingots={Items.IRON_INGOT,Items.GOLD_INGOT,Items.COPPER_INGOT};String[] names={"iron","gold","copper"};
  for(int i=0;i<3;i++){var item=new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,TechMod.id("crushed_"+names[i]))));Registry.register(BuiltInRegistries.ITEM,TechMod.id("crushed_"+names[i]),item);CRUSHED.put(raw[i],item);SMELTED.put(item,ingots[i]);}
  for(var kind:MachineKind.values()){
   var b=new MachineBlock(kind,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,TechMod.id(kind.id)))
    .strength(3.5f).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.IMMOVEABLE).lightLevel(s->s.getValue(MachineBlock.ACTIVE)?7:0));
   BLOCKS.put(kind,b);Registry.register(BuiltInRegistries.BLOCK,TechMod.id(kind.id),b);
   var item=new MachineItem(b,new Item.Properties().setId(ResourceKey.create(Registries.ITEM,TechMod.id(kind.id))));
   ITEMS.put(kind,item);Registry.register(BuiltInRegistries.ITEM,TechMod.id(kind.id),item);
   var menu=new MenuType<MachineMenu>((id,inv)->MachineMenu.client(kind,id,inv),FeatureFlags.VANILLA_SET);
   MENUS.put(kind,menu);Registry.register(BuiltInRegistries.MENU,TechMod.id(kind.id),menu);
  }
  ENTITY=FabricBlockEntityTypeBuilder.create(MachineBlockEntity::new,BLOCKS.values().toArray(Block[]::new)).build();
  Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,TechMod.id("machine"),ENTITY);
  ServerTickEvents.END_LEVEL_TICK.register(dev.admutium.techmod.energy.EnergyNetwork::tick);
  ServerChunkEvents.CHUNK_LOAD.register((w,c,generated)->dev.admutium.techmod.energy.EnergyNetwork.invalidate(w));
  ServerChunkEvents.CHUNK_UNLOAD.register((w,c)->dev.admutium.techmod.energy.EnergyNetwork.invalidate(w));
  ServerLevelEvents.UNLOAD.register((s,w)->dev.admutium.techmod.energy.EnergyNetwork.clear(w));
  ServerLifecycleEvents.SERVER_STOPPED.register(s->dev.admutium.techmod.energy.EnergyNetwork.clearAll());
 }
}
