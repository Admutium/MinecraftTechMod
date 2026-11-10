package dev.admutium.techmod.client.mixin;

import dev.admutium.techmod.client.suit.SuitClient;
import dev.admutium.techmod.suit.*;
import dev.admutium.techmod.energy.Energy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep gravity mode active on landing, and stop vertical drift when ascent/descent is released. */
@Mixin(LocalPlayer.class) public abstract class SuitGravityMixin {
 @Unique private boolean kontur$gravity;
 @Unique private boolean kontur$powered(){var d=SuitClient.wornData();return d.on("flight_gravity")&&d.level(SuitUpgrade.FLIGHT)>=3&&d.energy()>=Math.round(.5*(1-.08*d.level(SuitUpgrade.EFFICIENCY))*Energy.UNIT);}
 @Inject(method="aiStep",at=@At("HEAD"))private void before(CallbackInfo ci){var p=(LocalPlayer)(Object)this;boolean enabled=kontur$powered();if(enabled){p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.getAbilities().setFlyingSpeed(.32f);}else if(kontur$gravity){p.getAbilities().mayfly=p.isCreative()||p.isSpectator();p.getAbilities().flying=p.getAbilities().flying&&p.getAbilities().mayfly;}kontur$gravity=enabled;}
 @Redirect(method="aiStep",at=@At(value="FIELD",target="Lnet/minecraft/world/entity/player/Abilities;flying:Z",opcode=Opcodes.PUTFIELD))private void flying(Abilities abilities,boolean value){abilities.flying=value||kontur$powered();}
 @Inject(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V"))private void hover(CallbackInfo ci){if(!kontur$powered())return;var c=Minecraft.getInstance();var p=(LocalPlayer)(Object)this;boolean input=c.gui.screen()==null&&(c.options.keyJump.isDown()||c.options.keyShift.isDown());var v=p.getDeltaMovement();p.setDeltaMovement(v.x,input?Math.clamp(v.y,-.75,.75):0,v.z);p.resetFallDistance();}
}
