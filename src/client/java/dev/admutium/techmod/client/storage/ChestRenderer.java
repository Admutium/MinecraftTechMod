package dev.admutium.techmod.client.storage;
import dev.admutium.techmod.storage.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
/** Only the single facade sample is dynamic; the wooden case and lid are baked into the chunk mesh. */
public final class ChestRenderer implements BlockEntityRenderer<ReinforcedChestEntity,ChestRenderer.State> {
 private final ItemModelResolver resolver;
 public ChestRenderer(BlockEntityRendererProvider.Context c){resolver=c.itemModelResolver();}
 public static final class State extends BlockEntityRenderState{final ItemStackRenderState item=new ItemStackRenderState();Direction facing;}
 @Override public State createRenderState(){return new State();}
 @Override public int getViewDistance(){return 16;}
 @Override public void extractRenderState(ReinforcedChestEntity be,State s,float delta,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay){BlockEntityRenderState.extractBase(be,s,overlay);s.facing=be.getBlockState().getValue(ReinforcedChest.FACING);s.item.clear();if(camera.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()))<256&&!be.sample.getItem(0).isEmpty())resolver.updateForTopItem(s.item,be.sample.getItem(0),ItemDisplayContext.FIXED,be.getLevel(),null,0);}
 @Override public void submit(State s,PoseStack p,SubmitNodeCollector c,CameraRenderState camera){if(s.item.isEmpty())return;p.pushPose();p.translate(.5,0,.5);p.rotate(Axis.YP.rotationDegrees(-s.facing.toYRot()+180));p.translate(0,.36,-.443);p.rotate(Axis.YP.rotationDegrees(180));p.scale(.25f,.25f,.25f);s.item.submit(p,c,s.lightCoords,OverlayTexture.NO_OVERLAY,0);p.popPose();}
}
