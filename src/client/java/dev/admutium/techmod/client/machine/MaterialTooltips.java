package dev.admutium.techmod.client.machine;
import dev.admutium.techmod.material.Materials;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
public final class MaterialTooltips {
 public static void init(){ItemTooltipCallback.EVENT.register((stack,context,flags,lines)->{
  if(!Minecraft.getInstance().hasShiftDown())return;
  if(stack.is(Materials.SEEDS)||stack.is(Materials.ROOTS))lines.add(Component.translatable("tooltip.techmod.dandelion_alt"));
  String key=stack.is(Materials.HAMMER)?"hammer":stack.is(Materials.CUTTERS)?"cutters":stack.is(Materials.ROOTS)?"roots_processing":stack.is(Materials.CAOUTCHOUC)?"caoutchouc_processing":stack.is(Materials.SEEDS)?"seeds_growing":null;
  if(key!=null)lines.add(Component.translatable("tooltip.techmod."+key));
 });}
 private MaterialTooltips(){}
}
