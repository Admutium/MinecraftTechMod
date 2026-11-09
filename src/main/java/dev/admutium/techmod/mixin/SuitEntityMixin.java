package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Entity.class) public abstract class SuitEntityMixin {
 @Inject(method="getMaxAirSupply",at=@At("RETURN"),cancellable=true) private void air(CallbackInfoReturnable<Integer> cir){if((Object)this instanceof Player p){var d=Suits.data(p);if(d.energy()>0)cir.setReturnValue((int)(cir.getReturnValue()*(1+.1*d.level(SuitUpgrade.AIR))));}}
 @Inject(method="isCurrentlyGlowing",at=@At("HEAD"),cancellable=true) private void beacon(CallbackInfoReturnable<Boolean> cir){if((Object)this instanceof Player p){var d=Suits.data(p);if(d.energy()>0&&d.level(SuitUpgrade.BEACON)>0&&d.on("beacon"))cir.setReturnValue(true);}}
}
