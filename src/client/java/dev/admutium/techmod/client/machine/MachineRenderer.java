package dev.admutium.techmod.client.machine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.admutium.techmod.machine.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
public final class MachineRenderer implements BlockEntityRenderer<MachineBlockEntity,MachineRenderer.State> {
 private final ItemModelResolver resolver;
 private final dev.admutium.techmod.client.suit.SuitModels suitModels=new dev.admutium.techmod.client.suit.SuitModels();
 private final net.minecraft.client.model.geom.ModelPart roller,pressHead,driveCoupler,cutterBlade;
 private static final net.minecraft.resources.Identifier ROLLER_TEXTURE=net.minecraft.resources.Identifier.fromNamespaceAndPath("techmod","textures/block/machine_steel.png");
 public MachineRenderer(BlockEntityRendererProvider.Context c){resolver=c.itemModelResolver();
  var mesh=new net.minecraft.client.model.geom.builders.MeshDefinition();
  var cubes=net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,0).addBox(-1.45f,-2.85f,-1.45f,2.9f,5.7f,2.9f);
  for(int y=-3;y<3;y+=2){cubes.texOffs(0,16).addBox(-2,y,-2,4,1,4);}
  mesh.getRoot().addOrReplaceChild("roller",cubes,net.minecraft.client.model.geom.PartPose.ZERO);
  roller=net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,64).bakeRoot();
  var press=new net.minecraft.client.model.geom.builders.MeshDefinition();press.getRoot().addOrReplaceChild("head",net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,0).addBox(-4.5f,-1,-3,9,2,6).addBox(-1,1,-1,2,4,2),net.minecraft.client.model.geom.PartPose.ZERO);pressHead=net.minecraft.client.model.geom.builders.LayerDefinition.create(press,64,64).bakeRoot();
  var cutter=new net.minecraft.client.model.geom.builders.MeshDefinition();
  cutter.getRoot().addOrReplaceChild("disc",net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,0).addBox(-2.5f,-2.5f,-.35f,5,5,.7f),net.minecraft.client.model.geom.PartPose.ZERO);
  for(int i=0;i<8;i++){float angle=(float)(i*Math.PI/4);cutter.getRoot().addOrReplaceChild("tooth"+i,net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,0).addBox(-.5f,2,-.4f,1,1.5f,.8f),net.minecraft.client.model.geom.PartPose.offsetAndRotation(0,0,0,0,0,angle));}
  cutterBlade=net.minecraft.client.model.geom.builders.LayerDefinition.create(cutter,64,64).bakeRoot();
  var coupling=new net.minecraft.client.model.geom.builders.MeshDefinition();coupling.getRoot().addOrReplaceChild("coupling",net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,0).addBox(-1.75f,-1.75f,-1,3.5f,3.5f,2),net.minecraft.client.model.geom.PartPose.ZERO);driveCoupler=net.minecraft.client.model.geom.builders.LayerDefinition.create(coupling,64,64).bakeRoot();
 }
 public static final class State extends BlockEntityRenderState {
  int suitTint;boolean suitPresent;final ItemStackRenderState[] items=new ItemStackRenderState[8];MachineKind kind;Direction facing;boolean active;float rotation,press;
  State(){for(int i=0;i<8;i++)items[i]=new ItemStackRenderState();}
 }
 @Override public State createRenderState(){return new State();}
 @Override public int getViewDistance(){return 20;}
 @Override public void extractRenderState(MachineBlockEntity be,State state,float delta,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){
  BlockEntityRenderState.extractBase(be,state,overlay);state.kind=be.kind;state.facing=be.getBlockState().getValue(MachineBlock.FACING);state.active=be.getBlockState().getValue(MachineBlock.ACTIVE);state.rotation=state.active?(be.getLevel().getGameTime()%120+delta)*9:0;
  state.suitPresent=be.kind==MachineKind.STATION&&be.getItem(0).is(dev.admutium.techmod.suit.Suits.ITEM);state.suitTint=state.suitPresent?be.getItem(0).getOrDefault(dev.admutium.techmod.suit.Suits.DATA,dev.admutium.techmod.suit.SuitData.EMPTY).tint():0xffff9c26;
  state.press=be.kind==MachineKind.PRESS&&state.active?(float)Math.sin(Math.PI*(be.progress+delta)/80):0;
  boolean near=camera.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()))<16*16;
  for(int i=0;i<8;i++){
   state.items[i].clear();ItemStack stack=ItemStack.EMPTY;
   if(near && be.kind==MachineKind.CABINET)stack=be.getItem(i);
   else if(near && be.processes() && i==0)stack=be.getItem(0);
   if(!stack.isEmpty())resolver.updateForTopItem(state.items[i],stack,ItemDisplayContext.FIXED,be.getLevel(),null,be.getBlockPos().hashCode()+i);
  }
 }
 @Override public void submit(State state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){
  poses.pushPose();poses.translate(.5,0,.5);poses.rotate(Axis.YP.rotationDegrees(-state.facing.toYRot()+180));poses.translate(-.5,0,-.5);
  if(state.kind==MachineKind.STATION&&state.suitPresent){poses.pushPose();poses.translate(.5,1.7,.5);poses.scale(-1,-1,1);suitModels.submit(dev.admutium.techmod.client.suit.SuitRemainsRenderer.REST,false,poses,collector,state.lightCoords,state.suitTint,1);poses.popPose();}
  if(state.kind==MachineKind.CABINET){for(int i=0;i<8;i++){
   boolean back=i>=4;int local=i%4;poses.pushPose();poses.translate(back?.27+(local%2)*.46:.73-(local%2)*.46,.26+(1-local/2)*.49,back?1.015:-.015);
   if(!back)poses.rotate(Axis.YP.rotationDegrees(180));poses.scale(.42f,.42f,.42f);
   state.items[i].submit(poses,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);poses.popPose();
  }}else if((state.kind==MachineKind.FURNACE||state.kind==MachineKind.CRUSHER||state.kind==MachineKind.PRESS||state.kind==MachineKind.CUTTER) && !state.items[0].isEmpty()){
   poses.pushPose();poses.translate(.5,state.kind==MachineKind.CRUSHER?.85:state.kind==MachineKind.PRESS?.32:.48,state.kind==MachineKind.CRUSHER?.5:state.kind==MachineKind.PRESS?.35:.16);poses.rotate(Axis.YP.rotationDegrees(180));if(state.kind==MachineKind.PRESS)poses.rotate(Axis.XP.rotationDegrees(-90));poses.scale(.33f,.33f,.33f);
   state.items[0].submit(poses,collector,state.active?0x00f000f0:state.lightCoords,OverlayTexture.NO_OVERLAY,0);poses.popPose();
  }
  if(state.kind==MachineKind.DRIVE){poses.pushPose();poses.translate(.5,.1875,.02);poses.rotate(Axis.ZP.rotationDegrees(state.rotation));collector.submitModelPart(driveCoupler,poses,net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(ROLLER_TEXTURE),state.lightCoords,OverlayTexture.NO_OVERLAY,null);poses.popPose();}
  if(state.kind==MachineKind.CUTTER){poses.pushPose();poses.translate(.5,.5,.28);poses.rotate(Axis.ZP.rotationDegrees(state.rotation*2));collector.submitModelPart(cutterBlade,poses,net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(ROLLER_TEXTURE),state.lightCoords,OverlayTexture.NO_OVERLAY,null);poses.popPose();}
  if(state.kind==MachineKind.PRESS){poses.pushPose();poses.translate(.5,.65-.22*state.press,.35);collector.submitModelPart(pressHead,poses,net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(ROLLER_TEXTURE),state.lightCoords,OverlayTexture.NO_OVERLAY,null);poses.popPose();}
  if(state.kind==MachineKind.CRUSHER){
   for(int i=0;i<2;i++){
    poses.pushPose();poses.translate(i==0?.36:.64,.48,.22);poses.rotate(Axis.YP.rotationDegrees((i==0?1:-1)*state.rotation));
    collector.submitModelPart(roller,poses,net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(ROLLER_TEXTURE),state.lightCoords,OverlayTexture.NO_OVERLAY,null);poses.popPose();
   }
  }
  poses.popPose();
 }
}
