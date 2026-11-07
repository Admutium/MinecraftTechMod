package dev.admutium.techmod.machine;
import dev.admutium.techmod.TechMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import java.util.EnumMap;
public final class MachineSounds {
 public static final EnumMap<MachineKind,SoundEvent> LOOPS=new EnumMap<>(MachineKind.class);
 public static void init(){for(var kind:new MachineKind[]{MachineKind.GENERATOR,MachineKind.FURNACE,MachineKind.CRUSHER}){
  var id=TechMod.id(kind.id+"_loop");LOOPS.put(kind,Registry.register(BuiltInRegistries.SOUND_EVENT,id,SoundEvent.createFixedRangeEvent(id,6)));
 }}
 private MachineSounds(){}
}
