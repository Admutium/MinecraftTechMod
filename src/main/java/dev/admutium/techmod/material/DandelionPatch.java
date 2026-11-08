package dev.admutium.techmod.material;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
/** 3–6 distinct valid positions in the source chunk; never queries or modifies a neighbour chunk. */
public final class DandelionPatch implements Feature {
 public static final MapCodec<DandelionPatch> CODEC=MapCodec.unit(DandelionPatch::new);
 @Override public MapCodec<DandelionPatch> codec(){return CODEC;}
 @Override public boolean place(WorldGenLevel w,ChunkGenerator generator,RandomSource random,BlockPos origin){
  int target=3+random.nextInt(4),placed=0;int minX=(origin.getX()>>4)<<4,minZ=(origin.getZ()>>4)<<4;
  int cx=minX+4+random.nextInt(8),cz=minZ+4+random.nextInt(8);
  for(int i=0;i<32&&placed<target;i++){
   int x=cx+random.nextInt(7)-3,z=cz+random.nextInt(7)-3;
   var p=new BlockPos(x,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z),z);
   var state=Materials.CROP.getStateForAge(7);
   // FEATURES runs before LIGHT. Crop survival reads skylight, which is still zero here.
   // Validate the surface substrate directly; ordinary placed/farmed crops retain their light rule.
   if(w.isEmptyBlock(p)&&RubberDandelion.wildSoil(w.getBlockState(p.below()))){w.setBlock(p,state,2);placed++;}
  }return placed>0;
 }
}
