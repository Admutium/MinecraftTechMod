package dev.admutium.techmod.client.mixin;
import dev.admutium.techmod.client.suit.SuitPreview;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Replace the third-person skin silhouette; vanilla first-person hand rendering is deliberately untouched. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
 @Inject(method="getRenderType",at=@At("HEAD"),cancellable=true)
 private void kontur$skin(LivingEntityRenderState state,boolean bodyVisible,boolean translucent,boolean outline,CallbackInfoReturnable<RenderType> ci){if(state instanceof AvatarRenderState player&&SuitPreview.shownFor(player.id)&&!player.isSpectator)ci.setReturnValue(null);}
}
