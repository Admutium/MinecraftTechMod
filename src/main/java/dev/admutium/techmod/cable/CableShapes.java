package dev.admutium.techmod.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CableShapes {
    private CableShapes() {}
    public static VoxelShape build(BlockGetter level,BlockPos pos,CableSnapshot snapshot) {
        var boxes=CableHitGeometry.boxes(pos.getX(),pos.getY(),pos.getZ(),snapshot,n -> {
            if (!(level.getBlockEntity(new BlockPos(n.x(),n.y(),n.z())) instanceof CableBlockEntity be)) return false;
            CableSnapshot neighbor=be.getRenderData();
            return neighbor.has(n) || neighbor.holder(n.face());
        });
        return fromBoxes(boxes);
    }
    public static VoxelShape fromBoxes(java.util.List<CableHitGeometry.Box> boxes) {
        // Fill one bounded 4096-bit grid directly. No chained voxel-shape boolean unions.
        var voxels=new BitSetDiscreteVoxelShape(16,16,16);
        for (var b:boxes) {
            for(int x=b.minX();x<b.maxX();x++) for(int y=b.minY();y<b.maxY();y++) for(int z=b.minZ();z<b.maxZ();z++)
                voxels.fill(x,y,z);
        }
        return new CubeVoxelShape(voxels);
    }
}
