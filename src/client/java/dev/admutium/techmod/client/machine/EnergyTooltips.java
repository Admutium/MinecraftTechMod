package dev.admutium.techmod.client.machine;
import dev.admutium.techmod.machine.*;
import dev.admutium.techmod.energy.Energy;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
public final class EnergyTooltips {
 public static void init(){ItemTooltipCallback.EVENT.register((stack,context,flags,lines)->{
  boolean machine=stack.getItem() instanceof MachineItem,battery=stack.getItem() instanceof BatteryItem;
  if(!machine&&!battery)return;
  if(!Minecraft.getInstance().hasShiftDown()){lines.add(Component.translatable("tooltip.techmod.hold_shift"));return;}
  lines.removeIf(c->c.getContents() instanceof TranslatableContents t&&t.getKey().startsWith("tooltip.techmod.")||c.getString().matches("  \\d+:.*"));
  if(battery){lines.add(Component.translatable("tooltip.techmod.charge",Energy.format(BatteryItem.charge(stack),true),"300"));lines.add(Component.translatable("tooltip.techmod.limits","4.00","4.00"));}
  else {
   var item=(MachineItem)stack.getItem();var kind=((MachineBlock)item.getBlock()).kind;var state=stack.getOrDefault(Machines.STATE,MachineState.EMPTY);
   long total=0;int count=0;for(var b:state.batteries())if(!b.isEmpty()){count++;total+=BatteryItem.charge(b);}
   lines.add(Component.translatable("tooltip.techmod.installed",count));lines.add(Component.translatable("tooltip.techmod.charge",Energy.format(kind==MachineKind.CABINET?total:state.energy(),true),kind==MachineKind.CABINET?Integer.toString(count*300):"300"));
   lines.add(Component.translatable("tooltip.techmod.limits",kind==MachineKind.GENERATOR?"0.00":"8.00",kind==MachineKind.CABINET||kind==MachineKind.GENERATOR?"8.00":"0.00"));
   if(kind==MachineKind.CABINET)lines.add(Component.translatable("tooltip.techmod.current_limit",Energy.format(Math.min(Energy.PORT_RATE,count*Energy.BATTERY_RATE)*20,true)));
   for(int i=0;i<state.batteries().size();i++)if(!state.batteries().get(i).isEmpty())lines.add(Component.literal("  "+(i+1)+": "+Energy.format(BatteryItem.charge(state.batteries().get(i)),true)+" / 300 TE"));
   if(state.fuelTicks()>0)lines.add(Component.translatable("tooltip.techmod.active_fuel",String.format(java.util.Locale.ROOT,"%.2f",state.fuelTicks()/20.0)));
  }
 });}
}
