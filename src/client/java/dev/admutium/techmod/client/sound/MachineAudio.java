package dev.admutium.techmod.client.sound;
import dev.admutium.techmod.machine.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.*;
import java.util.*;
/** Bounded nearby-chunk discovery and at most twelve short-range positional loops. */
public final class MachineAudio {
 private static final Map<BlockPos,Loop> LOOPS=new HashMap<>();private static ClientLevel world;private static int ticks;
 public static void init(){
  MachineAudioConfig.load();ClientTickEvents.END_CLIENT_TICK.register(MachineAudio::tick);
  ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{if(screen instanceof SoundOptionsScreen)Screens.getWidgets(screen).add(Button.builder(Component.translatable("options.techmod.sounds"),b->client.setScreenAndShow(new MachineAudioScreen(screen))).bounds(w-154,5,148,20).build());});
 }
 private static void tick(Minecraft client){
  if(world!=client.level){for(var loop:LOOPS.values())client.getSoundManager().stop(loop);LOOPS.clear();world=client.level;}
  if(world==null||client.player==null)return;
  LOOPS.values().removeIf(Loop::isStopped);
  if(++ticks%10!=0||MachineAudioConfig.volume<=0)return;
  var eye=client.player.getEyePosition();var nearby=new ArrayList<MachineBlockEntity>();
  for(int x=((int)Math.floor(eye.x-6))>>4;x<=((int)Math.floor(eye.x+6))>>4;x++)for(int z=((int)Math.floor(eye.z-6))>>4;z<=((int)Math.floor(eye.z+6))>>4;z++){
   var chunk=world.getChunk(x,z,ChunkStatus.FULL,false);
   if(chunk instanceof LevelChunk c)for(var entity:c.getBlockEntities().values())if(entity instanceof MachineBlockEntity be&&MachineSounds.LOOPS.containsKey(be.kind)&&be.getBlockState().getValue(MachineBlock.ACTIVE)&&eye.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()))<36)nearby.add(be);
  }
  nearby.sort(Comparator.comparingDouble(be->eye.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()))));
  float mix=(float)(1/Math.sqrt(Math.max(1,Math.min(12,nearby.size()))));
  var selected=new HashSet<BlockPos>();for(int i=0;i<Math.min(12,nearby.size());i++)selected.add(nearby.get(i).getBlockPos());
  LOOPS.values().removeIf(loop->{if(!selected.contains(loop.pos)){client.getSoundManager().stop(loop);return true;}return false;});
  for(int i=0;i<Math.min(12,nearby.size());i++){
   var be=nearby.get(i);var p=be.getBlockPos();var loop=LOOPS.get(p);
   if(loop==null){loop=new Loop(world,p,be.kind);LOOPS.put(p,loop);client.getSoundManager().play(loop);}loop.mix=mix;
  }
 }
 private static final class Loop extends AbstractTickableSoundInstance {
  private final ClientLevel level;private final BlockPos pos;private final MachineKind kind;private float wall=1;private int age;private float mix=1;
  Loop(ClientLevel level,BlockPos p,MachineKind kind){super(MachineSounds.LOOPS.get(kind),SoundSource.BLOCKS,RandomSource.create());this.level=level;pos=p.immutable();this.kind=kind;x=p.getX()+.5;y=p.getY()+.5;z=p.getZ()+.5;looping=true;volume=.0001f;pitch=1;attenuation=SoundInstance.Attenuation.NONE;}
  @Override public boolean canStartSilent(){return true;}
  @Override public void tick(){
   var client=Minecraft.getInstance();if(client.level!=level||client.player==null){stop();return;}
   var eye=client.player.getEyePosition();double distance=eye.distanceTo(new Vec3(x,y,z));if(distance>=6){stop();return;}
   var be=level.getBlockEntity(pos);boolean active=be instanceof MachineBlockEntity m&&m.kind==kind&&m.getBlockState().getValue(MachineBlock.ACTIVE);
   if(++age%5==0){var hit=level.clip(new ClipContext(eye,new Vec3(x,y,z),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,client.player));wall=hit.getType()==HitResult.Type.BLOCK&&!hit.getBlockPos().equals(pos)?.2f:1;}
   float target=active?(float)(MachineAcoustics.gain(distance,MachineAudioConfig.volume,wall<1,1)*mix):0;
   volume+=(target-volume)*.25f;if(target==0&&volume<.0005f)stop();
  }
 }
 private MachineAudio(){}
}
