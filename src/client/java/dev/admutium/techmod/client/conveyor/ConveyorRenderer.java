package dev.admutium.techmod.client.conveyor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.admutium.techmod.conveyor.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
public final class ConveyorRenderer implements BlockEntityRenderer<ConveyorEntity,ConveyorRenderer.State> {
 private final ItemModelResolver resolver;private final net.minecraft.client.model.geom.ModelPart carriage,gate;
 private static final net.minecraft.resources.Identifier STEEL=net.minecraft.resources.Identifier.fromNamespaceAndPath("techmod","textures/block/machine_steel.png");
 public ConveyorRenderer(BlockEntityRendererProvider.Context c){resolver=c.itemModelResolver();var mesh=new net.minecraft.client.model.geom.builders.MeshDefinition();mesh.getRoot().addOrReplaceChild("carriage",net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,0).addBox(-5,0,-2,10,1,4).addBox(-5,1,-2,1,2,1).addBox(4,1,-2,1,2,1),net.minecraft.client.model.geom.PartPose.ZERO);carriage=net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,64).bakeRoot();var g=new net.minecraft.client.model.geom.builders.MeshDefinition();g.getRoot().addOrReplaceChild("gate",net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,0).addBox(-5,0,-.5f,10,1,1),net.minecraft.client.model.geom.PartPose.ZERO);gate=net.minecraft.client.model.geom.builders.LayerDefinition.create(g,64,64).bakeRoot();}
 public static final class State extends BlockEntityRenderState {final ItemStackRenderState[] items={new ItemStackRenderState(),new ItemStackRenderState(),new ItemStackRenderState()};final float[] t=new float[2];Conveyors.Kind kind;Direction facing;int slope,turn;float phase,gate;}
 @Override public State createRenderState(){return new State();}@Override public int getViewDistance(){return 16;}
 @Override public void extractRenderState(ConveyorEntity be,State s,float delta,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){BlockEntityRenderState.extractBase(be,s,overlay);s.kind=be.kind();s.facing=be.getBlockState().getValue(ConveyorBlock.FACING);s.slope=ConveyorBlock.slope(be.getBlockState());s.turn=ConveyorBlock.turn(be.getBlockState());double dt=be.getLevel().getGameTime()-be.syncedAt+delta;boolean near=camera.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()))<12*12;
  for(int i=0;i<3;i++)s.items[i].clear();
  for(int i=0;near&&i<Math.min(2,be.cargo.size());i++){var cargo=be.cargo.get(i);resolver.updateForTopItem(s.items[i],cargo.stack,ItemDisplayContext.FIXED,be.getLevel(),null,i);s.t[i]=(float)Math.clamp((cargo.progress+(be.moving?dt:0))/20,0,1);}
  if(near&&!be.hand.isEmpty())resolver.updateForTopItem(s.items[2],be.hand,ItemDisplayContext.FIXED,be.getLevel(),null,2);
  s.phase=(float)(be.armPhase==0?0:Math.min(be.hand.isEmpty()?20:10,be.armPhase+(be.powered?dt:0)));if(be.reverse)s.phase=-s.phase;
  s.gate=(float)Math.clamp((be.getLevel().getGameTime()-be.lastGate+delta)/8,0,1)*70-35;
 }
 @Override public void submit(State s,PoseStack p,SubmitNodeCollector c,CameraRenderState camera){
  p.pushPose();p.translate(.5,0,.5);p.rotate(Axis.YP.rotationDegrees(-s.facing.toYRot()+180));p.translate(-.5,0,-.5);
  for(int i=0;i<2;i++)if(!s.items[i].isEmpty()){float t=s.t[i];double x=.5,z=1-t;if(s.turn!=0){double sx=s.turn>0?1:0;x=(1-t)*(1-t)*sx+2*t*(1-t)*.5+t*t*.5;z=(1-t)*(1-t)*.5+2*t*(1-t)*.5;}
   p.pushPose();p.translate(x,.28+s.slope*t,z);p.rotate(Axis.XP.rotationDegrees(-90));p.scale(.38f,.38f,.38f);s.items[i].submit(p,c,s.lightCoords,OverlayTexture.NO_OVERLAY,0);p.popPose();}
  if(s.kind==Conveyors.Kind.MANIPULATOR){double phase=Math.abs(s.phase);double t=phase<=10?phase/10:(20-phase)/10;if(s.phase<0)t=1-t;double z=.95-1.4*t;
   p.pushPose();p.translate(.5,.25,z);c.submitModelPart(carriage,p,net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(STEEL),s.lightCoords,OverlayTexture.NO_OVERLAY,null);p.popPose();
   if(!s.items[2].isEmpty()){p.pushPose();p.translate(.5,.34,z);p.rotate(Axis.XP.rotationDegrees(-90));p.scale(.38f,.38f,.38f);s.items[2].submit(p,c,s.lightCoords,OverlayTexture.NO_OVERLAY,0);p.popPose();}
  }else if(s.kind==Conveyors.Kind.SPLITTER){p.pushPose();p.translate(.5,.29,.5);p.rotate(Axis.YP.rotationDegrees(s.gate));c.submitModelPart(gate,p,net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(STEEL),s.lightCoords,OverlayTexture.NO_OVERLAY,null);p.popPose();}p.popPose();
 }
}
