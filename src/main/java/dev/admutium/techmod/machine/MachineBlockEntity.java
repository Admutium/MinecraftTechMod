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
public final class MachineBlockEntity extends dev.admutium.techmod.cable.CableBlockEntity implements WorldlyContainer,MenuProvider {
 private final NonNullList<ItemStack> items;
 public final MachineKind kind;
 public long suitChargeRate;public final long[] suitChargeWindow=new long[20];public int suitChargeCursor;
 public long energy,incoming,outgoing,loss,displayIn,displayOut,displayLoss;
 private final long[][] flowWindow=new long[3][20];private int flowCursor;
 public int fuelTicks,fuelTotalTicks,progress,indicator;public boolean working,wantsWork,visualActive;
 private final int[] workHistory=new int[20],demandHistory=new int[20];private int workSum,demandSum;
 private long lastSent=-1;private String recipeKey="";private boolean removedContents;
 private int batteryCursor;
 private RecipeHolder<SmeltingRecipe> cachedRecipe;
 public MachineBlockEntity(BlockPos p,BlockState s){super(Machines.ENTITY,p,s);kind=((MachineBlock)s.getBlock()).kind;items=NonNullList.withSize(kind.slots,ItemStack.EMPTY);java.util.Arrays.fill(itemSides,0);itemSides[Direction.UP.ordinal()]=1;itemSides[Direction.DOWN.ordinal()]=2;var front=s.getValue(MachineBlock.FACING);itemSides[front.ordinal()]=2;itemSides[front.getClockWise().ordinal()]=1;itemSides[front.getCounterClockWise().ordinal()]=1;}
 public int driveLoad,driveMode;
 public final int[] itemSides={2,1,0,0,1,1};
 public int solarEfficiency,solarRate,solarStatus;private long nextSkyCheck;private boolean solarSkyOpen;
 public boolean generates(){return kind==MachineKind.GENERATOR||kind==MachineKind.SOLAR||kind==MachineKind.CREATIVE_SOURCE;}
 public boolean processes(){return kind==MachineKind.FURNACE||kind==MachineKind.CRUSHER||kind==MachineKind.PRESS||kind==MachineKind.CUTTER;}
 public int processTicks(){return kind==MachineKind.CRUSHER?120:kind==MachineKind.PRESS||kind==MachineKind.CUTTER?80:160;}
 public static int fuelDuration(ItemStack s){return s.is(Items.COAL_BLOCK)?9000:isFuel(s)?900:0;}
 public int batterySlot(){if(kind.slots==0)return -1;return kind==MachineKind.GENERATOR?1:2;}
 public long stored(){if(kind!=MachineKind.CABINET)return energy;long sum=0;for(var s:items)sum+=BatteryItem.charge(s);return sum;}
 public long capacity(){if(kind==MachineKind.STATION)return 4096L*Energy.UNIT;if(kind==MachineKind.CREATIVE_SOURCE)return 100000L*Energy.UNIT;if(kind!=MachineKind.CABINET)return kind==MachineKind.SOLAR||kind==MachineKind.DRIVE?SolarPower.CAPACITY:Energy.BUFFER;return items.stream().filter(s->s.getItem() instanceof BatteryItem).count()*Energy.BATTERY;}
 public long receive(long amount){
  amount=Math.max(0,Math.min(amount,capacity()-stored()));long left=amount;
  if(kind!=MachineKind.CABINET){energy+=amount;return amount;}
  while(left>0){int index=-1;long lowest=Long.MAX_VALUE;for(int k=0;k<8;k++){int i=(batteryCursor+k)%8;long e=BatteryItem.charge(items.get(i));if(items.get(i).getItem() instanceof BatteryItem && e<Energy.BATTERY && e<lowest){index=i;lowest=e;}}
   if(index<0)break;long n=Math.min(left,Math.min(Energy.BATTERY-lowest,Math.max(1,(left+7)/8)));BatteryItem.setCharge(items.get(index),lowest+n);left-=n;batteryCursor=(index+1)%8;
  }return amount-left;
 }
 public long portLimit(){if(kind==MachineKind.STATION)return 512L*Energy.UNIT/20;if(kind==MachineKind.CREATIVE_SOURCE)return 8192L*Energy.UNIT/20;if(kind==MachineKind.SOLAR)return SolarPower.PORT_RATE;return kind==MachineKind.CABINET?Math.min(Energy.PORT_RATE,items.stream().filter(s->s.getItem() instanceof BatteryItem).count()*Energy.BATTERY_RATE):Energy.PORT_RATE;}
 public long extract(long amount){
  amount=Math.max(0,Math.min(amount,stored()));long left=amount;
  if(kind!=MachineKind.CABINET){energy-=amount;return amount;}
  while(left>0){int index=-1;long highest=0;for(int k=0;k<8;k++){int i=(batteryCursor+k)%8;long e=BatteryItem.charge(items.get(i));if(e>highest){index=i;highest=e;}}
   if(index<0)break;long n=Math.min(left,Math.min(highest,Math.max(1,(left+7)/8)));BatteryItem.setCharge(items.get(index),highest-n);left-=n;batteryCursor=(index+1)%8;
  }return amount-left;
 }
 public void beginTick(){
  if(kind==MachineKind.CABINET && level instanceof ServerLevel w){var above=w.getBlockState(worldPosition.above());if(above.is(Machines.BLOCKS.get(kind)) && above.getValue(MachineBlock.UPPER))w.removeBlock(worldPosition.above(),false);}
  if(kind==MachineKind.DRIVE&&level instanceof ServerLevel server)dev.admutium.techmod.conveyor.ConveyorNetwork.registerDrive(server,this);
  incoming=outgoing=loss=0;working=false;wantsWork=false;
  if(kind==MachineKind.CREATIVE_SOURCE){energy=capacity();working=true;}
  if(kind==MachineKind.SOLAR&&level instanceof ServerLevel w){
   if(w.getGameTime()>=nextSkyCheck){solarSkyOpen=SolarPower.skyOpen(w,worldPosition);nextSkyCheck=w.getGameTime()+20;}
   if(w.getGameTime()%20==0)dev.admutium.techmod.cable.CableWorld.validate(w,worldPosition,this);
   boolean sun=SolarPower.hasSun(w);double daylight=SolarPower.efficiency(w.getOverworldClockTime(),1);
   double efficiency=sun&&solarSkyOpen?daylight*SolarPower.weather(w,worldPosition):0;
   solarEfficiency=(int)Math.round(efficiency*10000);long potential=Math.round(efficiency*SolarPower.MAX_PER_TICK);
   long produced=Math.min(capacity()-energy,potential);energy+=produced;solarRate=(int)(produced*20);working=produced>0;
   solarStatus=!sun?1:!solarSkyOpen?2:daylight<.0001?3:energy>=capacity()?4:0;
   if(produced>0)setChanged();
  }
  if(kind==MachineKind.GENERATOR && Energy.BUFFER-energy>=Energy.GENERATION){
   if(fuelTicks==0 && isFuel(items.get(0))){fuelTicks=fuelTotalTicks=fuelDuration(items.get(0));items.get(0).shrink(1);}
   if(fuelTicks>0){energy+=Energy.GENERATION;fuelTicks--;working=true;setChanged();}
  }
  if(processes())wantsWork=outputForRecipe()!=null;
 }
 private boolean fits(ItemStack result){var out=items.get(1);return !result.isEmpty() && (out.isEmpty() || ItemStack.isSameItemSameComponents(out,result)&&out.getCount()+result.getCount()<=out.getMaxStackSize());}
 private ItemStack outputForRecipe(){
  if(!(level instanceof ServerLevel w) || items.get(0).isEmpty()){cachedRecipe=null;return null;}
  Item special=kind==MachineKind.CRUSHER?Machines.CRUSHED.get(items.get(0).getItem()):kind==MachineKind.CUTTER?Machines.CUT.get(items.get(0).getItem()):kind==MachineKind.PRESS?Machines.PLATES.get(items.get(0).getItem()):Machines.SMELTED.get(items.get(0).getItem());
  if(kind==MachineKind.CRUSHER || kind==MachineKind.PRESS || kind==MachineKind.CUTTER || special!=null){cachedRecipe=null;ItemStack result=special==null?ItemStack.EMPTY:new ItemStack(special,kind==MachineKind.CRUSHER?2:kind==MachineKind.CUTTER?4:1);return fits(result)?result:null;}
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
  if(kind==MachineKind.STATION)dev.admutium.techmod.suit.Suits.stationTick(this);
  if(kind==MachineKind.DRIVE){if(level instanceof ServerLevel w&&w.getGameTime()%10==0)w.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),2);return;}
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
  indicator=kind==MachineKind.SOLAR?(visualActive?1:0):visualActive||stored()>0?1:0;
  if(processes() && wantsWork && (visualActive||stored()>0||BatteryItem.charge(items.get(batterySlot()))>0))
   indicator=demandSum>workSum+1 || !visualActive?2:1;
  if(level instanceof ServerLevel w){
   // Smooth brownouts instead of rebuilding chunk meshes on every alternate powered tick.
   BlockState s=getBlockState();if(w.getGameTime()%5==0 && (s.getValue(MachineBlock.ACTIVE)!=visualActive || s.getValue(MachineBlock.STATUS)!=indicator))
    w.setBlock(worldPosition,s.setValue(MachineBlock.ACTIVE,visualActive).setValue(MachineBlock.STATUS,indicator),Block.UPDATE_CLIENTS);
   if(w.getGameTime()%10==0){long stamp=visualStamp();if(stamp!=lastSent){lastSent=stamp;w.getChunkSource().blockChanged(worldPosition);w.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);}}
  }
 }
 private long visualStamp(){long h=progress/10+energy/Energy.UNIT+displayIn+displayOut;for(var s:items)h=h*31+(s.isEmpty()?0:Objects.hash(s.getItem(),s.getCount(),BatteryItem.charge(s)*100/Energy.BATTERY));return h;}
 public String statusKey(){if(kind==MachineKind.STATION)return "storage";if(kind==MachineKind.CREATIVE_SOURCE)return "generating";if(kind==MachineKind.DRIVE)return "drive";if(kind==MachineKind.SOLAR)return solarStatusKey(solarStatus);if(kind==MachineKind.GENERATOR)return working?"generating":fuelTicks>0||isFuel(items.get(0))?"full":"no_fuel";
  if(kind==MachineKind.CABINET)return capacity()==0?"no_batteries":"storage";
  if(items.get(0).isEmpty())return "no_input";if(outputForRecipe()==null)return "no_recipe_or_full";return working?(kind==MachineKind.CRUSHER?"crushing":kind==MachineKind.PRESS?"pressing":kind==MachineKind.CUTTER?"cutting":"smelting"):"no_power";}
 public static String solarStatusKey(int status){return switch(status){case 1->"no_sun";case 2->"shaded";case 3->"night";case 4->"full";default->"solar_generating";};}
 public static boolean isFuel(ItemStack s){return s.is(Items.COAL)||s.is(Items.CHARCOAL)||s.is(Items.COAL_BLOCK);}
 public MachineState portable(){var batteries=new ArrayList<ItemStack>();if(kind==MachineKind.CABINET)batteries.addAll(items);else if(batterySlot()>=0)batteries.add(items.get(batterySlot()));return new MachineState(energy,fuelTicks,batteries,fuelTotalTicks);}
 public ItemStack dropStack(){var s=new ItemStack(Machines.ITEMS.get(kind));s.set(Machines.STATE,portable());return s;}
 public void restore(MachineState s){
  energy=kind==MachineKind.CABINET?0:Energy.clamp(s.energy(),capacity());fuelTicks=kind==MachineKind.GENERATOR?Math.clamp(s.fuelTicks(),0,9000):0;progress=0;
  fuelTotalTicks=s.fuelTotalTicks()==9000||fuelTicks>900?9000:900;
  int count=kind==MachineKind.CABINET?8:kind.slots==0?0:1;for(int i=0;i<Math.min(count,s.batteries().size());i++)if(s.batteries().get(i).getItem() instanceof BatteryItem){var b=s.batteries().get(i).copyWithCount(1);BatteryItem.setCharge(b,BatteryItem.charge(b));items.set(kind==MachineKind.CABINET?i:batterySlot(),b);}setChanged();
 }
 @Override public void preRemoveSideEffects(BlockPos p,BlockState replacement){
  if(!removedContents && level instanceof ServerLevel w){removedContents=true;
   if(kind==MachineKind.SOLAR)SolarBlock.detach(w,worldPosition,snapshot());
   if(kind!=MachineKind.CABINET)for(int i=0;i<items.size();i++)if(i!=batterySlot()){Block.popResource(w,worldPosition,items.get(i));items.set(i,ItemStack.EMPTY);}
   EnergyNetwork.invalidate(w);
   if(kind==MachineKind.CABINET && w.getBlockState(worldPosition.above()).is(Machines.BLOCKS.get(kind)) && w.getBlockState(worldPosition.above()).getValue(MachineBlock.UPPER))w.removeBlock(worldPosition.above(),false);
  }
  // Do not call Container's default spill: batteries are preserved inside the machine item.
 }
 @Override public void setRemoved(){if(level!=null)EnergyNetwork.invalidate(level);super.setRemoved();}
 @Override public void loadAdditional(ValueInput i){super.loadAdditional(i);displayIn=i.getLongOr("display_in",0);displayOut=i.getLongOr("display_out",0);driveMode=Math.clamp(i.getIntOr("drive_mode",0),0,2);driveLoad=Math.clamp(i.getIntOr("drive_load",0),0,16);var sides=i.read("item_sides",com.mojang.serialization.Codec.INT.listOf()).orElse(java.util.Arrays.stream(itemSides).boxed().toList());for(int k=0;k<6;k++)itemSides[k]=k<sides.size()?Math.clamp(sides.get(k),0,2):0;energy=Energy.clamp(i.getLongOr("energy",0),capacity());fuelTicks=Math.clamp(i.getIntOr("fuel",0),0,9000);fuelTotalTicks=i.getIntOr("fuel_total",fuelTicks>900?9000:900);progress=Math.clamp(i.getIntOr("progress",0),0,processTicks()-1);recipeKey=i.getStringOr("recipe","");
  var saved=i.read("items",ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());for(int k=0;k<items.size();k++)items.set(k,k<saved.size()?saved.get(k):ItemStack.EMPTY);
 }
 @Override public void saveAdditional(ValueOutput o){super.saveAdditional(o);o.putLong("energy",energy);o.putInt("drive_mode",driveMode);o.putInt("drive_load",driveLoad);o.store("item_sides",com.mojang.serialization.Codec.INT.listOf(),java.util.Arrays.stream(itemSides).boxed().toList());o.putInt("fuel",fuelTicks);o.putInt("fuel_total",fuelTotalTicks);o.putInt("progress",progress);o.putString("recipe",recipeKey);o.store("items",ItemStack.OPTIONAL_CODEC.listOf(),items);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var tag=saveCustomOnly(r);tag.putLong("display_in",displayIn);tag.putLong("display_out",displayOut);return tag;}
 @Override public int getContainerSize(){return items.size();}
 @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
 @Override public ItemStack getItem(int i){return items.get(i);}
 @Override public ItemStack removeItem(int i,int n){var s=ContainerHelper.removeItem(items,i,n);if(!s.isEmpty())setChanged();return s;}
 @Override public ItemStack removeItemNoUpdate(int i){return ContainerHelper.takeItem(items,i);}
 @Override public void setItem(int i,ItemStack s){items.set(i,s);s.limitSize(s.getItem() instanceof BatteryItem?1:s.getMaxStackSize());setChanged();}
 @Override public boolean canPlaceItem(int slot,ItemStack s){
  if(kind.slots==0)return false;
  if(kind==MachineKind.STATION)return slot==0?s.is(dev.admutium.techmod.suit.Suits.ITEM):slot==2&&s.getItem() instanceof BatteryItem;
  if(kind==MachineKind.CABINET)return s.getItem() instanceof BatteryItem;
  if(slot==batterySlot())return s.getItem() instanceof BatteryItem;
  return slot==0 && (processes() || isFuel(s));
 }
 // Native hoppers and other sided inventory users share the same policy as manipulators.
 @Override public int[] getSlotsForFace(Direction face){
  if(!processes()&&kind!=MachineKind.GENERATOR)return new int[0];
  if(face==Direction.DOWN)return processes()?new int[]{1}:new int[0];
  return itemSides[face.ordinal()]==1?new int[]{0}:itemSides[face.ordinal()]==2&&processes()?new int[]{1}:new int[0];
 }
 @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction face){return face!=Direction.DOWN&&itemSides[face.ordinal()]==1&&slot==0&&canPlaceItem(slot,stack);}
 @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction face){return processes()&&slot==1&&(face==Direction.DOWN||itemSides[face.ordinal()]==2);}
 @Override public void clearContent(){items.clear();setChanged();}
 @Override public boolean stillValid(Player p){return level!=null && !isRemoved() && p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<64;}
 @Override public Component getDisplayName(){return Component.translatable("block.techmod."+kind.id);}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return kind==MachineKind.STATION?new dev.admutium.techmod.suit.SuitStationMenu(id,inv,this):new MachineMenu(kind,id,inv,this);}
}
