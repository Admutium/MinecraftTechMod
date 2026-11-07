package dev.admutium.techmod.conveyor;
import dev.admutium.techmod.TechMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.fabricmc.fabric.api.networking.v1.*;
public record FilterPayload(int menu,int slot,String item) implements CustomPacketPayload {
 public static final Type<FilterPayload> TYPE=new Type<>(TechMod.id("filter"));
 public static final StreamCodec<RegistryFriendlyByteBuf,FilterPayload> CODEC=new StreamCodec<>(){
  public FilterPayload decode(RegistryFriendlyByteBuf b){return new FilterPayload(b.readVarInt(),b.readVarInt(),b.readUtf(128));}
  public void encode(RegistryFriendlyByteBuf b,FilterPayload p){b.writeVarInt(p.menu);b.writeVarInt(p.slot);b.writeUtf(p.item,128);}
 };
 @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 public static void init(){PayloadTypeRegistry.serverboundPlay().register(TYPE,CODEC);ServerPlayNetworking.registerGlobalReceiver(TYPE,(payload,ctx)->ctx.server().execute(()->{
  var player=ctx.player();if(player.containerMenu instanceof ConveyorMenu menu&&menu.containerId==payload.menu&&menu.stillValid(player)&&payload.slot>=0&&payload.slot<9){
   var id=Identifier.tryParse(payload.item);if(id==null)return;var item=BuiltInRegistries.ITEM.getValue(id);if(item!=null&&item!=Items.AIR)menu.sample(payload.slot,new ItemStack(item));
  }
 }));}
}
