package dev.admutium.techmod.test;
import dev.admutium.techmod.machine.*;
import dev.admutium.techmod.conveyor.*;
import dev.admutium.techmod.energy.Energy;
import dev.admutium.techmod.client.conveyor.ConveyorScreen;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;
public final class ConveyorVisualTest implements FabricClientGameTest {
 private ConveyorEntity part(net.minecraft.server.level.ServerLevel w,Conveyors.Kind k,int x,int y,int z,Direction d){var p=new BlockPos(x,y,z);w.setBlock(p,Conveyors.BLOCKS.get(k).defaultBlockState().setValue(ConveyorBlock.FACING,d),3);return (ConveyorEntity)w.getBlockEntity(p);}
 private MachineBlockEntity drive(net.minecraft.server.level.ServerLevel w,int x,int y,int z,Direction d){var p=new BlockPos(x,y,z);w.setBlock(p,Machines.BLOCKS.get(MachineKind.DRIVE).defaultBlockState().setValue(MachineBlock.FACING,d),3);var be=(MachineBlockEntity)w.getBlockEntity(p);be.energy=10*Energy.UNIT;return be;}
 @Override public void runTest(ClientGameTestContext c){
  var reload=c.computeOnClient(client->{client.options.languageCode="ru_ru";client.getLanguageManager().setSelected("ru_ru");return client.reloadResourcePacks();});c.waitFor(client->reload.isDone(),1000);
  try(var sp=c.worldBuilder().adjustSettings(s->s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()){
   sp.getServer().runCommand("gamerule minecraft:send_command_feedback false");
   sp.getServer().runCommand("fill -12 2 -10 12 2 10 minecraft:smooth_stone");sp.getServer().runCommand("time set 6000");sp.getServer().runCommand("tp @a 0 3 6 180 5");
   sp.getServer().runOnServer(server->{var w=sp.getConnection().getServerLevel();
    w.setBlock(new BlockPos(-6,3,0),Blocks.CHEST.defaultBlockState(),3);((Container)w.getBlockEntity(new BlockPos(-6,3,0))).setItem(0,new ItemStack(Items.IRON_INGOT,64));
    part(w,Conveyors.Kind.MANIPULATOR,-5,3,0,Direction.EAST);part(w,Conveyors.Kind.BELT,-4,3,0,Direction.EAST);part(w,Conveyors.Kind.BELT,-3,3,0,Direction.EAST);part(w,Conveyors.Kind.MANIPULATOR,-2,3,0,Direction.WEST).reverse=true;
    w.setBlock(new BlockPos(-1,3,0),Machines.BLOCKS.get(MachineKind.PRESS).defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH),3);var press=(MachineBlockEntity)w.getBlockEntity(new BlockPos(-1,3,0));press.energy=300*Energy.UNIT;press.itemSides[Direction.WEST.ordinal()]=1;press.itemSides[Direction.EAST.ordinal()]=2;
    part(w,Conveyors.Kind.MANIPULATOR,0,3,0,Direction.EAST);part(w,Conveyors.Kind.BELT,1,3,0,Direction.EAST);part(w,Conveyors.Kind.BELT,2,3,0,Direction.EAST);part(w,Conveyors.Kind.MANIPULATOR,3,3,0,Direction.WEST).reverse=true;w.setBlock(new BlockPos(4,3,0),Blocks.CHEST.defaultBlockState(),3);
    drive(w,-4,3,-1,Direction.SOUTH);drive(w,1,3,-1,Direction.SOUTH);
    var p=new BlockPos(6,3,3);part(w,Conveyors.Kind.BELT,6,3,4,Direction.NORTH);w.setBlock(p,Conveyors.BLOCKS.get(Conveyors.Kind.BELT).defaultBlockState().setValue(ConveyorBlock.SLOPE,1),3);w.setBlock(p.above(),w.getBlockState(p).setValue(ConveyorBlock.UPPER,true),3);part(w,Conveyors.Kind.BELT,6,4,2,Direction.NORTH);drive(w,7,3,4,Direction.WEST);
    ((ConveyorEntity)w.getBlockEntity(new BlockPos(6,3,4))).offer(new ItemStack(Items.DIAMOND,4),w.getGameTime());
    var split=part(w,Conveyors.Kind.SPLITTER,0,3,5,Direction.NORTH);part(w,Conveyors.Kind.BELT,0,3,6,Direction.NORTH);part(w,Conveyors.Kind.BELT,-1,3,5,Direction.WEST);part(w,Conveyors.Kind.BELT,1,3,5,Direction.EAST);drive(w,-1,3,6,Direction.EAST);split.offer(new ItemStack(Items.GOLD_INGOT,4),w.getGameTime());
    var player=sp.getConnection().getServerPlayer();int i=0;for(var plate:Machines.PLATES.values())player.getInventory().setItem(i++,new ItemStack(plate));player.getInventory().setItem(3,new ItemStack(Conveyors.ITEMS.get(Conveyors.Kind.BELT)));
   });
   sp.getConnection().waitForChunksRender();c.waitTicks(100);c.getInput().lookAt(new BlockPos(-1,3,0));c.takeScreenshot("kontur-automation");
   sp.getServer().runCommand("tp @a -4 3 3");c.waitFor(client->client.player.getX()<-3);c.getInput().lookAt(new BlockPos(-4,3,0));c.waitTicks(8);c.takeScreenshot("kontur-manipulator-close");
   sp.getServer().runCommand("tp @a 7 3 6");c.waitFor(client->client.player.getX()>6);c.getInput().lookAt(new BlockPos(6,4,3));c.waitTicks(6);c.takeScreenshot("kontur-ramp");
   sp.getServer().runCommand("tp @a 0 3 3");c.waitFor(client->client.player.getX()<1);
   sp.getServer().runOnServer(s->sp.getConnection().getServerPlayer().openMenu((MachineBlockEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(-1,3,0))));c.waitForScreen(dev.admutium.techmod.client.machine.MachineScreen.class);c.waitTicks(4);c.takeScreenshot("kontur-press-screen");
   c.computeOnClient(client->{var screen=(dev.admutium.techmod.client.machine.MachineScreen)client.gui.screen();double x=(client.getWindow().getGuiScaledWidth()-218)/2+180,y=(client.getWindow().getGuiScaledHeight()-228)/2+15;screen.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(x,y,new net.minecraft.client.input.MouseButtonInfo(0,0)),false);return true;});c.waitTicks(3);c.takeScreenshot("kontur-machine-sides");c.setScreen(()->null);
   sp.getServer().runOnServer(s->sp.getConnection().getServerPlayer().openMenu((MachineBlockEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(-4,3,-1))));c.waitForScreen(dev.admutium.techmod.client.machine.MachineScreen.class);c.waitTicks(3);c.takeScreenshot("kontur-drive-screen");
   c.computeOnClient(client->{var screen=(dev.admutium.techmod.client.machine.MachineScreen)client.gui.screen();double x=(client.getWindow().getGuiScaledWidth()-218)/2+70,y=(client.getWindow().getGuiScaledHeight()-228)/2+60;screen.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(x,y,new net.minecraft.client.input.MouseButtonInfo(0,0)),false);return true;});c.waitTicks(5);sp.getServer().runOnServer(s->{var drive=(MachineBlockEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(-4,3,-1));if(drive.driveMode!=1)throw new AssertionError("Drive redstone button failed");drive.driveMode=0;});c.setScreen(()->null);
   sp.getServer().runCommand("tp @a -4 3 3");c.waitFor(client->client.player.getX()<-3);
   sp.getServer().runOnServer(s->sp.getConnection().getServerPlayer().openMenu((ConveyorEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(-5,3,0))));c.waitForScreen(ConveyorScreen.class);c.waitTicks(4);c.takeScreenshot("kontur-filter-screen");
   c.computeOnClient(client->{for(var widget:net.fabricmc.fabric.api.client.screen.v1.Screens.getWidgets(client.gui.screen()))if(widget instanceof net.minecraft.client.gui.components.EditBox box)box.setValue("minecraft:golden_apple");net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new FilterPayload(client.player.containerMenu.containerId,8,"minecraft:golden_apple"));return true;});c.waitTicks(8);c.takeScreenshot("kontur-filter-search");
   sp.getServer().runOnServer(s->{var be=(ConveyorEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(-5,3,0));if(!be.filter.getItem(8).is(Items.GOLDEN_APPLE))throw new AssertionError("Catalog selection did not reach server");});c.setScreen(()->null);
   sp.getServer().runCommand("tp @a -3.7 3.25 0.5");c.waitFor(client->client.player.getX()<-3&&client.player.getZ()<1);
   double standing=c.computeOnClient(client->{client.player.getAbilities().flying=false;client.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);return client.player.getX();});c.waitTicks(15);
   c.computeOnClient(client->{if(client.player.getX()<=standing+.02)throw new AssertionError("Player was not carried by the belt");return true;});
   sp.getServer().runCommand("tp @a -4 3 3");c.waitFor(client->client.player.getZ()>2);
   c.computeOnClient(client->{client.player.getInventory().setSelectedSlot(3);return true;});c.getInput().lookAt(new BlockPos(-3,2,3));c.waitTicks(5);c.takeScreenshot("kontur-placement-preview");
  }
 }
}
