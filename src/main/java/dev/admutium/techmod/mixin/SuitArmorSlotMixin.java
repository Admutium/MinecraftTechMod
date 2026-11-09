package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.*;import net.minecraft.world.inventory.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ArmorSlot.class) public abstract class SuitArmorSlotMixin extends Slot {
 public SuitArmorSlotMixin(net.minecraft.world.Container c,int i,int x,int y){super(c,i,x,y);}
 @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true) private void place(ItemStack s,CallbackInfoReturnable<Boolean> cir){if(s.is(Suits.ITEM)||container instanceof net.minecraft.world.entity.player.Inventory inv&&!Suits.worn(inv.player).isEmpty())cir.setReturnValue(false);}
 @Inject(method="mayPickup",at=@At("HEAD"),cancellable=true) private void pickup(Player p,CallbackInfoReturnable<Boolean> cir){if(getItem().is(Suits.ITEM))cir.setReturnValue(false);}
 @Inject(method="isActive",at=@At("HEAD"),cancellable=true) private void active(CallbackInfoReturnable<Boolean> cir){if(container instanceof net.minecraft.world.entity.player.Inventory inv&&!Suits.worn(inv.player).isEmpty()&&!getItem().is(Suits.ITEM))cir.setReturnValue(false);}
}
