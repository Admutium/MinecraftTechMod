package dev.admutium.techmod.machine;
import dev.admutium.techmod.energy.Energy;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class MachineMenu extends AbstractContainerMenu {
 public final MachineKind kind;private final Container inventory;private final ContainerData data;private final MachineBlockEntity machine;
 public static MachineMenu client(MachineKind kind,int id,Inventory inv){return new MachineMenu(kind,id,inv,new SimpleContainer(kind.slots),new SimpleContainerData(20),null);}
 public MachineMenu(MachineKind kind,int id,Inventory inv,MachineBlockEntity be){this(kind,id,inv,be,serverData(be),be);}
 private MachineMenu(MachineKind kind,int id,Inventory player,Container inv,ContainerData data,MachineBlockEntity be){
  super(Machines.MENUS.get(kind),id);this.kind=kind;inventory=inv;this.data=data;machine=be;
  if(kind==MachineKind.CABINET){for(int i=0;i<8;i++){int side=i/4,local=i%4;addSlot(battery(inv,i,60+side*60+(local%2)*22,33+(local/2)*22));}}
  else if(kind==MachineKind.GENERATOR){addSlot(new Slot(inv,0,50,53){@Override public boolean mayPlace(ItemStack s){return MachineBlockEntity.isFuel(s);}});addSlot(battery(inv,1,140,53));}
  else if(kind!=MachineKind.SOLAR){addSlot(new Slot(inv,0,42,53));addSlot(new Slot(inv,1,108,53){@Override public boolean mayPlace(ItemStack s){return false;}});addSlot(battery(inv,2,158,53));}
  addStandardInventorySlots(player,28,138);addDataSlots(data);
 }
 private static Slot battery(Container inv,int index,int x,int y){return new Slot(inv,index,x,y){@Override public boolean mayPlace(ItemStack s){return s.getItem() instanceof BatteryItem;}@Override public int getMaxStackSize(){return 1;}};}
 private static ContainerData serverData(MachineBlockEntity be){return new ContainerData(){public int getCount(){return 20;}public void set(int i,int n){}public int get(int i){
  long[] values={be.stored(),be.capacity(),be.displayIn,be.displayOut,be.displayLoss};
  if(i<10)return (int)(values[i/2]>>>((i%2)*16))&65535;
  return switch(i){case 10->be.progress;case 11->be.fuelTicks;case 12->be.visualActive?1:0;case 13->be.wantsWork?1:0;case 14->be.indicator;case 15->be.fuelTotalTicks;case 16->be.solarEfficiency;case 17->be.solarRate;case 18->be.solarStatus;default->0;};
 }};}
 public long value(int field){return (data.get(field*2)&65535L)|((data.get(field*2+1)&65535L)<<16);}
 public int solarEfficiency(){return data.get(16);}
 public int solarRate(){return data.get(17);}
 public int processTicks(){return kind==MachineKind.CRUSHER?120:160;}
 public int fuelTotalTicks(){return data.get(15);}
 public int progress(){return data.get(10);}public int fuel(){return data.get(11);}public boolean active(){return data.get(12)!=0;}
 public String stateKey(){if(kind==MachineKind.SOLAR)return MachineBlockEntity.solarStatusKey(data.get(18));if(kind==MachineKind.CABINET)return value(1)==0?"no_batteries":"storage";
  if(kind==MachineKind.GENERATOR)return active()?"generating":fuel()>0||!inventory.getItem(0).isEmpty()?"full":"no_fuel";
  if(inventory.getItem(0).isEmpty())return "no_input";return data.get(13)==0?"no_recipe_or_full":data.get(14)==2 || !active()?"no_power":kind==MachineKind.CRUSHER?"crushing":"smelting";}
 @Override public boolean stillValid(Player p){return machine==null || machine.stillValid(p);}
 @Override public ItemStack quickMoveStack(Player p,int index){
  if(kind==MachineKind.SOLAR)return ItemStack.EMPTY;
  var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();int count=kind.slots;
  if(index<count){if(!moveItemStackTo(stack,count,slots.size(),true))return ItemStack.EMPTY;}
  else if(stack.getItem() instanceof BatteryItem){if(!moveItemStackTo(stack,kind==MachineKind.CABINET?0:kind==MachineKind.GENERATOR?1:2,count,false))return ItemStack.EMPTY;}
  else if(kind==MachineKind.CABINET || kind==MachineKind.GENERATOR && !MachineBlockEntity.isFuel(stack) || !moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;
  if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return original;
 }
}
