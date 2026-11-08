package dev.admutium.techmod.conveyor;
import dev.admutium.techmod.TechMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.*;
public record ConveyorModePayload(int mode) implements CustomPacketPayload {
 public static final Type<ConveyorModePayload> TYPE=new Type<>(TechMod.id("conveyor_mode"));
 public static final StreamCodec<RegistryFriendlyByteBuf,ConveyorModePayload> CODEC=new StreamCodec<>(){
  public ConveyorModePayload decode(RegistryFriendlyByteBuf b){return new ConveyorModePayload(b.readVarInt());}
  public void encode(RegistryFriendlyByteBuf b,ConveyorModePayload p){b.writeVarInt(p.mode);}
 };
 @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void init(){PayloadTypeRegistry.serverboundPlay().register(TYPE,CODEC);ServerPlayNetworking.registerGlobalReceiver(TYPE,(p,c)->c.server().execute(()->{
  var player=c.player();if(p.mode>=-1&&p.mode<=1)ConveyorItem.setMode(player,p.mode);
 }));}
}
