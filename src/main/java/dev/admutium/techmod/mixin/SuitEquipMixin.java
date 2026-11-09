package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.Suits;import net.minecraft.world.item.equipment.Equippable;import net.minecraft.world.item.ItemStack;import net.minecraft.world.entity.player.Player;import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Equippable.class) public abstract class SuitEquipMixin {
 @Inject(method="swapWithEquipmentSlot",at=@At("HEAD"),cancellable=true) private void deny(ItemStack item,Player p,CallbackInfoReturnable<InteractionResult> cir){if(item.is(Suits.ITEM)||!Suits.worn(p).isEmpty())cir.setReturnValue(InteractionResult.FAIL);}
}
