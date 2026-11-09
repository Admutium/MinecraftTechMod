package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Mob.class) public abstract class SuitMobMixin {
 @Inject(method="setTarget",at=@At("HEAD"),cancellable=true) private void cloak(LivingEntity target,CallbackInfo ci){if(target instanceof Player p&&SuitRuntime.hidden(p))ci.cancel();}
}
