package dev.admutium.techmod.client.suit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.PlayerModelType;
public final class SuitLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
 private final SuitModels models=new SuitModels();
 public SuitLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent){super(parent);}
 @Override public void submit(PoseStack poses,SubmitNodeCollector out,int light,AvatarRenderState state,float yaw,float pitch){if(!SuitPreview.shownFor(state.id)||state.isSpectator||state.isInvisible)return;models.submit(SuitPose.capture(getParentModel(),SuitPreview.wingMode()),state.skin.model()==PlayerModelType.SLIM,poses,out,light);}
}
