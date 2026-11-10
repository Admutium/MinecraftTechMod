package dev.admutium.techmod.suit;
import dev.admutium.techmod.TechMod;
import net.minecraft.network.*;import net.minecraft.network.codec.StreamCodec;import net.minecraft.network.protocol.common.custom.CustomPacketPayload;import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.networking.v1.*;
public final class SuitPackets {
 public record Action(String key,String value,int menu) implements CustomPacketPayload {
  public static final Type<Action> TYPE=new Type<>(TechMod.id("suit_action"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=new StreamCodec<>(){public Action decode(RegistryFriendlyByteBuf b){return new Action(b.readUtf(32),b.readUtf(128),b.readVarInt());}public void encode(RegistryFriendlyByteBuf b,Action p){b.writeUtf(p.key,32);b.writeUtf(p.value,128);b.writeVarInt(p.menu);}};public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record State(ItemStack stack,ItemStack worn,long[] rates,int warp,int menu,long stationEnergy,long stationIn,long charging) implements CustomPacketPayload {
  public static final Type<State> TYPE=new Type<>(TechMod.id("suit_state"));
  public static final StreamCodec<RegistryFriendlyByteBuf,State> CODEC=new StreamCodec<>(){public State decode(RegistryFriendlyByteBuf b){var stack=ItemStack.OPTIONAL_STREAM_CODEC.decode(b);var worn=ItemStack.OPTIONAL_STREAM_CODEC.decode(b);long[] rates=new long[5];for(int i=0;i<5;i++)rates[i]=b.readVarLong();return new State(stack,worn,rates,b.readVarInt(),b.readVarInt(),b.readVarLong(),b.readVarLong(),b.readVarLong());}public void encode(RegistryFriendlyByteBuf b,State p){ItemStack.OPTIONAL_STREAM_CODEC.encode(b,p.stack);ItemStack.OPTIONAL_STREAM_CODEC.encode(b,p.worn);for(int i=0;i<5;i++)b.writeVarLong(p.rates[i]);b.writeVarInt(p.warp);b.writeVarInt(p.menu);b.writeVarLong(p.stationEnergy);b.writeVarLong(p.stationIn);b.writeVarLong(p.charging);}};public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public record Wire(net.minecraft.core.BlockPos pos,long rate) implements CustomPacketPayload {
  public static final Type<Wire> TYPE=new Type<>(TechMod.id("suit_wire"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Wire> CODEC=new StreamCodec<>(){public Wire decode(RegistryFriendlyByteBuf b){return new Wire(b.readBlockPos(),b.readVarLong());}public void encode(RegistryFriendlyByteBuf b,Wire p){b.writeBlockPos(p.pos);b.writeVarLong(p.rate);}};public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static void init(){PayloadTypeRegistry.clientboundPlay().register(Wire.TYPE,Wire.CODEC);PayloadTypeRegistry.serverboundPlay().register(Action.TYPE,Action.CODEC);PayloadTypeRegistry.clientboundPlay().register(State.TYPE,State.CODEC);ServerPlayNetworking.registerGlobalReceiver(Action.TYPE,(a,ctx)->ctx.server().execute(()->{
  var p=ctx.player();var s=Suits.session(p);
  if(a.key.equals("input")){if(s!=null)try{s.input=Integer.parseInt(a.value)&7;}catch(NumberFormatException ignored){}return;}
  if(a.key.equals("inventory")){if(s!=null)p.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,player)->new SuitInventoryMenu(id,inv,s),net.minecraft.network.chat.Component.literal("Инвентарь костюма")));return;}
  if(a.key.equals("toggle")){if(s!=null){SuitRuntime.toggle(p,s,a.value);send(p);}return;}
  if(a.key.equals("flight")){if(s!=null){SuitRuntime.selectFlight(p,s,a.value);send(p);}return;}
  if(p.containerMenu instanceof SuitStationMenu m&&m.containerId==a.menu&&m.stillValid(p)){m.action(a.key,a.value);send(p);}
 }));}
 public static void send(net.minecraft.server.level.ServerPlayer p){var s=Suits.session(p);var worn=net.minecraft.world.item.ItemStack.EMPTY;if(s!=null){worn=s.stack.copy();worn.set(Suits.DATA,s.snapshot());}int menu=-1;long energy=0,input=0,charging=s==null?0:s.chargeRate;if(p.containerMenu instanceof SuitStationMenu m){s=m.subject();menu=m.containerId;energy=m.station.energy;input=m.station.displayIn;charging=m.station.suitChargeRate;}var stack=ItemStack.EMPTY;if(s!=null){stack=s.stack.copy();stack.set(Suits.DATA,s.snapshot());}ServerPlayNetworking.send(p,new State(stack,worn,s==null?new long[5]:s.rates.clone(),s==null?0:s.warpTicks,menu,energy,input,charging));}
}
