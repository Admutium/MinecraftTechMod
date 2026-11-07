package dev.admutium.techmod.conveyor;
import dev.admutium.techmod.machine.*;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
public final class DriveBlock extends MachineBlock {
 public DriveBlock(Properties p){super(MachineKind.DRIVE,p);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){
  for(var d:Direction.Plane.HORIZONTAL){var s=c.getLevel().getBlockState(c.getClickedPos().relative(d));if(s.getBlock() instanceof ConveyorBlock b&&b.kind==Conveyors.Kind.BELT&&!s.getValue(ConveyorBlock.UPPER)&&ConveyorBlock.slope(s)==0&&ConveyorBlock.turn(s)==0&&s.getValue(ConveyorBlock.FACING).getAxis()!=d.getAxis())return defaultBlockState().setValue(FACING,d);}
  return null;
 }
}
