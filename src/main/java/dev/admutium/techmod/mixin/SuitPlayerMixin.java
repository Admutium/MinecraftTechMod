package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.*;
import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Player.class) public abstract class SuitPlayerMixin {
 @Inject(method="getDestroySpeed",at=@At("RETURN"),cancellable=true) private void mining(BlockState state,CallbackInfoReturnable<Float> cir){cir.setReturnValue(SuitRuntime.mining((Player)(Object)this,cir.getReturnValue()));}
}
