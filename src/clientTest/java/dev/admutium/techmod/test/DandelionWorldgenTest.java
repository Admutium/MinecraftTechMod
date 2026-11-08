package dev.admutium.techmod.test;
import dev.admutium.techmod.material.Materials;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
/** Full noise-terrain generation, including FEATURES before lighting; no manual feature placement. */
public final class DandelionWorldgenTest implements FabricClientGameTest {
 @Override public void runTest(ClientGameTestContext c){
  try(var sp=c.worldBuilder().adjustSettings(s->{s.setSeed("42");s.setGenerateStructures(false);s.updateDimensions((r,d)->d.replaceOverworldGenerator(r,new NoiseBasedChunkGenerator(new FixedBiomeSource(r.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS)),r.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD))));}).create()){
   int[] found={0},chunks={0};BlockPos[] first={null};
   for(int cx=0;cx<12;cx++){final int xChunk=cx;sp.getServer().runOnServer(server->{var w=sp.getConnection().getServerLevel();for(int cz=0;cz<12;cz++){
    var chunk=w.getChunk(xChunk,cz);chunks[0]++;
    for(int x=xChunk*16;x<(xChunk+1)*16;x++)for(int z=cz*16;z<(cz+1)*16;z++){
     int y=w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);
     for(int dy=-1;dy<=2;dy++){var p=new BlockPos(x,y+dy,z);if(chunk.getBlockState(p).is(Materials.CROP)){found[0]++;if(first[0]==null)first[0]=p;}}
    }
   }});c.waitTick();}
   System.out.println("DANDELION_WORLDGEN chunks="+chunks[0]+" natural_plants="+found[0]);
   if(found[0]==0)throw new AssertionError("No natural rubber dandelions in 144 fresh plains chunks");
   var p=first[0];sp.getServer().runCommand("gamemode spectator @a");sp.getServer().runCommand("tp @a "+(p.getX()+.5)+" "+(p.getY()+2)+" "+(p.getZ()+4.5));c.waitTicks(20);c.getInput().lookAt(p);c.takeScreenshot("kontur-natural-dandelions");
  }
 }
}
