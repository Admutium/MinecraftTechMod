package dev.admutium.techmod.client.conveyor;
import dev.admutium.techmod.conveyor.*;
import dev.admutium.techmod.machine.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.gizmos.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.network.chat.Component;
public final class ConveyorPreview {
 private static volatile java.util.List<BlockPos> COVERAGE=java.util.List.of();
 private static boolean covered(Minecraft c,BlockPos p){
  for(var d:Direction.values())for(int y=-1;y<=1;y++){var at=p.relative(d).offset(0,y,0);if(c.level.hasChunkAt(at)&&c.level.getBlockEntity(at) instanceof ConveyorEntity be&&be.drivePos!=null&&be.driveDistance<15&&c.level.getBlockEntity(be.drivePos) instanceof MachineBlockEntity drive&&drive.driveLoad<16)return true;}return false;
 }
 public static void init(){
  ClientTickEvents.END_CLIENT_TICK.register(c->{if(c.player==null||c.level==null||c.gui.screen()!=null||c.player.tickCount%10!=0||!(c.hitResult instanceof BlockHitResult hit))return;
   var coverage=new java.util.ArrayList<BlockPos>();var at=hit.getBlockPos();
   if(c.level.getBlockEntity(at) instanceof MachineBlockEntity driver&&driver.kind==MachineKind.DRIVE){for(int x=(at.getX()-16)>>4;x<=(at.getX()+16)>>4;x++)for(int z=(at.getZ()-16)>>4;z<=(at.getZ()+16)>>4;z++){var chunk=c.level.getChunk(x,z,net.minecraft.world.level.chunk.status.ChunkStatus.FULL,false);if(chunk instanceof net.minecraft.world.level.chunk.LevelChunk loaded)for(var entity:loaded.getBlockEntities().values())if(entity instanceof ConveyorEntity part&&at.equals(part.drivePos))coverage.add(part.getBlockPos());}}
   COVERAGE=java.util.List.copyOf(coverage);
   if(c.level.getBlockEntity(hit.getBlockPos()) instanceof ConveyorEntity be)c.player.sendOverlayMessage(Component.translatable("conveyor.techmod."+be.statusKey()));
   else if(c.level.getBlockEntity(hit.getBlockPos()) instanceof MachineBlockEntity be&&be.kind==MachineKind.DRIVE)c.player.sendOverlayMessage(Component.translatable("screen.techmod.drive_load",be.driveLoad));
  });
  LevelRenderEvents.BEFORE_GIZMOS.register(ctx->{var c=Minecraft.getInstance();if(c.level==null||c.player==null||c.gui.screen()!=null||!(c.hitResult instanceof BlockHitResult hit))return;
   var item=c.player.getMainHandItem().getItem();boolean placing=item instanceof ConveyorItem||item instanceof MachineItem m&&((MachineBlock)m.getBlock()).kind==MachineKind.DRIVE;
   if(!placing)return;
   try(var scope=c.levelRenderer.collectPerFrameRenderThreadGizmos()){
    var context=new BlockPlaceContext(new UseOnContext(c.player,InteractionHand.MAIN_HAND,hit));var block=((net.minecraft.world.item.BlockItem)item).getBlock();var state=block.getStateForPlacement(context);var pos=context.getClickedPos();
    boolean valid=state!=null&&c.level.getBlockState(pos).canBeReplaced(context);int color=!valid?0xffed7755:block instanceof ConveyorBlock&&!covered(c,pos)?0xffe5b34c:0xff76db5b;
    if(state!=null){for(var box:state.getShape(c.level,pos,CollisionContext.empty()).toAabbs())Gizmos.cuboid(box.move(pos),GizmoStyle.strokeAndFill(color,1.5f,(color&0x00ffffff)|0x20000000));if(block instanceof ConveyorBlock){var end=ConveyorBlock.output(pos,state);Gizmos.arrow(Vec3.atCenterOf(pos).add(0,-.15,0),Vec3.atCenterOf(end).add(0,-.15,0),color,2);}}
    else Gizmos.cuboid(new AABB(pos),GizmoStyle.stroke(color));
    // Actual assigned sections, including branches, are visible when inspecting a drive.
    var at=hit.getBlockPos();if(c.level.getBlockEntity(at) instanceof MachineBlockEntity drive&&drive.kind==MachineKind.DRIVE){
     for(var p:COVERAGE)Gizmos.cuboid(new AABB(p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+.3,p.getZ()+1),GizmoStyle.stroke(0xff76db5b));
    }
   }
  });
 }
}
