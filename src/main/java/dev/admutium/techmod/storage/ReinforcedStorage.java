package dev.admutium.techmod.storage;
import dev.admutium.techmod.TechMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
public final class ReinforcedStorage {
 public static ReinforcedChest BLOCK;public static Item ITEM;public static BlockEntityType<ReinforcedChestEntity> ENTITY;public static MenuType<ReinforcedChestMenu> MENU;
 public static void init(){
  BLOCK=new ReinforcedChest(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,TechMod.id("reinforced_chest"))).strength(3,12).noOcclusion().sound(net.minecraft.world.level.block.SoundType.WOOD));
  Registry.register(BuiltInRegistries.BLOCK,TechMod.id("reinforced_chest"),BLOCK);
  ITEM=Registry.register(BuiltInRegistries.ITEM,TechMod.id("reinforced_chest"),new BlockItem(BLOCK,new Item.Properties().setId(ResourceKey.create(Registries.ITEM,TechMod.id("reinforced_chest"))).useBlockDescriptionPrefix()));
  ENTITY=FabricBlockEntityTypeBuilder.create(ReinforcedChestEntity::new,BLOCK).build();Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,TechMod.id("reinforced_chest"),ENTITY);
  MENU=new MenuType<>((id,inv)->new ReinforcedChestMenu(id,inv),FeatureFlags.VANILLA_SET);Registry.register(BuiltInRegistries.MENU,TechMod.id("reinforced_chest"),MENU);
  // TODO(crafting): assign a recipe after the common machine/component progression is balanced.
 }
}
