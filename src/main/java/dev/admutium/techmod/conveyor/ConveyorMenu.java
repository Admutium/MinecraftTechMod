package dev.admutium.techmod.conveyor;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class ConveyorMenu extends AbstractContainerMenu {
 public final ConveyorEntity be;private final Container ghosts;private final ContainerData data;
 public ConveyorMenu(int id,Inventory inv){this(id,inv,null);}
 public ConveyorMenu(int id,Inventory inv,ConveyorEntity entity){super(Conveyors.MENU,id);be=entity;ghosts=be==null?new SimpleContainer(9):be.filter;
  for(int i=0;i<9;i++)addSlot(new Slot(ghosts,i,18+(i%3)*20,63+(i/3)*20){@Override public boolean mayPickup(Player p){return false;}});
  addStandardInventorySlots(inv,19,146);
  data=be==null?new SimpleContainerData(4):new ContainerData(){public int getCount(){return 4;}public void set(int i,int n){}public int get(int i){return switch(i){case 0->be.filtering?1:0;case 1->be.reverse?1:0;case 2->be.status;default->be.kind().ordinal();};}};addDataSlots(data);
 }
 public boolean filtering(){return data.get(0)!=0;}public boolean reverse(){return data.get(1)!=0;}public int status(){return data.get(2);}public int kind(){return data.get(3);}
 @Override public void clicked(int slot,int button,ContainerInput action,Player p){if(slot>=0&&slot<9){if(be!=null){if(button==1)ghosts.setItem(slot,ItemStack.EMPTY);else if(!getCarried().isEmpty())ghosts.setItem(slot,getCarried().copyWithCount(1));be.sync();broadcastChanges();}return;}super.clicked(slot,button,action,p);}
 public void sample(int slot,ItemStack s){if(be!=null&&slot>=0&&slot<9){be.filter.setItem(slot,s.copyWithCount(1));be.sync();broadcastChanges();}}
 @Override public boolean clickMenuButton(Player p,int button){if(be==null||!stillValid(p))return false;if(button==0)be.filtering=!be.filtering;else if(button==1)be.reverse=!be.reverse;else return false;be.sync();return true;}
 @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
 @Override public boolean stillValid(Player p){return be==null||!be.isRemoved()&&p.distanceToSqr(be.getBlockPos().getX()+.5,be.getBlockPos().getY()+.5,be.getBlockPos().getZ()+.5)<64;}
}
