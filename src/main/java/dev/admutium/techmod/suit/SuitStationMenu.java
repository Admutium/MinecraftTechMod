package dev.admutium.techmod.suit;
import dev.admutium.techmod.machine.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.core.GlobalPos;
public final class SuitStationMenu extends AbstractContainerMenu {
 public final Container bay;public final MachineBlockEntity station;public final Player owner;
 public SuitStationMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(3),null);}
 public SuitStationMenu(int id,Inventory inv,MachineBlockEntity station){this(id,inv,station,station);}
 private SuitStationMenu(int id,Inventory inv,Container bay,MachineBlockEntity station){super(Suits.STATION_MENU,id);this.bay=bay;this.station=station;owner=inv.player;addSlot(new Slot(bay,0,12,30){public boolean mayPlace(ItemStack s){return s.is(Suits.ITEM);}});addStandardInventorySlots(inv,12,146);}
 public SuitSession subject(){var stack=bay.getItem(0);return stack.is(Suits.ITEM)?new SuitSession(stack):Suits.session(owner);}
 @Override public boolean stillValid(Player p){return station==null||station.stillValid(p);}
 @Override public ItemStack quickMoveStack(Player p,int i){var slot=slots.get(i);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(i==0){if(!moveItemStackTo(s,1,slots.size(),true))return ItemStack.EMPTY;}else if(!s.is(Suits.ITEM)||!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
 public void action(String key,String value){if(station==null||!stillValid(owner))return;
  if(key.equals("create")){if(bay.getItem(0).isEmpty()){var stack=new ItemStack(Suits.ITEM);Suits.ensure(stack);bay.setItem(0,stack);}return;}
  if(key.equals("equip")){if(!station.getLevel().getBlockState(station.getBlockPos().above()).getCollisionShape(station.getLevel(),station.getBlockPos().above()).isEmpty()){Suits.message(owner,"Нужно свободное место над станцией");return;}var stack=bay.getItem(0);if(!stack.is(Suits.ITEM)||Suits.ensure(stack).sealed())return;for(var slot:EquipmentSlot.values())if(slot.isArmor()&&!owner.getItemBySlot(slot).isEmpty()){Suits.message(owner,"Снимите всю броню перед экипировкой");return;}var s=new SuitSession(stack);s.home=java.util.Optional.of(GlobalPos.of(owner.level().dimension(),station.getBlockPos()));s.flush();bay.setItem(0,ItemStack.EMPTY);owner.setItemSlot(EquipmentSlot.CHEST,stack);syncEquipment();return;}
  if(key.equals("remove")){var s=Suits.session(owner);if(s!=null&&bay.getItem(0).isEmpty()){s.flush();SuitRuntime.clear(owner,s);owner.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);Suits.SESSIONS.remove(owner.getUUID());bay.setItem(0,s.stack);syncEquipment();}return;}
  var s=subject();if(s==null)return;
  if(key.equals("all")){for(var u:SuitUpgrade.values()){if(s.level(u)==0)s.unlocked(u);s.levels.put(u.id(),u.max);}}
  else if(key.equals("upgrade")){var u=SuitUpgrade.byId(value);if(u!=null)s.upgrade(u);}
  else if(key.equals("color")){try{s.tint=0xff000000|Integer.parseInt(value.replace("#",""),16)&0xffffff;}catch(NumberFormatException ignored){}}
  else if(key.equals("range")){try{s.magnetRange=Math.clamp(Integer.parseInt(value),0,3);}catch(NumberFormatException ignored){}}
  else if(key.equals("blacklist")&&s.level(SuitUpgrade.BLACKLIST)>0)edit(s.blacklist,value);
  else if(key.equals("ores")&&s.level(SuitUpgrade.ORE_SCAN)>0)edit(s.ores,value);
  else if(key.equals("recover")&&s.sealed){for(var item:s.salvage)owner.getInventory().add(item);s.salvage.removeIf(ItemStack::isEmpty);if(s.salvage.isEmpty())s.sealed=false;}
  else if(key.equals("feed")){int room=32*s.level(SuitUpgrade.NUTRIENTS)-s.nutrients;for(int i=0;i<owner.getInventory().getContainerSize()&&room>0;i++){var item=owner.getInventory().getItem(i);if(item.is(Suits.NUTRIENT)){int n=Math.min(room,item.getCount());item.shrink(n);s.nutrients+=n;room-=n;}}}
  s.flush();bay.setChanged();
 }
 private void syncEquipment(){if(owner instanceof net.minecraft.server.level.ServerPlayer p)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket(p.getId(),java.util.List.of(com.mojang.datafixers.util.Pair.of(EquipmentSlot.CHEST,p.getItemBySlot(EquipmentSlot.CHEST).copy()))));}
 private void edit(java.util.Set<String> set,String value){var id=net.minecraft.resources.Identifier.tryParse(value);if(id==null)return;if(!set.remove(id.toString())&&set.size()<64)set.add(id.toString());}
}
