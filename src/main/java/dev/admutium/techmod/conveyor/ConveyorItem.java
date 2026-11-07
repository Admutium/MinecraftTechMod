package dev.admutium.techmod.conveyor;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import java.util.*;
public final class ConveyorItem extends BlockItem {
 private static final Map<UUID,Integer> MODES=new HashMap<>(),CLIENT_MODES=new HashMap<>();
 public ConveyorItem(ConveyorBlock b,Properties p){super(b,p);}
 public static int mode(Player p){return p==null?0:(p.level().isClientSide()?CLIENT_MODES:MODES).getOrDefault(p.getUUID(),0);}
 public static void clear(){MODES.clear();CLIENT_MODES.clear();}
 @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<Component> out,TooltipFlag flag){
  out.accept(Component.translatable("tooltip.techmod.conveyor_mode"));out.accept(Component.translatable("tooltip.techmod.conveyor_rotate"));
 }
 @Override public InteractionResult use(Level w,Player p,InteractionHand hand){
  if(p.isShiftKeyDown()&&((ConveyorBlock)getBlock()).kind==Conveyors.Kind.BELT){int n=mode(p)==0?1:mode(p)==1?-1:0;(w.isClientSide()?CLIENT_MODES:MODES).put(p.getUUID(),n);p.sendOverlayMessage(Component.translatable("conveyor.techmod.mode."+n));return InteractionResult.SUCCESS;}
  return InteractionResult.PASS;
 }
}
