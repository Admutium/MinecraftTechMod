package dev.admutium.techmod.conveyor;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import java.util.*;
public final class ConveyorItem extends BlockItem {
 private static final Map<UUID,Integer> MODES=new java.util.concurrent.ConcurrentHashMap<>(),CLIENT_MODES=new java.util.concurrent.ConcurrentHashMap<>();
 public ConveyorItem(ConveyorBlock b,Properties p){super(b,p);}
 public static int mode(Player p){return p==null?0:(p.level().isClientSide()?CLIENT_MODES:MODES).getOrDefault(p.getUUID(),0);}
 public static void clearServerPlayer(UUID id){MODES.remove(id);}
 public static void clearClient(){CLIENT_MODES.clear();}
 public static void clear(){MODES.clear();CLIENT_MODES.clear();}
 @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<Component> out,TooltipFlag flag){
  out.accept(Component.translatable("tooltip.techmod.conveyor_mode"));out.accept(Component.translatable("tooltip.techmod.conveyor_rotate"));
 }
 public static void setMode(Player p,int mode){if(mode < -1||mode>1)return;(p.level().isClientSide()?CLIENT_MODES:MODES).put(p.getUUID(),mode);}
 public static net.minecraft.world.item.context.BlockPlaceContext placementContext(net.minecraft.world.item.context.BlockPlaceContext c){
  if(!(c.getItemInHand().getItem() instanceof ConveyorItem i)||((ConveyorBlock)i.getBlock()).kind!=Conveyors.Kind.BELT||c.getPlayer()!=null&&c.getPlayer().isShiftKeyDown())return c;
  var w=c.getLevel();var hit=c.getClickLocation();var clicked=net.minecraft.core.BlockPos.containing(hit.subtract(c.getClickedFace().getStepX()*.001,c.getClickedFace().getStepY()*.001,c.getClickedFace().getStepZ()*.001));
  var state=w.getBlockState(clicked);
  if(state.getBlock() instanceof ConveyorBlock b&&b.kind==Conveyors.Kind.BELT){var base=ConveyorBlock.owner(clicked,state);state=w.getBlockState(base);var end=ConveyorBlock.output(base,state);
   if(w.getBlockState(end).isAir()){
    var direction=state.getValue(ConveyorBlock.FACING);
    // Only end-extension inherits direction. Ground placement still follows the player's view for corners.
    return new net.minecraft.world.item.context.BlockPlaceContext(w,c.getPlayer(),c.getHand(),c.getItemInHand(),new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(end),direction,end,false)){
     @Override public net.minecraft.core.Direction getHorizontalDirection(){return direction;}
    };
   }
  }return c;
 }
 @Override public net.minecraft.world.item.context.BlockPlaceContext updatePlacementContext(net.minecraft.world.item.context.BlockPlaceContext c){return placementContext(c);}
}
