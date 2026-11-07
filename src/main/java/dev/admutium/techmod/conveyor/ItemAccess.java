package dev.admutium.techmod.conveyor;
import dev.admutium.techmod.machine.*;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;
/** All mutations stay on the server thread; battery slots are never exposed to automation. */
public final class ItemAccess {
 private static Container container(Level w,BlockPos p){if(!w.hasChunkAt(p))return null;var s=w.getBlockState(p);if(s.getBlock() instanceof ChestBlock chest){if(s.getValue(ChestBlock.TYPE)!=net.minecraft.world.level.block.state.properties.ChestType.SINGLE&&!w.hasChunkAt(p.relative(ChestBlock.getConnectedDirection(s))))return null;return ChestBlock.getContainer(chest,s,w,p,true);}return w.getBlockEntity(p) instanceof Container c?c:null;}
 private static int[] slots(Container c,Direction face,boolean insert){
  if(c instanceof MachineBlockEntity m){if(m.kind.slots==0||m.kind==MachineKind.CABINET||m.itemSides[face.ordinal()]!=(insert?1:2))return new int[0];return insert?new int[]{0}:m.processes()?new int[]{1}:new int[0];}
  if(c instanceof WorldlyContainer sided)return sided.getSlotsForFace(face);
  return java.util.stream.IntStream.range(0,c.getContainerSize()).toArray();
 }
 public static ItemStack peek(Level w,BlockPos p,Direction face,ConveyorEntity arm){
  if(!w.hasChunkAt(p))return ItemStack.EMPTY;
  if(w.getBlockEntity(p) instanceof ConveyorEntity belt){if(belt.kind()!=Conveyors.Kind.BELT||belt.cargo.isEmpty()||belt.cargo.getFirst().progress<8)return ItemStack.EMPTY;var s=belt.cargo.getFirst().stack;return arm.accepts(s)?s.copyWithCount(Math.min(4,s.getCount())):ItemStack.EMPTY;}
  Container c=container(w,p);if(c==null)return ItemStack.EMPTY;
  for(int slot:slots(c,face,false)){var s=c.getItem(slot);if(!s.isEmpty()&&arm.accepts(s)&&(!(c instanceof WorldlyContainer v)||v.canTakeItemThroughFace(slot,s,face)))return s.copyWithCount(Math.min(4,s.getCount()));}return ItemStack.EMPTY;
 }
 public static boolean canPut(Level w,BlockPos p,Direction face,ItemStack s,long tick){
  if(!w.hasChunkAt(p))return false;
  if(w.getBlockEntity(p) instanceof ConveyorEntity belt)return belt.room(s,tick);
  Container c=container(w,p);if(c==null)return false;int room=0;for(int slot:slots(c,face,true)){if(!c.canPlaceItem(slot,s)||c instanceof WorldlyContainer v&&!v.canPlaceItemThroughFace(slot,s,face))continue;var a=c.getItem(slot);if(a.isEmpty()||ItemStack.isSameItemSameComponents(a,s))room+=Math.min(s.getMaxStackSize(),c.getMaxStackSize())-(a.isEmpty()?0:a.getCount());}return room>=s.getCount();
 }
 public static ItemStack take(Level w,BlockPos p,Direction face,ConveyorEntity arm){
  if(!w.hasChunkAt(p))return ItemStack.EMPTY;
  if(w.getBlockEntity(p) instanceof ConveyorEntity belt){if(belt.kind()!=Conveyors.Kind.BELT||belt.cargo.isEmpty()||belt.cargo.getFirst().progress<8)return ItemStack.EMPTY;var first=belt.cargo.getFirst();if(!arm.accepts(first.stack))return ItemStack.EMPTY;ItemStack s=first.stack.split(Math.min(4,first.stack.getCount()));if(first.stack.isEmpty())belt.cargo.removeFirst();belt.sync();return s;}
  Container c=container(w,p);if(c==null)return ItemStack.EMPTY;for(int slot:slots(c,face,false)){var s=c.getItem(slot);if(!s.isEmpty()&&arm.accepts(s)&&(!(c instanceof WorldlyContainer v)||v.canTakeItemThroughFace(slot,s,face))){var result=c.removeItem(slot,Math.min(4,s.getCount()));c.setChanged();return result;}}return ItemStack.EMPTY;
 }
 public static boolean put(Level w,BlockPos p,Direction face,ItemStack s,long tick){
  if(!canPut(w,p,face,s,tick))return false;if(w.getBlockEntity(p) instanceof ConveyorEntity belt){boolean accepted=belt.offer(s,tick);if(accepted){var cargo=belt.cargo.getLast();cargo.progress=belt.cargo.size()==1?10:Math.max(0,Math.min(10,belt.cargo.getFirst().progress-10));belt.sync();}return accepted;}
  Container c=container(w,p);var left=s.copy();for(int slot:slots(c,face,true)){if(!c.canPlaceItem(slot,left)||c instanceof WorldlyContainer v&&!v.canPlaceItemThroughFace(slot,left,face))continue;var a=c.getItem(slot);if(a.isEmpty()){int n=Math.min(left.getCount(),Math.min(left.getMaxStackSize(),c.getMaxStackSize()));c.setItem(slot,left.split(n));}else if(ItemStack.isSameItemSameComponents(a,left)){int n=Math.min(left.getCount(),Math.min(a.getMaxStackSize(),c.getMaxStackSize())-a.getCount());a.grow(n);left.shrink(n);}if(left.isEmpty())break;}c.setChanged();return left.isEmpty();
 }
 private ItemAccess(){}
}
