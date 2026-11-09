package dev.admutium.techmod.client.suit;
import dev.admutium.techmod.suit.*;import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.*;import net.minecraft.client.renderer.entity.state.EntityRenderState;import net.minecraft.client.renderer.state.level.CameraRenderState;import net.minecraft.client.model.geom.PartPose;
public final class SuitRemainsRenderer extends EntityRenderer<SuitRemains,SuitRemainsRenderer.State>{
 private final SuitModels model=new SuitModels();public static final SuitPose REST=new SuitPose(PartPose.ZERO,PartPose.ZERO,PartPose.offset( -5,2,0),PartPose.offset(5,2,0),PartPose.offset(-1.9f,12,0),PartPose.offset(1.9f,12,0),0);
 public static class State extends EntityRenderState{int tint;}
 public SuitRemainsRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.6f;}
 public State createRenderState(){return new State();}
 @Override public void extractRenderState(SuitRemains e,State s,float delta){super.extractRenderState(e,s,delta);s.tint=e.getItem().getOrDefault(Suits.DATA,SuitData.EMPTY).tint();}
 @Override public void submit(State s,PoseStack p,SubmitNodeCollector out,CameraRenderState c){p.pushPose();p.translate(0,.2,.7);p.rotate(Axis.XP.rotationDegrees(90));p.scale(-1,-1,1);p.translate(0,-1.5,0);model.submit(REST,false,p,out,s.lightCoords,s.tint,1);p.popPose();super.submit(s,p,out,c);}
}
