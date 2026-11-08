package dev.admutium.techmod.storage;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import java.util.List;
public final class ReinforcedChestEntity extends BaseContainerBlockEntity {
 private NonNullList<ItemStack> items=NonNullList.withSize(36,ItemStack.EMPTY);
 public final SimpleContainer sample=new SimpleContainer(1){@Override public void setChanged(){super.setChanged();ReinforcedChestEntity.this.setChanged();if(level!=null&&!level.isClientSide())level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);}};
 private int viewers;
 public ReinforcedChestEntity(BlockPos p,BlockState s){super(ReinforcedStorage.ENTITY,p,s);}
 @Override public int getContainerSize(){return 36;}
 @Override protected NonNullList<ItemStack> getItems(){return items;}
 @Override protected void setItems(NonNullList<ItemStack> i){items=i;}
 @Override protected Component getDefaultName(){return Component.translatable("block.techmod.reinforced_chest");}
 @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return new ReinforcedChestMenu(id,inv,this);}
 @Override public void startOpen(net.minecraft.world.entity.ContainerUser p){if(!(p.getLivingEntity() instanceof Player player)||!player.isSpectator())setOpen(++viewers>0);}
 @Override public void stopOpen(net.minecraft.world.entity.ContainerUser p){if(!(p.getLivingEntity() instanceof Player player)||!player.isSpectator())setOpen((viewers=Math.max(0,viewers-1))>0);}
 // Recover a saved open lid after a restart or chunk reload with no active viewers.
 public void closeIfUnused(){if(viewers==0&&getBlockState().getValue(ReinforcedChest.OPEN))setOpen(false);}
 private void setOpen(boolean open){if(level!=null&&!level.isClientSide()&&!isRemoved()&&getBlockState().getValue(ReinforcedChest.OPEN)!=open){level.setBlock(worldPosition,getBlockState().setValue(ReinforcedChest.OPEN,open),2);level.playSound(null,worldPosition,open?net.minecraft.sounds.SoundEvents.CHEST_OPEN:net.minecraft.sounds.SoundEvents.CHEST_CLOSE,net.minecraft.sounds.SoundSource.BLOCKS,.4f,.9f);}}
 @Override protected void saveAdditional(ValueOutput o){super.saveAdditional(o);o.store("items",ItemStack.OPTIONAL_CODEC.listOf(),items);o.store("sample",ItemStack.OPTIONAL_CODEC,sample.getItem(0));}
 @Override protected void loadAdditional(ValueInput i){super.loadAdditional(i);var list=i.read("items",ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());items=NonNullList.withSize(36,ItemStack.EMPTY);for(int k=0;k<Math.min(36,list.size());k++)items.set(k,list.get(k));sample.setItem(0,i.read("sample",ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY).copyWithCount(1));}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){// Never transmit the entire chest inventory to nearby clients.
  var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,r);output.store("sample",ItemStack.OPTIONAL_CODEC,sample.getItem(0));return output.buildResult();}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
