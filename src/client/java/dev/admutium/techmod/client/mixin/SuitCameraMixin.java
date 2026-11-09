package dev.admutium.techmod.client.mixin;
import dev.admutium.techmod.client.suit.SuitClient;import net.minecraft.client.*;import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Camera.class) public abstract class SuitCameraMixin {
 @Inject(method="extractRenderState",at=@At("RETURN")) private void bank(CameraRenderState state,DeltaTracker tracker,CallbackInfo ci){if(Math.abs(SuitClient.bank)>.01f&&state.isFirstPerson){state.viewRotationMatrix=new org.joml.Matrix4f(state.viewRotationMatrix).rotateLocalZ((float)Math.toRadians(SuitClient.bank));state.orientation=new org.joml.Quaternionf(state.orientation).rotateZ((float)Math.toRadians(-SuitClient.bank));}}
}
