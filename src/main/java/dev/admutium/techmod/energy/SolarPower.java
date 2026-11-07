package dev.admutium.techmod.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;

/** Potential sunlight, separate from energy accepted into a full buffer. */
public final class SolarPower {
 public static final long CAPACITY=10*Energy.UNIT, MAX_PER_TICK=450, PORT_RATE=1800;
 private SolarPower(){}
 public static double efficiency(long time,double weather){return Math.max(0,Math.sin(Math.PI*Math.floorMod(time,24000L)/12000.0))*Math.clamp(weather,0,1);}
 public static boolean skyOpen(ServerLevel w,BlockPos p){
  int top=w.getHeight(Heightmap.Types.WORLD_SURFACE,p.getX(),p.getZ());
  var cursor=new BlockPos.MutableBlockPos();
  for(int y=p.getY()+1;y<top;y++){
   cursor.set(p.getX(),y,p.getZ());var s=w.getBlockState(cursor);
   if(s.getLightDampening()>0||s.is(BlockTags.LEAVES)||(!s.isAir()&&!(s.getBlock() instanceof net.minecraft.world.level.block.TransparentBlock)&&!s.getCollisionShape(w,cursor).isEmpty()))return false;
  }return true;
 }
 public static double weather(ServerLevel w,BlockPos p){
  if(w.getBiome(p).value().getPrecipitationAt(p,w.getSeaLevel())==Biome.Precipitation.NONE)return 1;
  double rain=w.getRainLevel(1),thunder=w.getThunderLevel(1);
  return (1-.6*rain)*(1-.75*thunder);
 }
 public static boolean hasSun(ServerLevel w){return w.dimensionType().hasSkyLight()&&w.dimensionType().skybox()==DimensionType.Skybox.OVERWORLD;}
}
