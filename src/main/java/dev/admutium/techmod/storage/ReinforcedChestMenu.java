package dev.admutium.techmod.storage;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class ReinforcedChestMenu extends AbstractContainerMenu {
 private final Container storage,sample;private final ReinforcedChestEntity chest;
 public ReinforcedChestMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(36),new SimpleContainer(1),null);}
 public ReinforcedChestMenu(int id,Inventory inv,ReinforcedChestEntity be){this(id,inv,be,be.sample,be);}
 private ReinforcedChestMenu(int id,Inventory inv,Container storage,Container sample,ReinforcedChestEntity be){
  super(ReinforcedStorage.MENU,id);this.storage=storage;this.sample=sample;chest=be;storage.startOpen(inv.player);
  for(int i=0;i<36;i++)addSlot(new Slot(storage,i,12+(i%9)*18,28+(i/9)*18));
  addSlot(new Slot(sample,0,190,46){@Override public boolean mayPlace(ItemStack s){return false;}@Override public boolean mayPickup(Player p){return false;}});
  addStandardInventorySlots(inv,12,126);
 }
 @Override public void clicked(int slot,int button,ContainerInput action,Player p){if(slot==36){if(stillValid(p)&&action==ContainerInput.PICKUP)sample.setItem(0,button==1?ItemStack.EMPTY:getCarried().copyWithCount(1));return;}super.clicked(slot,button,action,p);}
 @Override public ItemStack quickMoveStack(Player p,int index){if(index==36)return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(index<36){if(!moveItemStackTo(s,37,slots.size(),true))return ItemStack.EMPTY;}else if(!moveItemStackTo(s,0,36,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
 @Override public boolean stillValid(Player p){return chest==null||chest.stillValid(p);}
 @Override public void removed(Player p){super.removed(p);storage.stopOpen(p);}
}
