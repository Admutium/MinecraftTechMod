package dev.admutium.techmod.mixin;
import dev.admutium.techmod.suit.*;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.world.level.gamerules.GameRules;import net.minecraft.world.entity.EquipmentSlot;import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ServerPlayer.class) public abstract class SuitDeathMixin {
 @Inject(method="addAdditionalSaveData",at=@At("HEAD")) private void save(net.minecraft.world.level.storage.ValueOutput out,CallbackInfo ci){var s=Suits.session((ServerPlayer)(Object)this);if(s!=null)s.flush();}
 @Inject(method="die",at=@At("HEAD")) private void remains(DamageSource source,CallbackInfo ci){var p=(ServerPlayer)(Object)this;var s=Suits.session(p);if(s==null)return;s.flush();if(p.level().getGameRules().get(GameRules.KEEP_INVENTORY))return;
  var entity=new SuitRemains(Suits.REMAINS,p.level());s.sealed=true;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var item=inv.getItem(i);if(!item.isEmpty()&&item!=s.stack)s.salvage.add(item.copy());}s.flush();entity.setItem(s.stack.copy());entity.setPos(p.getX(),p.getY(),p.getZ());if(p.level().addFreshEntity(entity)){inv.clearContent();p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);SuitRuntime.clear(p,s);Suits.SESSIONS.remove(p.getUUID());}
 }
}
