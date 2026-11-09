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
 @Override public void submit(PoseStack poses,SubmitNodeCollector out,int light,AvatarRenderState state,float yaw,float pitch){var d=state.getDataOrDefault(SuitClient.VISUAL,dev.admutium.techmod.suit.SuitData.EMPTY);boolean actual=!d.id().equals(dev.admutium.techmod.suit.SuitData.EMPTY_ID);if((!actual&&!SuitPreview.shownFor(state.id))||state.isSpectator||state.isInvisible)return;int wings=actual?(d.energy()>0&&d.on("flight_gravity")?2:state.isFallFlying&&d.on("flight_elytra")?1:0):SuitPreview.wingMode();var pose=SuitPose.capture(getParentModel(),wings);float alpha=actual&&d.energy()>0&&d.on("cloak")?Math.max(.045f,Math.min(1,d.levels().getOrDefault("_reveal",0)/12f)):1;models.submit(pose,state.skin.model()==PlayerModelType.SLIM,poses,out,light,actual?d.tint():SuitPreview.accent(),alpha);
  if(actual&&d.energy()>0&&d.on("run")&&state.getDataOrDefault(SuitClient.SPRINT,false)&&alpha==1){for(int i=1;i<=2;i++){poses.pushPose();poses.translate(0,0,i*.42);models.submit(pose,state.skin.model()==PlayerModelType.SLIM,poses,out,light,d.tint(),.12f/i);poses.popPose();}}
 }
}
