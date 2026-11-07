package dev.admutium.techmod.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

/** Implement on future machine blocks. Acceptance controls the visible terminal, not energy transfer. */
public interface CablePort {
    boolean acceptsCable(BlockGetter level, BlockPos pos, Direction side);
    // TODO(energy): add network membership, voltage and transfer after the wiring prototype is validated.
}
