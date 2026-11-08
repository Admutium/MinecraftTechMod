package dev.admutium.techmod.test;
import dev.admutium.techmod.client.suit.*;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
public final class SuitVisualTest implements FabricClientGameTest {
 private static java.util.List<Float> armUvs(SuitModel m){var uv=new java.util.ArrayList<Float>();m.root().getChild("right_arm").visit(new com.mojang.blaze3d.vertex.PoseStack(),(pose,path,index,cube)->{for(var polygon:cube.polygons)for(var vertex:polygon.vertices()){uv.add(vertex.u());uv.add(vertex.v());}});return uv;}

 @Override public void runTest(ClientGameTestContext c){
  var reload=c.computeOnClient(client->{client.options.languageCode="ru_ru";client.getLanguageManager().setSelected("ru_ru");return client.reloadResourcePacks();});c.waitFor(client->reload.isDone(),1000);
  try(var sp=c.worldBuilder().adjustSettings(s->s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()){
   sp.getServer().runCommand("gamerule minecraft:send_command_feedback false");sp.getServer().runCommand("fill -20 2 -20 20 2 20 minecraft:smooth_stone");sp.getServer().runCommand("time set 6000");sp.getServer().runCommand("weather clear");sp.getServer().runCommand("tp @a 0 3 0 180 0");
   sp.getServer().runOnServer(server->{var p=sp.getConnection().getServerPlayer();p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);});sp.getConnection().waitForChunksRender();c.getInput().resizeWindow(1280,720);c.waitTicks(15);
   c.computeOnClient(client->{var d=SuitModel.definition();if(!armUvs(new SuitModel(d,false,SuitModel.Pass.BASE)).equals(armUvs(new SuitModel(d,true,SuitModel.Pass.BASE))))throw new AssertionError("Slim arm geometry changed UV mapping");client.options.setCameraType(CameraType.FIRST_PERSON);client.options.fov().set(55);return true;});
   c.getInput().pressKey(75);c.waitForScreen(SuitPreviewScreen.class);c.waitTicks(5);c.takeScreenshot("kontur-suit-fitting");c.setScreen(()->null);c.waitTicks(8);c.takeScreenshot("kontur-suit-first-person-right");
   c.computeOnClient(client->{if(!SuitPreview.enabled())throw new AssertionError("Suit preview not enabled");client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);client.options.fov().set(55);return true;});c.waitTicks(8);c.takeScreenshot("kontur-suit-front");
   c.getInput().holdShift();c.waitTicks(6);c.takeScreenshot("kontur-suit-crouch");c.getInput().releaseShift();c.getInput().holdKey(options->options.keyUp);c.waitTicks(7);c.takeScreenshot("kontur-suit-walk");c.getInput().releaseKey(options->options.keyUp);
   c.computeOnClient(client->{client.options.setCameraType(CameraType.THIRD_PERSON_BACK);return true;});c.waitTicks(8);c.takeScreenshot("kontur-suit-back");
   c.computeOnClient(client->{SuitPreview.setWingMode(1);client.options.fov().set(55);return true;});c.waitTicks(5);c.takeScreenshot("kontur-suit-rainbow-wings");
   c.computeOnClient(client->{SuitPreview.setWingMode(2);return true;});c.waitTicks(5);c.takeScreenshot("kontur-suit-gravity-wings");
   // Use vanilla elytra physics only as a pose fixture; the visual preview grants no flight.
   sp.getServer().runCommand("tp @a 0 15 0 180 0");sp.getServer().runOnServer(server->{var p=sp.getConnection().getServerPlayer();p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new ItemStack(net.minecraft.world.item.Items.ELYTRA));p.getAbilities().flying=false;p.setOnGround(false);p.startFallFlying();p.setDeltaMovement(0,-.05,-.3);});
   c.waitFor(client->client.player.isFallFlying());c.waitTicks(16);c.takeScreenshot("kontur-suit-flight-pose");
   sp.getServer().runOnServer(server->{var p=sp.getConnection().getServerPlayer();p.stopFallFlying();p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,ItemStack.EMPTY);});sp.getServer().runCommand("tp @a 0 3 0 180 0");c.waitTicks(6);
   c.computeOnClient(client->{SuitPreview.setWingMode(0);SuitPreview.cycleColor();client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);client.options.fov().set(55);return true;});c.waitTicks(5);c.takeScreenshot("kontur-suit-teal");
   c.computeOnClient(client->{client.options.setCameraType(CameraType.FIRST_PERSON);client.options.fov().set(55);client.player.setMainArm(net.minecraft.world.entity.HumanoidArm.LEFT);return true;});c.waitTicks(8);c.takeScreenshot("kontur-suit-first-person-left");
   sp.getServer().runOnServer(server->{var p=sp.getConnection().getServerPlayer();for(var slot:new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET})if(!p.getItemBySlot(slot).isEmpty())throw new AssertionError("Preview equipped a real armor item");});
   c.computeOnClient(client->{SuitPreview.setEnabled(false);client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);client.options.fov().set(55);client.player.setMainArm(net.minecraft.world.entity.HumanoidArm.RIGHT);return true;});c.waitTicks(5);c.takeScreenshot("kontur-suit-preview-off");
   // Recreate renderer resources while preview is enabled, then verify the screen still opens.
   var refresh=c.computeOnClient(client->{SuitPreview.setEnabled(true);return client.reloadResourcePacks();});c.waitFor(client->refresh.isDone(),1000);c.waitTicks(5);c.takeScreenshot("kontur-suit-after-reload");
  }
 }
}
