package dev.admutium.techmod.material;

import dev.admutium.techmod.TechMod;
import dev.admutium.techmod.machine.Machines;
import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.biome.Biomes;
import java.util.*;

public final class Materials {
 public static final List<Item> CREATIVE=new ArrayList<>();
 public static Item HAMMER,CUTTERS,COPPER_WIRE,ROOTS,SEEDS,CAOUTCHOUC,RUBBER,RAW_TIN,TIN_INGOT,CRUSHED_TIN,TIN_PLATE;
 public static RubberDandelion CROP;
 public static Block TIN_ORE,DEEPSLATE_TIN_ORE,TIN_BLOCK;
 private static Item.Properties props(String name){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,TechMod.id(name)));}
 private static Item item(String name){return register(name,new Item(props(name)));}
 private static Item register(String name,Item item){Registry.register(BuiltInRegistries.ITEM,TechMod.id(name),item);CREATIVE.add(item);return item;}
 private static Block block(String name,float strength,float resistance,SoundType sound){
  Block b=new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,TechMod.id(name))).strength(strength,resistance).sound(sound).requiresCorrectToolForDrops());
  Registry.register(BuiltInRegistries.BLOCK,TechMod.id(name),b);register(name,new BlockItem(b,props(name).useBlockDescriptionPrefix()));return b;
 }
 public static void init(){
  HAMMER=register("hammer",new Item(props("hammer").durability(32)));
  // Provisional manual cutter durability; crafting progression will be balanced separately.
  CUTTERS=register("cutters",new Item(props("cutters").durability(64)));
  COPPER_WIRE=item("copper_wire");ROOTS=item("dandelion_roots");CAOUTCHOUC=item("caoutchouc");RUBBER=item("rubber");
  RAW_TIN=item("raw_tin");TIN_INGOT=item("tin_ingot");CRUSHED_TIN=item("crushed_tin");TIN_PLATE=item("tin_plate");
  TIN_ORE=block("tin_ore",3,3,SoundType.STONE);DEEPSLATE_TIN_ORE=block("deepslate_tin_ore",4.5f,3,SoundType.DEEPSLATE);TIN_BLOCK=block("tin_block",3,6,SoundType.METAL);
  CROP=new RubberDandelion(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,TechMod.id("rubber_dandelion"))).noCollision().noOcclusion().instabreak().sound(SoundType.CROP).randomTicks());
  Registry.register(BuiltInRegistries.BLOCK,TechMod.id("rubber_dandelion"),CROP);
  SEEDS=register("dandelion_seeds",new BlockItem(CROP,props("dandelion_seeds")));
  Machines.CRUSHED.put(RAW_TIN,CRUSHED_TIN);Machines.SMELTED.put(RAW_TIN,TIN_INGOT);Machines.SMELTED.put(CRUSHED_TIN,TIN_INGOT);
  Machines.SMELTED.put(TIN_ORE.asItem(),TIN_INGOT);Machines.SMELTED.put(DEEPSLATE_TIN_ORE.asItem(),TIN_INGOT);
  Machines.SMELTED.put(CAOUTCHOUC,RUBBER);Machines.PLATES.put(TIN_INGOT,TIN_PLATE);Machines.PLATES.put(ROOTS,CAOUTCHOUC);
  Machines.CUT.put(Machines.PLATES.get(Items.COPPER_INGOT),COPPER_WIRE);
  ManualRecipe.init();
  Registry.register(BuiltInRegistries.FEATURE_TYPE,TechMod.id("dandelion_patch"),DandelionPatch.CODEC);
  BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,TechMod.id("tin_ore")));
  BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.PLAINS,Biomes.SUNFLOWER_PLAINS,Biomes.MEADOW),GenerationStep.Decoration.VEGETAL_DECORATION,ResourceKey.create(Registries.PLACED_FEATURE,TechMod.id("dandelion_patch")));
 }
 private Materials(){}
}
