package dev.admutium.techmod.test;
import dev.admutium.techmod.material.*;
import dev.admutium.techmod.storage.*;
import dev.admutium.techmod.machine.*;
import dev.admutium.techmod.conveyor.*;
import dev.admutium.techmod.energy.Energy;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import java.util.*;
public final class MaterialsVisualTest implements FabricClientGameTest {
 @Override public void runTest(ClientGameTestContext c){
  try(var sp=c.worldBuilder().adjustSettings(s->s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()){
   sp.getServer().runCommand("gamerule minecraft:send_command_feedback false");sp.getServer().runCommand("fill -10 2 -10 10 2 10 minecraft:smooth_stone");sp.getServer().runCommand("time set 6000");sp.getServer().runCommand("tp @a 1 3 6 180 15");
   sp.getServer().runOnServer(server->{var w=sp.getConnection().getServerLevel();
    var cp=new BlockPos(0,3,0);w.setBlock(cp,Machines.BLOCKS.get(MachineKind.CUTTER).defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH),3);var cutter=(MachineBlockEntity)w.getBlockEntity(cp);cutter.energy=Energy.BUFFER;cutter.setItem(0,new ItemStack(Machines.PLATES.get(Items.COPPER_INGOT),16));
    var chestPos=new BlockPos(2,3,0);w.setBlock(chestPos,ReinforcedStorage.BLOCK.defaultBlockState().setValue(ReinforcedChest.FACING,Direction.SOUTH),3);var chest=(ReinforcedChestEntity)w.getBlockEntity(chestPos);chest.sample.setItem(0,new ItemStack(Materials.TIN_INGOT));for(int i=0;i<36;i++)if(i%4==0)chest.setItem(i,new ItemStack(i%8==0?Materials.TIN_INGOT:Materials.COPPER_WIRE,16));
    w.setBlock(new BlockPos(-4,3,0),Materials.TIN_ORE.defaultBlockState(),3);w.setBlock(new BlockPos(-3,3,0),Materials.DEEPSLATE_TIN_ORE.defaultBlockState(),3);w.setBlock(new BlockPos(-2,3,0),Materials.TIN_BLOCK.defaultBlockState(),3);
    for(int i=0;i<4;i++){var p=new BlockPos(4,3,-2+i);w.setBlock(p.below(),Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE,7),3);w.setBlock(p,Materials.CROP.getStateForAge(i==0?0:i==1?3:i==2?5:7),3);}
    var player=sp.getConnection().getServerPlayer();player.getInventory().setItem(0,new ItemStack(Conveyors.ITEMS.get(Conveyors.Kind.BELT)));var display=List.of(Materials.HAMMER,Materials.CUTTERS,Materials.COPPER_WIRE,Materials.RAW_TIN,Materials.CRUSHED_TIN,Materials.TIN_INGOT,Materials.TIN_PLATE,Materials.ROOTS,Materials.SEEDS,Materials.CAOUTCHOUC,Materials.RUBBER,Machines.PLATES.get(Items.IRON_INGOT),Machines.PLATES.get(Items.COPPER_INGOT),Machines.PLATES.get(Items.GOLD_INGOT));for(int i=0;i<display.size();i++)player.getInventory().setItem(9+i,new ItemStack(display.get(i)));
   });sp.getConnection().waitForChunksRender();c.waitTicks(30);c.getInput().lookAt(new BlockPos(0,3,0));c.takeScreenshot("kontur-materials-machines");
   c.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_G);c.waitTicks(5);c.computeOnClient(client->{if(ConveyorItem.mode(client.player)!=1)throw new AssertionError("G did not select ascent");return true;});sp.getServer().runOnServer(server->{if(ConveyorItem.mode(sp.getConnection().getServerPlayer())!=1)throw new AssertionError("G mode did not reach server");});
   c.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_G);c.waitTicks(5);c.computeOnClient(client->{if(ConveyorItem.mode(client.player)!=-1)throw new AssertionError("G did not select descent");return true;});c.getInput().lookAt(new BlockPos(1,2,3));c.takeScreenshot("kontur-descent-preview");
   sp.getServer().runCommand("gamemode survival @a");c.waitFor(client->!client.player.isCreative());c.getInput().pressKey(options->options.keyInventory);c.waitForScreen(net.minecraft.client.gui.screens.inventory.InventoryScreen.class);c.waitTicks(5);c.takeScreenshot("kontur-materials-inventory");c.setScreen(()->null);sp.getServer().runCommand("gamemode creative @a");c.waitFor(client->client.player.isCreative());
   sp.getServer().runOnServer(server->sp.getConnection().getServerPlayer().openMenu((ReinforcedChestEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(2,3,0))));c.waitForScreen(dev.admutium.techmod.client.storage.ChestScreen.class);c.waitTicks(5);c.takeScreenshot("kontur-reinforced-chest-screen");c.setScreen(()->null);
   sp.getServer().runOnServer(server->sp.getConnection().getServerPlayer().openMenu((MachineBlockEntity)sp.getConnection().getServerLevel().getBlockEntity(new BlockPos(0,3,0))));c.waitForScreen(dev.admutium.techmod.client.machine.MachineScreen.class);c.waitTicks(5);c.takeScreenshot("kontur-metal-cutter-screen");c.setScreen(()->null);
   c.computeOnClient(client->{client.player.getInventory().setSelectedSlot(1);return true;});sp.getServer().runCommand("tp @a 5 3 3");c.waitFor(client->client.player.getX()>4);c.getInput().lookAt(new BlockPos(4,3,0));c.waitTicks(8);c.takeScreenshot("kontur-dandelion-stages");
   // Opening the actual mod tab builds its display list and catches duplicate item registration.
   c.computeOnClient(client->{net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(client.player.connection.enabledFeatures(),true,client.level.registryAccess());for(var item:Materials.CREATIVE){String name=new ItemStack(item).getHoverName().getString();if(name.startsWith("item.techmod.")||name.startsWith("block.techmod."))throw new AssertionError("Missing translation: "+name);}return true;});
  }
 }
}
