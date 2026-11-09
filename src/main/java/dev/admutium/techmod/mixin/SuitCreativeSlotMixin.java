package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.Suits;import net.minecraft.server.level.ServerPlayer;import net.minecraft.server.network.ServerGamePacketListenerImpl;import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerGamePacketListenerImpl.class) public abstract class SuitCreativeSlotMixin {
 @Shadow public ServerPlayer player;
 @Inject(method="handleSetCreativeModeSlot",at=@At("HEAD"),cancellable=true)private void protect(ServerboundSetCreativeModeSlotPacket packet,CallbackInfo ci){if(!player.level().getServer().isSameThread())return;if(packet.slotNum()>=5&&packet.slotNum()<=8&&(!Suits.worn(player).isEmpty()||packet.itemStack().is(Suits.ITEM))){player.inventoryMenu.broadcastFullState();ci.cancel();}}
}
