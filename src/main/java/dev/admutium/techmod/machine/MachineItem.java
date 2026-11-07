package dev.admutium.techmod.machine;
import dev.admutium.techmod.energy.Energy;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
public final class MachineItem extends BlockItem {
 public MachineItem(MachineBlock b,Properties p){super(b,p.useBlockDescriptionPrefix().stacksTo(1));}
 @Override public net.minecraft.world.InteractionResult place(net.minecraft.world.item.context.BlockPlaceContext c){
  var old=c.getLevel().getBlockEntity(c.getClickedPos());var cable=old instanceof dev.admutium.techmod.cable.CableBlockEntity be?be.snapshot():dev.admutium.techmod.cable.CableSnapshot.EMPTY;
  var result=super.place(c);
  if(result.consumesAction()&&((MachineBlock)getBlock()).kind==MachineKind.SOLAR&&c.getLevel().getBlockEntity(c.getClickedPos()) instanceof MachineBlockEntity be)be.update(cable);
  return result;
 }
 @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,TooltipDisplay d,Consumer<Component> out,TooltipFlag f){
  var state=s.getOrDefault(Machines.STATE,MachineState.EMPTY);int count=0;long total=0;
  for(var b:state.batteries())if(!b.isEmpty()){count++;total+=BatteryItem.charge(b);}
  if(((MachineBlock)getBlock()).kind.slots>0)out.accept(Component.translatable("tooltip.techmod.installed",count));
  if(((MachineBlock)getBlock()).kind==MachineKind.CABINET)
   out.accept(Component.translatable("tooltip.techmod.charge",Energy.format(total),Integer.toString(count*300)));
  else out.accept(Component.translatable("tooltip.techmod.charge",Energy.format(state.energy()),((MachineBlock)getBlock()).kind.slots==0?"10":"300"));
  for(int i=0;i<state.batteries().size();i++)if(!state.batteries().get(i).isEmpty())
    out.accept(Component.literal("  "+(i+1)+": "+Energy.format(BatteryItem.charge(state.batteries().get(i)))+" / 300 TE"));
  if(state.fuelTicks()>0)out.accept(Component.translatable("tooltip.techmod.active_fuel",state.fuelTicks()/20));
 }
}
