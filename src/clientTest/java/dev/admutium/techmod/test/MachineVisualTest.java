package dev.admutium.techmod.test;
import dev.admutium.techmod.client.machine.MachineScreen;
import dev.admutium.techmod.machine.*;
import dev.admutium.techmod.energy.Energy;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.item.*;
public final class MachineVisualTest implements FabricClientGameTest {
 @Override public void runTest(ClientGameTestContext c){
  var languageReload=c.computeOnClient(client->{client.options.languageCode="ru_ru";client.getLanguageManager().setSelected("ru_ru");return client.reloadResourcePacks();});
  c.waitFor(client->languageReload.isDone(),1000);
  try(var sp=c.worldBuilder().adjustSettings(s->s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()){
   sp.getServer().runCommand("gamerule minecraft:send_command_feedback false");
   sp.getServer().runCommand("time set 6000");
   sp.getServer().runCommand("fill -8 2 -10 8 2 10 minecraft:smooth_stone");
   sp.getServer().runCommand("tp @a 0 3 5 180 8");
   sp.getServer().runOnServer(server->{
    var w=sp.getConnection().getServerLevel();
    for(var kind:new MachineKind[]{MachineKind.CABINET,MachineKind.GENERATOR,MachineKind.FURNACE,MachineKind.CRUSHER,MachineKind.SOLAR}){
     int x=kind==MachineKind.CABINET?0:kind==MachineKind.GENERATOR?-2:kind==MachineKind.FURNACE?2:kind==MachineKind.CRUSHER?4:6;BlockPos p=new BlockPos(x,3,0);
     var state=Machines.BLOCKS.get(kind).defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH);
     w.setBlock(p,state,3);
     var be=(MachineBlockEntity)w.getBlockEntity(p);
     if(kind==MachineKind.CABINET){for(int i=0;i<8;i++){var b=new ItemStack(Machines.BATTERY);BatteryItem.setCharge(b,Energy.BATTERY*i/7);be.setItem(i,b);}}
     else if(kind==MachineKind.GENERATOR){be.setItem(0,new ItemStack(Items.COAL_BLOCK));be.setItem(1,new ItemStack(Machines.BATTERY));}
     else if(kind==MachineKind.SOLAR){be.update(new dev.admutium.techmod.cable.CableSnapshot(dev.admutium.techmod.cable.CableLayout.placement(x,3,0,dev.admutium.techmod.cable.Face.DOWN).bit(),0,java.util.List.of()));}
     else{be.setItem(0,new ItemStack(kind==MachineKind.CRUSHER?Items.RAW_IRON:Items.IRON_ORE,16));var b=new ItemStack(Machines.BATTERY);BatteryItem.setCharge(b,Energy.BATTERY);be.setItem(2,b);}
    }
    for(int x=-2;x<=6;x++){
     var p=new BlockPos(x,3,-1);var be=dev.admutium.techmod.cable.CableWorld.ensure(w,p);
     be.update(new dev.admutium.techmod.cable.CableSnapshot(dev.admutium.techmod.cable.CableLayout.placement(x,3,-1,dev.admutium.techmod.cable.Face.DOWN).bit(),0,java.util.List.of()));
    }
    for(int x=0;x<=3;x++){
     w.setBlock(new BlockPos(x,3,-4),net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState(),3);
     var p=new BlockPos(x,4,-4);
     if(x<3)w.setBlock(p,Machines.BLOCKS.get(MachineKind.SOLAR).defaultBlockState(),3);
     dev.admutium.techmod.cable.CableWorld.ensure(w,p).update(new dev.admutium.techmod.cable.CableSnapshot(dev.admutium.techmod.cable.CableLayout.placement(x,4,-4,dev.admutium.techmod.cable.Face.DOWN).bit(),0,java.util.List.of()));
    }
    w.setBlock(new BlockPos(4,4,-4),Machines.BLOCKS.get(MachineKind.CABINET).defaultBlockState(),3);((MachineBlockEntity)w.getBlockEntity(new BlockPos(4,4,-4))).setItem(0,new ItemStack(Machines.BATTERY));
    var player=sp.getConnection().getServerPlayer();
    for(int i=0;i<5;i++){var b=new ItemStack(Machines.BATTERY);BatteryItem.setCharge(b,Energy.BATTERY*i/4);player.getInventory().setItem(i,b);}
    player.getInventory().setItem(5,new ItemStack(dev.admutium.techmod.TechMod.COPPER_CABLE));
    int index=6;for(var ore:Machines.CRUSHED.values())player.getInventory().setItem(index++,new ItemStack(ore));
   });
   sp.getConnection().waitForChunksRender();c.waitTicks(30);c.getInput().lookAt(new BlockPos(1,3,0));c.takeScreenshot("kontur-machines");
   sp.getServer().runCommand("tp @a 5 3 4");c.waitFor(client->client.player.getX()>4);c.getInput().lookAt(new BlockPos(1,3,0));c.waitTicks(5);c.takeScreenshot("kontur-machines-side");
   sp.getServer().runCommand("tp @a 0 3 -5");c.waitFor(client->client.player.getZ()< -4);c.getInput().lookAt(new BlockPos(1,3,0));c.waitTicks(5);c.takeScreenshot("kontur-machines-rear");
   sp.getServer().runCommand("tp @a 0 3 5");
   c.computeOnClient(client->{client.player.getInventory().setSelectedSlot(5);return true;});c.waitTicks(8);c.takeScreenshot("kontur-wire-right");
   sp.getServer().runOnServer(server->sp.getConnection().getServerPlayer().setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(dev.admutium.techmod.TechMod.COPPER_CABLE)));
   c.computeOnClient(client->{client.player.getInventory().setSelectedSlot(0);return true;});
   c.waitFor(client->client.player.getOffhandItem().is(dev.admutium.techmod.TechMod.COPPER_CABLE));c.waitTicks(8);c.takeScreenshot("kontur-wire-left");
   sp.getServer().runOnServer(server->sp.getConnection().getServerPlayer().setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,ItemStack.EMPTY));
   sp.getServer().runCommand("tp @a 4 3 3");c.waitFor(client->client.player.getX()>3);c.getInput().lookAt(new BlockPos(4,3,0));c.waitTicks(5);c.takeScreenshot("kontur-crusher-close");
   sp.getServer().runCommand("tp @a 7 3 2");c.waitFor(client->client.player.getX()>6);c.getInput().lookAt(new BlockPos(6,3,0));c.waitTicks(5);c.takeScreenshot("kontur-solar-close");
   sp.getServer().runCommand("tp @a 1 2.6 -1");c.waitFor(client->client.player.getZ()<0);c.getInput().lookAt(new BlockPos(1,4,-4));c.waitTicks(6);c.takeScreenshot("kontur-solar-array");
   sp.getServer().runCommand("tp @a 2 3 4");c.waitFor(client->client.player.getX()>1&&client.player.getX()<3);
   for(var kind:new MachineKind[]{MachineKind.CABINET,MachineKind.GENERATOR,MachineKind.FURNACE,MachineKind.CRUSHER,MachineKind.SOLAR}){
    int x=kind==MachineKind.CABINET?0:kind==MachineKind.GENERATOR?-2:kind==MachineKind.FURNACE?2:kind==MachineKind.CRUSHER?4:6;
    sp.getServer().runOnServer(server->{var player=sp.getConnection().getServerPlayer();player.openMenu((MachineBlockEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(x,3,0)));});
    c.waitForScreen(MachineScreen.class);c.waitTicks(4);c.takeScreenshot("kontur-"+kind.id+"-screen");c.setScreen(()->null);c.waitTicks(2);
   }
   c.setScreen(()->new net.minecraft.client.gui.screens.options.SoundOptionsScreen(null,net.minecraft.client.Minecraft.getInstance().options));c.waitTicks(3);
   c.computeOnClient(client->{
    var button=net.fabricmc.fabric.api.client.screen.v1.Screens.getWidgets(client.gui.screen()).stream().filter(w->w instanceof net.minecraft.client.gui.components.Button&&w.getMessage().getString().equals(net.minecraft.network.chat.Component.translatable("options.techmod.sounds").getString())).findFirst().orElseThrow();
    ((net.minecraft.client.gui.components.Button)button).onPress(null);return true;
   });
   c.waitForScreen(dev.admutium.techmod.client.sound.MachineAudioScreen.class);c.waitTicks(3);c.takeScreenshot("kontur-audio-settings");
   c.computeOnClient(client->{double old=dev.admutium.techmod.client.sound.MachineAudioConfig.volume;dev.admutium.techmod.client.sound.MachineAudioConfig.volume=.42;dev.admutium.techmod.client.sound.MachineAudioConfig.save();dev.admutium.techmod.client.sound.MachineAudioConfig.volume=0;dev.admutium.techmod.client.sound.MachineAudioConfig.load();if(Math.abs(dev.admutium.techmod.client.sound.MachineAudioConfig.volume-.42)>.0001)throw new AssertionError("Audio volume not persisted");dev.admutium.techmod.client.sound.MachineAudioConfig.volume=old;dev.admutium.techmod.client.sound.MachineAudioConfig.save();return true;});
   c.setScreen(()->null);
  }
 }
}
