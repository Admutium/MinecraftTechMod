package dev.admutium.techmod.conveyor;
import dev.admutium.techmod.TechMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.*;
public final class Conveyors {
 public enum Kind {BELT("conveyor"),MANIPULATOR("manipulator"),SPLITTER("splitter");public final String id;Kind(String id){this.id=id;}}
 public static final EnumMap<Kind,ConveyorBlock> BLOCKS=new EnumMap<>(Kind.class);
 public static final EnumMap<Kind,Item> ITEMS=new EnumMap<>(Kind.class);
 public static BlockEntityType<ConveyorEntity> ENTITY;
 public static MenuType<ConveyorMenu> MENU;
 public static void init(){
  FilterPayload.init();ConveyorModePayload.init();
  for(var k:Kind.values()){
   var b=new ConveyorBlock(k,BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,TechMod.id(k.id))).strength(2).noOcclusion().requiresCorrectToolForDrops());BLOCKS.put(k,b);Registry.register(BuiltInRegistries.BLOCK,TechMod.id(k.id),b);
   var item=new ConveyorItem(b,new Item.Properties().setId(ResourceKey.create(Registries.ITEM,TechMod.id(k.id))).useBlockDescriptionPrefix());ITEMS.put(k,item);Registry.register(BuiltInRegistries.ITEM,TechMod.id(k.id),item);
  }
  ENTITY=FabricBlockEntityTypeBuilder.create(ConveyorEntity::new,BLOCKS.values().toArray(ConveyorBlock[]::new)).build();Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,TechMod.id("conveyor"),ENTITY);
  MENU=new MenuType<>((id,inv)->new ConveyorMenu(id,inv),FeatureFlags.VANILLA_SET);Registry.register(BuiltInRegistries.MENU,TechMod.id("conveyor"),MENU);
  ServerTickEvents.END_LEVEL_TICK.register(ConveyorNetwork::tick);
  ServerChunkEvents.CHUNK_LOAD.register((w,c,g)->ConveyorNetwork.invalidate(w));ServerChunkEvents.CHUNK_UNLOAD.register((w,c)->ConveyorNetwork.invalidate(w));
  ServerLevelEvents.UNLOAD.register((s,w)->ConveyorNetwork.clear(w));ServerLifecycleEvents.SERVER_STOPPED.register(s->{ConveyorNetwork.clearAll();ConveyorItem.clear();});
 }
}
