package dev.admutium.techmod.material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
public final class RubberDandelion extends CropBlock {
 public RubberDandelion(Properties p){super(p);}
 @Override protected ItemLike getBaseSeedId(){return Materials.SEEDS;}
 @Override protected boolean mayPlaceOn(BlockState soil,BlockGetter w,BlockPos p){return soil.is(Blocks.FARMLAND)||soil.is(Blocks.GRASS_BLOCK)||soil.is(Blocks.DIRT);}
 @Override protected boolean canSurvive(BlockState s,LevelReader w,BlockPos p){
  var soil=w.getBlockState(p.below());return hasSufficientLight(w,p)&&(soil.is(Blocks.FARMLAND)||isMaxAge(s)&&(soil.is(Blocks.GRASS_BLOCK)||soil.is(Blocks.DIRT)));
 }
}
