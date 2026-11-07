package dev.admutium.techmod.cable;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CableVoxelPerformanceTest {
    @Test void actualMinecraftVoxelShapeRemainsSmallForDenseSuspensions() {
        List<CableSpan> spans=new ArrayList<>();
        for(int i=0;i<8;i++) spans.add(new CableSpan(new UUID(0,i),
                CableLayout.placement(-2,4,0,Face.NORTH),CableLayout.placement(2,4,i%2,Face.NORTH)));
        var snapshot=new CableSnapshot((1L<<54)-1,0,spans);
        var boxes=CableHitGeometry.boxes(0,4,0,snapshot,n -> true);
        long before=System.nanoTime();
        var shape=CableShapes.fromBoxes(boxes);
        long built=System.nanoTime();
        assertFalse(shape.isEmpty());
        assertTrue(shape.toAabbs().size()<512);
        var cache=new CableShapeCache<net.minecraft.world.phys.shapes.VoxelShape>();
        for(int i=0;i<100_000;i++) {
            var selected=cache.get(() -> shape);
            selected.bounds();
        }
        long after=System.nanoTime();
        final int[] edges={0};
        for(int i=0;i<1000;i++) shape.forAllEdges((x1,y1,z1,x2,y2,z2) -> edges[0]++);
        long outlined=System.nanoTime();
        assertTrue(edges[0]>0);
        // A new group should also be cheap after one-time Minecraft class initialization.
        for(int i=0;i<1000;i++) CableShapes.fromBoxes(boxes);
        long rebuilt=System.nanoTime();
        System.out.printf(Locale.ROOT,"Dense cable: %d input boxes, %d final boxes; cold build %.3f ms; 100000 cached queries %.3f ms; 1000 full outlines %.3f ms; 1000 warm builds %.3f ms%n",
                boxes.size(),shape.toAabbs().size(),(built-before)/1e6,(after-built)/1e6,(outlined-after)/1e6,(rebuilt-outlined)/1e6);
    }
}
