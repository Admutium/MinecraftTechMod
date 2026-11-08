package dev.admutium.techmod.material;

import dev.admutium.techmod.TechMod;
import dev.admutium.techmod.machine.Machines;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Instant manual processing; tool remainder copies its original components and wears once per craft. */
public final class ManualRecipe extends CustomRecipe {
 public static final RecipeSerializer<ManualRecipe> SERIALIZER=new RecipeSerializer<>(MapCodec.unit(ManualRecipe::new),StreamCodec.unit(new ManualRecipe()));
 public static void init(){Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,TechMod.id("manual_processing"),SERIALIZER);}
 private ItemStack result(CraftingInput in){
  Item tool=null,ingredient=null;int quantity=0,tools=0;
  for(var s:in.items())if(!s.isEmpty()){
   if(s.is(Materials.HAMMER)||s.is(Materials.CUTTERS)){tool=s.getItem();tools++;if(s.getDamageValue()>=s.getMaxDamage())return ItemStack.EMPTY;}
   else{if(ingredient!=null&&ingredient!=s.getItem())return ItemStack.EMPTY;ingredient=s.getItem();quantity++;}
  }
  if(tools!=1)return ItemStack.EMPTY;
  if(tool==Materials.HAMMER){if(ingredient==Materials.ROOTS&&quantity==2)return new ItemStack(Materials.CAOUTCHOUC);if(quantity==1&&Machines.PLATES.containsKey(ingredient)&&ingredient!=Materials.ROOTS)return new ItemStack(Machines.PLATES.get(ingredient));}
  if(tool==Materials.CUTTERS&&quantity==1&&Machines.CUT.containsKey(ingredient))return new ItemStack(Machines.CUT.get(ingredient),4);
  return ItemStack.EMPTY;
 }
 @Override public boolean matches(CraftingInput in,Level level){return !result(in).isEmpty();}
 @Override public ItemStack assemble(CraftingInput in){return result(in);}
 @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput in){
  var out=NonNullList.withSize(in.size(),ItemStack.EMPTY);
  for(int i=0;i<in.size();i++){var original=in.getItem(i);if(original.is(Materials.HAMMER)||original.is(Materials.CUTTERS)){
   int damage=original.getDamageValue()+1;if(damage<original.getMaxDamage()){var copy=original.copyWithCount(1);copy.setDamageValue(damage);out.set(i,copy);}
  }}return out;
 }
 @Override public RecipeSerializer<ManualRecipe> getSerializer(){return SERIALIZER;}
}
