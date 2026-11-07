package dev.admutium.techmod.machine;
import dev.admutium.techmod.energy.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import java.util.*;
public final class MachineBlockEntity extends BlockEntity implements Container,MenuProvider {
 private final NonNullList<ItemStack> items;
 public final MachineKind kind;
 public long energy,incoming,outgoing,loss,displayIn,displayOut,displayLoss;
 private final long[][] flowWindow=new long[3][20];private int flowCursor;
 public int fuelTicks,fuelTotalTicks,progress,indicator;public boolean working,wantsWork,visualActive;
 private final int[] workHistory=new int[20],demandHistory=new int[20];private int workSum,demandSum;
 private long lastSent=-1;private String recipeKey="";private boolean removedContents;
 private int batteryCursor;
 private RecipeHolder<SmeltingRecipe> cachedRecipe;
 public MachineBlockEntity(BlockPos p,BlockState s){super(Machines.ENTITY,p,s);kind=((MachineBlock)s.getBlock()).kind;items=NonNullList.withSize(kind.slots,ItemStack.EMPTY);}
 public boolean processes(){return kind==MachineKind.FURNACE||kind==MachineKind.CRUSHER;}
 public int processTicks(){return kind==MachineKind.CRUSHER?120:160;}
 public static int fuelDuration(ItemStack s){return s.is(Items.COAL_BLOCK)?9000:isFuel(s)?900:0;}
 public int batterySlot(){return kind==MachineKind.GENERATOR?1:2;}
 public long stored(){if(kind!=MachineKind.CABINET)return energy;long sum=0;for(var s:items)sum+=BatteryItem.charge(s);return sum;}
 public long capacity(){if(kind!=MachineKind.CABINET)return Energy.BUFFER;return items.stream().filter(s->s.getItem() instanceof BatteryItem).count()*Energy.BATTERY;}
 public long receive(long amount){
  amount=Math.max(0,Math.min(amount,capacity()-stored()));long left=amount;
  if(kind!=MachineKind.CABINET){energy+=amount;return amount;}
  while(left>0){int index=-1;long lowest=Long.MAX_VALUE;for(int k=0;k<8;k++){int i=(batteryCursor+k)%8;long e=BatteryItem.charge(items.get(i));if(items.get(i).getItem() instanceof BatteryItem && e<Energy.BATTERY && e<lowest){index=i;lowest=e;}}
   if(index<0)break;long n=Math.min(left,Math.min(Energy.BATTERY-lowest,Math.max(1,(left+7)/8)));BatteryItem.setCharge(items.get(index),lowest+n);left-=n;batteryCursor=(index+1)%8;
  }return amount-left;
 }
 public long portLimit(){return kind==MachineKind.CABINET?Math.min(Energy.PORT_RATE,items.stream().filter(s->s.getItem() instanceof BatteryItem).count()*Energy.BATTERY_RATE):Energy.PORT_RATE;}
 public long extract(long amount){
  amount=Math.max(0,Math.min(amount,stored()));long left=amount;
  if(kind!=MachineKind.CABINET){energy-=amount;return amount;}
  while(left>0){int index=-1;long highest=0;for(int k=0;k<8;k++){int i=(batteryCursor+k)%8;long e=BatteryItem.charge(items.get(i));if(e>highest){index=i;highest=e;}}
   if(index<0)break;long n=Math.min(left,Math.min(highest,Math.max(1,(left+7)/8)));BatteryItem.setCharge(items.get(index),highest-n);left-=n;batteryCursor=(index+1)%8;
  }return amount-left;
 }
 public void beginTick(){
  if(kind==MachineKind.CABINET && level instanceof ServerLevel w){var above=w.getBlockState(worldPosition.above());if(above.is(Machines.BLOCKS.get(kind)) && above.getValue(MachineBlock.UPPER))w.removeBlock(worldPosition.above(),false);}
  incoming=outgoing=loss=0;working=false;wantsWork=false;
  if(kind==MachineKind.GENERATOR && Energy.BUFFER-energy>=Energy.GENERATION){
   if(fuelTicks==0 && isFuel(items.get(0))){fuelTicks=fuelTotalTicks=fuelDuration(items.get(0));items.get(0).shrink(1);}
   if(fuelTicks>0){energy+=Energy.GENERATION;fuelTicks--;working=true;setChanged();}
  }
  if(processes())wantsWork=outputForRecipe()!=null;
 }
 private boolean fits(ItemStack result){var out=items.get(1);return !result.isEmpty() && (out.isEmpty() || ItemStack.isSameItemSameComponents(out,result)&&out.getCount()+result.getCount()<=out.getMaxStackSize());}
 private ItemStack outputForRecipe(){
  if(!(level instanceof ServerLevel w) || items.get(0).isEmpty()){cachedRecipe=null;return null;}
  Item special=kind==MachineKind.CRUSHER?Machines.CRUSHED.get(items.get(0).getItem()):Machines.SMELTED.get(items.get(0).getItem());
  if(kind==MachineKind.CRUSHER || special!=null){cachedRecipe=null;ItemStack result=special==null?ItemStack.EMPTY:new ItemStack(special,kind==MachineKind.CRUSHER?2:1);return fits(result)?result:null;}
  var input=new SingleRecipeInput(items.get(0));
  var recipe=w.recipeAccess().getRecipeFor(RecipeType.SMELTING,input,w,cachedRecipe).orElse(null);
  cachedRecipe=recipe;if(recipe==null)return null;
  ItemStack result=recipe.value().assemble(input);
  ItemStack out=items.get(1);
  return !result.isEmpty() && (out.isEmpty() || ItemStack.isSameItemSameComponents(out,result) && out.getCount()+result.getCount()<=out.getMaxStackSize())?result:null;
 }
 public void finishTick(){
  displayIn+=incoming-flowWindow[0][flowCursor];displayOut+=outgoing-flowWindow[1][flowCursor];displayLoss+=loss-flowWindow[2][flowCursor];
  flowWindow[0][flowCursor]=incoming;flowWindow[1][flowCursor]=outgoing;flowWindow[2][flowCursor]=loss;flowCursor=(flowCursor+1)%20;
  if(processes()){
   ItemStack result=outputForRecipe();String key=result==null?recipeKey:cachedRecipe==null?net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(items.get(0).getItem()).toString():cachedRecipe.id().toString();
   if(!key.equals(recipeKey)){progress=0;recipeKey=key;}
   if(result!=null){
    ItemStack battery=items.get(batterySlot());long deficit=Math.max(0,Energy.SMELT_TICK-energy);
    long from=Math.min(deficit,Math.min(Energy.BATTERY_RATE,BatteryItem.charge(battery)));
    if(from>0){BatteryItem.setCharge(battery,BatteryItem.charge(battery)-from);energy+=from;}
    if(energy>=Energy.SMELT_TICK){energy-=Energy.SMELT_TICK;progress++;working=true;setChanged();
     if(progress>=processTicks()){if(items.get(1).isEmpty())items.set(1,result.copy());else items.get(1).grow(result.getCount());items.get(0).shrink(1);progress=0;}
    }
   }else if(items.get(0).isEmpty())progress=0;
  }else if(kind==MachineKind.GENERATOR){
   var b=items.get(batterySlot());long n=Math.min(energy,Math.min(Energy.BATTERY_RATE,Energy.BATTERY-BatteryItem.charge(b)));
   if(b.getItem() instanceof BatteryItem && n>0){energy-=n;BatteryItem.setCharge(b,BatteryItem.charge(b)+n);setChanged();}
  }
  int history=(flowCursor+19)%20;
  workSum+=(working?1:0)-workHistory[history];workHistory[history]=working?1:0;
  demandSum+=(wantsWork?1:0)-demandHistory[history];demandHistory[history]=wantsWork?1:0;
  visualActive=workSum>0;
  indicator=visualActive||stored()>0?1:0;
  if(processes() && wantsWork && (visualActive||stored()>0||BatteryItem.charge(items.get(batterySlot()))>0))
   indicator=demandSum>workSum+1 || !visualActive?2:1;
  if(level instanceof ServerLevel w){
   // Smooth brownouts instead of rebuilding chunk meshes on every alternate powered tick.
   BlockState s=getBlockState();if(w.getGameTime()%5==0 && (s.getValue(MachineBlock.ACTIVE)!=visualActive || s.getValue(MachineBlock.STATUS)!=indicator))
    w.setBlock(worldPosition,s.setValue(MachineBlock.ACTIVE,visualActive).setValue(MachineBlock.STATUS,indicator),Block.UPDATE_CLIENTS);
   if(w.getGameTime()%10==0){long stamp=visualStamp();if(stamp!=lastSent){lastSent=stamp;w.getChunkSource().blockChanged(worldPosition);w.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}}
  }
 }
 private long visualStamp(){long h=progress/10;for(var s:items)h=h*31+(s.isEmpty()?0:Objects.hash(s.getItem(),s.getCount(),BatteryItem.charge(s)*100/Energy.BATTERY));return h;}
 public String statusKey(){if(kind==MachineKind.GENERATOR)return working?"generating":fuelTicks>0||isFuel(items.get(0))?"full":"no_fuel";
  if(kind==MachineKind.CABINET)return capacity()==0?"no_batteries":"storage";
  if(items.get(0).isEmpty())return "no_input";if(outputForRecipe()==null)return "no_recipe_or_full";return working?(kind==MachineKind.CRUSHER?"crushing":"smelting"):"no_power";}
 public static boolean isFuel(ItemStack s){return s.is(Items.COAL)||s.is(Items.CHARCOAL)||s.is(Items.COAL_BLOCK);}
 public MachineState portable(){var batteries=new ArrayList<ItemStack>();if(kind==MachineKind.CABINET)batteries.addAll(items);else batteries.add(items.get(batterySlot()));return new MachineState(energy,fuelTicks,batteries,fuelTotalTicks);}
 public ItemStack dropStack(){var s=new ItemStack(Machines.ITEMS.get(kind));s.set(Machines.STATE,portable());return s;}
 public void restore(MachineState s){
  energy=kind==MachineKind.CABINET?0:Energy.clamp(s.energy(),Energy.BUFFER);fuelTicks=kind==MachineKind.GENERATOR?Math.clamp(s.fuelTicks(),0,9000):0;progress=0;
  fuelTotalTicks=s.fuelTotalTicks()==9000||fuelTicks>900?9000:900;
  int count=kind==MachineKind.CABINET?8:1;for(int i=0;i<Math.min(count,s.batteries().size());i++)if(s.batteries().get(i).getItem() instanceof BatteryItem){var b=s.batteries().get(i).copyWithCount(1);BatteryItem.setCharge(b,BatteryItem.charge(b));items.set(kind==MachineKind.CABINET?i:batterySlot(),b);}setChanged();
 }
 @Override public void preRemoveSideEffects(BlockPos p,BlockState replacement){
  if(!removedContents && level instanceof ServerLevel w){removedContents=true;
   if(kind!=MachineKind.CABINET)for(int i=0;i<items.size();i++)if(i!=batterySlot()){Block.popResource(w,worldPosition,items.get(i));items.set(i,ItemStack.EMPTY);}
   EnergyNetwork.invalidate(w);
   if(kind==MachineKind.CABINET && w.getBlockState(worldPosition.above()).is(Machines.BLOCKS.get(kind)) && w.getBlockState(worldPosition.above()).getValue(MachineBlock.UPPER))w.removeBlock(worldPosition.above(),false);
  }
  // Do not call Container's default spill: batteries are preserved inside the machine item.
 }
 @Override public void setRemoved(){if(level!=null)EnergyNetwork.invalidate(level);super.setRemoved();}
 @Override public void loadAdditional(ValueInput i){super.loadAdditional(i);energy=Energy.clamp(i.getLongOr("energy",0),Energy.BUFFER);fuelTicks=Math.clamp(i.getIntOr("fuel",0),0,9000);fuelTotalTicks=i.getIntOr("fuel_total",fuelTicks>900?9000:900);progress=Math.clamp(i.getIntOr("progress",0),0,processTicks()-1);recipeKey=i.getStringOr("recipe","");
  var saved=i.read("items",ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());for(int k=0;k<items.size();k++)items.set(k,k<saved.size()?saved.get(k):ItemStack.EMPTY);
 }
 @Override public void saveAdditional(ValueOutput o){super.saveAdditional(o);o.putLong("energy",energy);o.putInt("fuel",fuelTicks);o.putInt("fuel_total",fuelTotalTicks);o.putInt("progress",progress);o.putString("recipe",recipeKey);o.store("items",ItemStack.OPTIONAL_CODEC.listOf(),items);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveCustomOnly(r);}
 @Override public int getContainerSize(){return items.size();}
 @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
 @Override public ItemStack getItem(int i){return items.get(i);}
 @Override public ItemStack removeItem(int i,int n){var s=ContainerHelper.removeItem(items,i,n);if(!s.isEmpty())setChanged();return s;}
 @Override public ItemStack removeItemNoUpdate(int i){return ContainerHelper.takeItem(items,i);}
 @Override public void setItem(int i,ItemStack s){items.set(i,s);s.limitSize(s.getItem() instanceof BatteryItem?1:s.getMaxStackSize());setChanged();}
 @Override public boolean canPlaceItem(int slot,ItemStack s){
  if(kind==MachineKind.CABINET)return s.getItem() instanceof BatteryItem;
  if(slot==batterySlot())return s.getItem() instanceof BatteryItem;
  return slot==0 && (processes() || isFuel(s));
 }
 @Override public void clearContent(){items.clear();setChanged();}
 @Override public boolean stillValid(Player p){return level!=null && !isRemoved() && p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<64;}
 @Override public Component getDisplayName(){return Component.translatable("block.techmod."+kind.id);}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new MachineMenu(kind,id,inv,this);}
}
