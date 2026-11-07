package dev.admutium.techmod.machine;
import dev.admutium.techmod.energy.Energy;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import java.util.List;
import java.util.function.Consumer;
public final class BatteryItem extends Item {
 public BatteryItem(Properties p){super(p.stacksTo(1));}
 public static long charge(ItemStack s){return s.getItem() instanceof BatteryItem?Energy.clamp(s.getOrDefault(Machines.CHARGE,0L),Energy.BATTERY):0;}
 public static void setCharge(ItemStack s,long e){
  if(!(s.getItem() instanceof BatteryItem))return;
  e=Energy.clamp(e,Energy.BATTERY);s.set(Machines.CHARGE,e);
  s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(List.of((float)(100*e/Energy.BATTERY)),List.of(),List.of(),List.of()));
 }
 @Override public boolean isBarVisible(ItemStack s){return true;}
 @Override public int getBarWidth(ItemStack s){return (int)Math.round(13.0*charge(s)/Energy.BATTERY);}
 @Override public int getBarColor(ItemStack s){return 0x82ce36;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,TooltipDisplay d,Consumer<Component> out,TooltipFlag f){
  out.accept(Component.translatable("tooltip.techmod.charge",Energy.format(charge(s)),"300").append(Component.literal(" ("+(100*charge(s)/Energy.BATTERY)+"%)")));

 }
}
