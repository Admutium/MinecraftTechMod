package dev.admutium.techmod.client.mixin;
import dev.admutium.techmod.client.suit.SuitClient;import dev.admutium.techmod.suit.*;import net.minecraft.client.renderer.LightmapRenderStateExtractor;import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LightmapRenderStateExtractor.class) public abstract class SuitLightMixin {
 @Inject(method="extract",at=@At("RETURN")) private void light(LightmapRenderState state,float partial,CallbackInfo ci){var d=SuitClient.wornData();if(d.energy()<=0)return;if(d.on("night")&&d.level(SuitUpgrade.NIGHT)>0){state.nightVisionEffectIntensity=1;state.nightVisionColor=new org.joml.Vector3f(1,1,1);state.needsUpdate=true;}if(d.on("light")&&d.level(SuitUpgrade.LIGHT)>0){state.brightness=Math.max(state.brightness,d.level(SuitUpgrade.LIGHT)*.15f);state.needsUpdate=true;}}
}
