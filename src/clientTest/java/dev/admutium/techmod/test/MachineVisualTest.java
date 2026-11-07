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
   sp.getServer().runCommand("fill -8 2 -10 8 2 10 minecraft:smooth_stone");
   sp.getServer().runCommand("tp @a 0 3 5 180 8");
   sp.getServer().runOnServer(server->{
    var w=sp.getConnection().getServerLevel();
    for(var kind:MachineKind.values()){
     int x=kind==MachineKind.CABINET?0:kind==MachineKind.GENERATOR?-2:2;BlockPos p=new BlockPos(x,3,0);
     var state=Machines.BLOCKS.get(kind).defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH);
     w.setBlock(p,state,3);if(kind==MachineKind.CABINET)w.setBlock(p.above(),state.setValue(MachineBlock.UPPER,true),3);
     var be=(MachineBlockEntity)w.getBlockEntity(p);
     if(kind==MachineKind.CABINET){for(int i=0;i<8;i++){var b=new ItemStack(Machines.BATTERY);BatteryItem.setCharge(b,Energy.BATTERY*i/7);be.setItem(i,b);}}
     else if(kind==MachineKind.GENERATOR){be.setItem(0,new ItemStack(Items.COAL,8));be.setItem(1,new ItemStack(Machines.BATTERY));}
     else{be.setItem(0,new ItemStack(Items.IRON_ORE,16));var b=new ItemStack(Machines.BATTERY);BatteryItem.setCharge(b,Energy.BATTERY);be.setItem(2,b);}
    }
    for(int x=-2;x<=2;x++){
     var p=new BlockPos(x,3,-1);var be=dev.admutium.techmod.cable.CableWorld.ensure(w,p);
     be.update(new dev.admutium.techmod.cable.CableSnapshot(dev.admutium.techmod.cable.CableLayout.placement(x,3,-1,dev.admutium.techmod.cable.Face.DOWN).bit(),0,java.util.List.of()));
    }
    var player=sp.getConnection().getServerPlayer();
    for(int i=0;i<5;i++){var b=new ItemStack(Machines.BATTERY);BatteryItem.setCharge(b,Energy.BATTERY*i/4);player.getInventory().setItem(i,b);}
   });
   sp.getConnection().waitForChunksRender();c.waitTicks(30);c.getInput().lookAt(new BlockPos(0,4,0));c.takeScreenshot("kontur-machines");
   sp.getServer().runCommand("tp @a 5 3 4");c.waitFor(client->client.player.getX()>4);c.getInput().lookAt(new BlockPos(0,4,0));c.waitTicks(5);c.takeScreenshot("kontur-machines-side");
   sp.getServer().runCommand("tp @a 0 3 -5");c.waitFor(client->client.player.getZ()< -4);c.getInput().lookAt(new BlockPos(0,4,0));c.waitTicks(5);c.takeScreenshot("kontur-machines-rear");
   sp.getServer().runCommand("tp @a 0 3 5");
   for(var kind:MachineKind.values()){
    int x=kind==MachineKind.CABINET?0:kind==MachineKind.GENERATOR?-2:2;
    sp.getServer().runOnServer(server->{var player=sp.getConnection().getServerPlayer();player.openMenu((MachineBlockEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(x,3,0)));});
    c.waitForScreen(MachineScreen.class);c.waitTicks(4);c.takeScreenshot("kontur-"+kind.id+"-screen");c.setScreen(()->null);c.waitTicks(2);
   }
  }
 }
}
