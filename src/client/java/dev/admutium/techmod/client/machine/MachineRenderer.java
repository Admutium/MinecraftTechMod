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
 public MachineRenderer(BlockEntityRendererProvider.Context c){resolver=c.itemModelResolver();}
 public static final class State extends BlockEntityRenderState {
  final ItemStackRenderState[] items=new ItemStackRenderState[8];MachineKind kind;Direction facing;boolean active;
  State(){for(int i=0;i<8;i++)items[i]=new ItemStackRenderState();}
 }
 @Override public State createRenderState(){return new State();}
 @Override public int getViewDistance(){return 20;}
 @Override public void extractRenderState(MachineBlockEntity be,State state,float delta,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){
  BlockEntityRenderState.extractBase(be,state,overlay);state.kind=be.kind;state.facing=be.getBlockState().getValue(MachineBlock.FACING);state.active=be.getBlockState().getValue(MachineBlock.ACTIVE);
  boolean near=camera.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()))<16*16;
  for(int i=0;i<8;i++){
   state.items[i].clear();ItemStack stack=ItemStack.EMPTY;
   if(near && be.kind==MachineKind.CABINET)stack=be.getItem(i);
   else if(near && be.kind==MachineKind.FURNACE && i==0)stack=be.getItem(0);
   if(!stack.isEmpty())resolver.updateForTopItem(state.items[i],stack,ItemDisplayContext.FIXED,be.getLevel(),null,be.getBlockPos().hashCode()+i);
  }
 }
 @Override public void submit(State state,PoseStack poses,SubmitNodeCollector collector,CameraRenderState camera){
  poses.pushPose();poses.translate(.5,0,.5);poses.rotate(Axis.YP.rotationDegrees(-state.facing.toYRot()+180));poses.translate(-.5,0,-.5);
  if(state.kind==MachineKind.CABINET){for(int i=0;i<8;i++){
   boolean back=i>=4;int local=i%4;poses.pushPose();poses.translate(back?.27+(local%2)*.46:.73-(local%2)*.46,.28+(1-local/2),back?1.015:-.015);
   if(!back)poses.rotate(Axis.YP.rotationDegrees(180));poses.scale(.42f,.42f,.42f);
   state.items[i].submit(poses,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);poses.popPose();
  }}else if(state.kind==MachineKind.FURNACE && !state.items[0].isEmpty()){
   poses.pushPose();poses.translate(.5,.48,.16);poses.rotate(Axis.YP.rotationDegrees(180));poses.scale(.33f,.33f,.33f);
   state.items[0].submit(poses,collector,state.active?0x00f000f0:state.lightCoords,OverlayTexture.NO_OVERLAY,0);poses.popPose();
  }
  poses.popPose();
 }
}
