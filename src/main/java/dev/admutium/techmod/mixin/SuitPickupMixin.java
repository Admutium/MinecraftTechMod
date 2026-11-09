package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.*;import net.minecraft.world.entity.item.ItemEntity;import net.minecraft.world.entity.player.*;import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ItemEntity.class) public abstract class SuitPickupMixin {
 @Inject(method="playerTouch",at=@At("HEAD"),cancellable=true) private void filter(Player p,CallbackInfo ci){if(p.level().isClientSide())return;var s=Suits.session(p);if(s!=null&&s.level(SuitUpgrade.BLACKLIST)>0&&s.blocked(((ItemEntity)(Object)this).getItem()))ci.cancel();}
 @Redirect(method="playerTouch",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z")) private boolean cargo(Inventory inv,ItemStack stack){boolean added=inv.add(stack);var s=Suits.session(inv.player);if(s!=null&&!stack.isEmpty()){int n=stack.getCount();s.insertCargo(stack);if(stack.getCount()<n){added=true;s.flush();}}return added;}
}
