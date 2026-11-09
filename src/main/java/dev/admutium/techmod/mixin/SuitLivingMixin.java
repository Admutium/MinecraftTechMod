package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.ItemStack;import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntity.class) public abstract class SuitLivingMixin {
 @Inject(method="getDamageAfterMagicAbsorb",at=@At("RETURN"),cancellable=true) private void damage(DamageSource source,float amount,CallbackInfoReturnable<Float> cir){if((Object)this instanceof Player p)cir.setReturnValue(SuitRuntime.reduce(p,source,cir.getReturnValue()));}
 @Inject(method="canDispenserEquipIntoSlot",at=@At("HEAD"),cancellable=true) private void dispenser(EquipmentSlot slot,CallbackInfoReturnable<Boolean> cir){if((Object)this instanceof Player p&&!Suits.worn(p).isEmpty())cir.setReturnValue(false);}
 @Inject(method="canGlideUsing",at=@At("HEAD"),cancellable=true) private static void glide(ItemStack stack,EquipmentSlot slot,CallbackInfoReturnable<Boolean> cir){if(stack.is(Suits.ITEM)){var d=stack.getOrDefault(Suits.DATA,SuitData.EMPTY);cir.setReturnValue(d.energy()>0&&d.level(SuitUpgrade.FLIGHT)>=2&&d.on("flight_elytra"));}}
}
