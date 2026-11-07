package dev.admutium.techmod.machine;
import dev.admutium.techmod.energy.Energy;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public final class MachineItem extends BlockItem {
 public MachineItem(MachineBlock b,Properties p){super(b,p.useBlockDescriptionPrefix().stacksTo(1));}
 @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,TooltipDisplay d,Consumer<Component> out,TooltipFlag f){
  var state=s.getOrDefault(Machines.STATE,MachineState.EMPTY);int count=0;long total=0;
  for(var b:state.batteries())if(!b.isEmpty()){count++;total+=BatteryItem.charge(b);}
  out.accept(Component.translatable("tooltip.techmod.installed",count));
  if(((MachineBlock)getBlock()).kind==MachineKind.CABINET)
   out.accept(Component.translatable("tooltip.techmod.charge",Energy.format(total),Integer.toString(count*300)));
  else out.accept(Component.translatable("tooltip.techmod.charge",Energy.format(state.energy()),"300"));
  for(int i=0;i<state.batteries().size();i++)if(!state.batteries().get(i).isEmpty())
    out.accept(Component.literal("  "+(i+1)+": "+Energy.format(BatteryItem.charge(state.batteries().get(i)))+" / 300 TE"));
  if(state.fuelTicks()>0)out.accept(Component.translatable("tooltip.techmod.active_fuel",state.fuelTicks()/20));
 }
}
