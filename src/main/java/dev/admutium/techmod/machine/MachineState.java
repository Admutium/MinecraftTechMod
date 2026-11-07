package dev.admutium.techmod.machine;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import java.util.List;
/** Only batteries, buffer and active fuel survive as part of the machine item. */
public record MachineState(long energy,int fuelTicks,List<ItemStack> batteries) {
 public static final MachineState EMPTY=new MachineState(0,0,List.of());
 public MachineState { batteries=batteries.stream().limit(8).map(ItemStack::copy).toList(); }
 public static final Codec<MachineState> CODEC=RecordCodecBuilder.create(i->i.group(
  Codec.LONG.optionalFieldOf("energy",0L).forGetter(MachineState::energy),
  Codec.INT.optionalFieldOf("fuel_ticks",0).forGetter(MachineState::fuelTicks),
  ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("batteries",List.of()).forGetter(MachineState::batteries)
 ).apply(i,MachineState::new));
}
