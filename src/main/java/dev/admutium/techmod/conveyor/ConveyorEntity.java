package dev.admutium.techmod.conveyor;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import java.util.*;
public final class ConveyorEntity extends BlockEntity implements MenuProvider {
 public static final class Cargo {public ItemStack stack;public int progress;public Cargo(ItemStack s,int p){stack=s;progress=p;}}
 public final List<Cargo> cargo=new ArrayList<>();public final SimpleContainer filter=new SimpleContainer(9);
 public BlockPos drivePos;
 public boolean filtering,reverse,moving,powered;public int armPhase,status=1,driveDistance;public boolean rightNext;public ItemStack hand=ItemStack.EMPTY;public long syncedAt,lastArrival=-100,lastGate=-100;
 public ConveyorEntity(BlockPos p,BlockState s){super(Conveyors.ENTITY,p,s);}
 public Conveyors.Kind kind(){return ((ConveyorBlock)getBlockState().getBlock()).kind;}
 public boolean accepts(ItemStack s){if(!filtering)return true;for(int i=0;i<9;i++)if(filter.getItem(i).is(s.getItem()))return true;return false;}
 public boolean room(ItemStack stack,long tick){return kind()!=Conveyors.Kind.MANIPULATOR&&cargo.size()<2&&tick-lastArrival>=20&&accepts(stack);}
 public boolean offer(ItemStack stack,long tick){if(stack.isEmpty()||stack.getCount()>4||!room(stack,tick))return false;cargo.add(new Cargo(stack.copy(),0));lastArrival=tick;sync();return true;}
 public void sync(){setChanged();if(level instanceof ServerLevel w){syncedAt=w.getGameTime();w.getChunkSource().blockChanged(worldPosition);w.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}}
 @Override public void preRemoveSideEffects(BlockPos p,BlockState replacement){
  if(level instanceof ServerLevel w){for(var c:cargo)Block.popResource(w,p,c.stack);cargo.clear();Block.popResource(w,p,hand);hand=ItemStack.EMPTY;
   int slope=ConveyorBlock.slope(getBlockState());if(slope!=0){var other=p.offset(0,slope,0);if(w.getBlockState(other).is(getBlockState().getBlock())&&w.getBlockState(other).getValue(ConveyorBlock.UPPER))w.removeBlock(other,false);}ConveyorNetwork.invalidate(w);
  }
 }
 @Override public void setRemoved(){if(level!=null)ConveyorNetwork.invalidate(level);super.setRemoved();}
 @Override public void saveAdditional(ValueOutput o){super.saveAdditional(o);if(drivePos!=null)o.store("drive",BlockPos.CODEC,drivePos);o.store("cargo",ItemStack.OPTIONAL_CODEC.listOf(),cargo.stream().map(c->c.stack).toList());o.store("progress",com.mojang.serialization.Codec.INT.listOf(),cargo.stream().map(c->c.progress).toList());o.store("filter",ItemStack.OPTIONAL_CODEC.listOf(),java.util.stream.IntStream.range(0,9).mapToObj(filter::getItem).toList());o.store("hand",ItemStack.OPTIONAL_CODEC,hand);o.putBoolean("filtering",filtering);o.putBoolean("reverse",reverse);o.putBoolean("right",rightNext);o.putBoolean("moving",moving);o.putBoolean("powered",powered);o.putInt("phase",armPhase);o.putInt("status",status);o.putInt("drive_distance",driveDistance);o.putLong("sync",syncedAt);o.putLong("arrival",lastArrival);o.putLong("gate",lastGate);}
 @Override public void loadAdditional(ValueInput i){super.loadAdditional(i);drivePos=i.read("drive",BlockPos.CODEC).orElse(null);cargo.clear();var stacks=i.read("cargo",ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());var times=i.read("progress",com.mojang.serialization.Codec.INT.listOf()).orElse(List.of());for(int n=0;n<Math.min(2,stacks.size());n++)if(!stacks.get(n).isEmpty())cargo.add(new Cargo(stacks.get(n).copyWithCount(Math.min(4,stacks.get(n).getCount())),n<times.size()?Math.clamp(times.get(n),0,20):0));var f=i.read("filter",ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());for(int n=0;n<9;n++)filter.setItem(n,n<f.size()?f.get(n).copyWithCount(1):ItemStack.EMPTY);hand=i.read("hand",ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);filtering=i.getBooleanOr("filtering",false);reverse=i.getBooleanOr("reverse",false);rightNext=i.getBooleanOr("right",false);moving=i.getBooleanOr("moving",false);powered=i.getBooleanOr("powered",false);armPhase=Math.clamp(i.getIntOr("phase",0),0,20);status=i.getIntOr("status",1);driveDistance=i.getIntOr("drive_distance",0);syncedAt=i.getLongOr("sync",0);lastArrival=i.getLongOr("arrival",-100);lastGate=i.getLongOr("gate",-100);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveCustomOnly(r);}
 public String statusKey(){return switch(status){case 0->"running";case 1->"no_drive";case 2->"no_power";case 3->"redstone";default->"blocked";};}
 @Override public Component getDisplayName(){return Component.translatable("block.techmod."+kind().id);}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new ConveyorMenu(id,inv,this);}
}
