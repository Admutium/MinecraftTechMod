package dev.admutium.techmod.client.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.admutium.techmod.client.suit.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("RETURN"))
 private void kontur$previewState(net.minecraft.world.entity.Avatar avatar,net.minecraft.client.renderer.entity.state.AvatarRenderState state,float delta,CallbackInfo ci){
  var data=avatar instanceof net.minecraft.world.entity.player.Player player?dev.admutium.techmod.suit.Suits.data(player):dev.admutium.techmod.suit.SuitData.EMPTY;
  state.setData(SuitClient.SPRINT,avatar.isSprinting());state.setData(SuitClient.VISUAL,data);if(data.id().equals(dev.admutium.techmod.suit.SuitData.EMPTY_ID)&&!SuitPreview.shownFor(state.id))return;
  if(data.energy()>0&&data.on("cloak")&&data.levels().getOrDefault("_reveal",0)==0){state.rightHandItemState.clear();state.leftHandItemState.clear();state.nameTag=null;}
  state.showHat=state.showJacket=state.showLeftPants=state.showRightPants=state.showLeftSleeve=state.showRightSleeve=state.showCape=false;
  // Hide equipment only in the extracted visual state. Actual inventories and armor attributes are untouched.
  state.headEquipment=state.chestEquipment=state.legsEquipment=state.feetEquipment=net.minecraft.world.item.ItemStack.EMPTY;state.heldOnHead.clear();
 }
}
