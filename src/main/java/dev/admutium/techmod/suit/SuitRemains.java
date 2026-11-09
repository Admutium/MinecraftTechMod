package dev.admutium.techmod.suit;
import net.minecraft.world.entity.*;import net.minecraft.world.entity.item.ItemEntity;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.Level;import net.minecraft.world.*;
/** Gravity and save/load are vanilla item physics, but lifetime, pickup and damage are overridden. */
public final class SuitRemains extends ItemEntity {
 public SuitRemains(EntityType<? extends ItemEntity> type,Level level){super(type,level);setUnlimitedLifetime();setNeverPickUp();setPermanentlyInvulnerable(true);}
 @Override public void tick(){setUnlimitedLifetime();setNeverPickUp();setPermanentlyInvulnerable(true);super.tick();if(getY()<level().getMinY()+1){setPos(getX(),level().getMinY()+1,getZ());setDeltaMovement(0,0,0);setNoGravity(true);}}
 @Override public void playerTouch(Player p){}
 @Override public InteractionResult interact(Player p,InteractionHand hand,net.minecraft.world.phys.Vec3 location){if(!level().isClientSide()&&!isRemoved()){var stack=getItem().copy();if(p.getInventory().add(stack)&&stack.isEmpty()){setItem(net.minecraft.world.item.ItemStack.EMPTY);discard();}else Suits.message(p,"Освободите место для костюма");}return InteractionResult.SUCCESS;}
 @Override public boolean isPickable(){return true;}
 @Override public boolean isAttackable(){return false;}
}
