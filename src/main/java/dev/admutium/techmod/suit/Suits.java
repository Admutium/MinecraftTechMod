package dev.admutium.techmod.suit;
import dev.admutium.techmod.TechMod;
import dev.admutium.techmod.energy.Energy;
import dev.admutium.techmod.machine.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.inventory.*;
import net.minecraft.world.flag.FeatureFlags;
import java.util.*;
public final class Suits {
 public static final DataComponentType<SuitData> DATA=DataComponentType.<SuitData>builder().persistent(SuitData.CODEC).build();
 public static final Item ITEM=new SuitItem(props("kontur_suit").component(DataComponents.EQUIPPABLE,Equippable.builder(EquipmentSlot.CHEST).setDispensable(false).setSwappable(false).setEquipOnInteract(false).setDamageOnHurt(false).build()).component(DataComponents.GLIDER,net.minecraft.util.Unit.INSTANCE));
 public static final Item NUTRIENT=new Item(props("nutrient_capsule"));
 public static final MenuType<SuitStationMenu> STATION_MENU=new MenuType<>(SuitStationMenu::new,FeatureFlags.VANILLA_SET);
 public static final MenuType<SuitInventoryMenu> INVENTORY_MENU=new MenuType<>(SuitInventoryMenu::new,FeatureFlags.VANILLA_SET);
 public static final net.minecraft.world.entity.EntityType<SuitRemains> REMAINS=net.minecraft.world.entity.EntityType.Builder.<SuitRemains>of(SuitRemains::new,net.minecraft.world.entity.MobCategory.MISC).sized(.8f,.45f).fireImmune().clientTrackingRange(10).updateInterval(3).build(ResourceKey.create(Registries.ENTITY_TYPE,TechMod.id("suit_remains")));
 public static final Map<UUID,SuitSession> SESSIONS=new HashMap<>();
 public static java.util.function.Function<Player,SuitData> clientData=p->worn(p).getOrDefault(DATA,SuitData.EMPTY);
 static Item.Properties props(String id){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,TechMod.id(id)));}
 // TODO(crafting): replace free prototype assembly and upgrade buttons with the agreed material progression.
 public static void init(){Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,TechMod.id("suit_data"),DATA);Registry.register(BuiltInRegistries.ITEM,TechMod.id("kontur_suit"),ITEM);Registry.register(BuiltInRegistries.ITEM,TechMod.id("nutrient_capsule"),NUTRIENT);Registry.register(BuiltInRegistries.MENU,TechMod.id("suit_service"),STATION_MENU);Registry.register(BuiltInRegistries.MENU,TechMod.id("suit_inventory"),INVENTORY_MENU);Registry.register(BuiltInRegistries.ENTITY_TYPE,TechMod.id("suit_remains"),REMAINS);SuitPackets.init();SuitRuntime.init();net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server->{SESSIONS.values().forEach(SuitSession::flush);SESSIONS.clear();});ServerTickEvents.END_SERVER_TICK.register(server->{for(var player:server.getPlayerList().getPlayers())SuitRuntime.tick(player);});ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{var s=SESSIONS.remove(handler.player.getUUID());if(s!=null){SuitRuntime.clear(handler.player,s);s.flush();}});}
 public static ItemStack worn(Player p){if(p.getInventory()==null)return ItemStack.EMPTY;var s=p.getItemBySlot(EquipmentSlot.CHEST);return s.is(ITEM)?s:ItemStack.EMPTY;}
 public static SuitData ensure(ItemStack s){var d=s.get(DATA);if(d==null||d.id().equals(SuitData.EMPTY_ID)){d=SuitData.fresh();s.set(DATA,d);}return d;}
 public static SuitSession session(Player p){var stack=worn(p);var old=SESSIONS.get(p.getUUID());if(stack.isEmpty()){if(old!=null){old.flush();SuitRuntime.clear(p,old);SESSIONS.remove(p.getUUID());}return null;}if(old==null||old.stack!=stack){if(old!=null){old.flush();SuitRuntime.clear(p,old);}old=new SuitSession(stack);SESSIONS.put(p.getUUID(),old);}return old;}
 public static SuitData data(Player p){if(p.level().isClientSide())return clientData.apply(p);var s=session(p);return s==null?SuitData.EMPTY:s.snapshot();}
 public static void stationTick(MachineBlockEntity station){if(!(station.getLevel() instanceof net.minecraft.server.level.ServerLevel level))return;long charged=0;var stack=station.getItem(0);if(stack.is(ITEM)){var s=new SuitSession(stack);charged+=charge(station,s);s.flush();}for(var p:level.getEntitiesOfClass(ServerPlayer.class,new net.minecraft.world.phys.AABB(station.getBlockPos()).inflate(1.5))){var s=session(p);if(s!=null){charged+=charge(station,s);s.flush();}}station.suitChargeRate+=charged-station.suitChargeWindow[station.suitChargeCursor];station.suitChargeWindow[station.suitChargeCursor]=charged;station.suitChargeCursor=(station.suitChargeCursor+1)%20;}
 private static long charge(MachineBlockEntity be,SuitSession s){long amount=Math.min(s.capacity()-s.energy,Math.min(be.energy,(64L+64*s.level(SuitUpgrade.CHARGE))*Energy.UNIT/20));if(amount>0){be.energy-=amount;s.energy+=amount;s.chargeTick+=amount;be.working=true;be.setChanged();}return Math.max(0,amount);}
 public static void message(Player p,String text){p.sendOverlayMessage(net.minecraft.network.chat.Component.literal(text));}
}
