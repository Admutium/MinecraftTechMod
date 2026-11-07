package dev.admutium.techmod.cable;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class CableRegressionTest {
    private static Node center(int x,int y,int z,Face f) { return CableLayout.placement(x,y,z,f); }
    @Test void oneRunPerFaceAndLegacySurplusIsRefundable() {
        long legacy=(1L<<54)-1;
        long normalized=CableLayout.centered(legacy,0);
        assertEquals(6,Long.bitCount(normalized));
        assertEquals(48,CableLayout.surplus(legacy,0));
        assertEquals(normalized,CableLayout.centered(normalized,0));
        for(Face f:Face.values()) assertTrue((normalized & center(0,0,0,f).bit())!=0);
        assertEquals(54,CableLayout.surplus(legacy,63));
        assertEquals(0,CableLayout.centered(legacy,63));
    }
    @Test void clickHeightDoesNotChangePlacementAndExistingFaceRejectsMoreSlots() {
        for(Face f:Face.values()) {
            Node n=center(2,3,4,f);
            for(int slot=0;slot<9;slot++) {
                Node legacy=Node.fromIndex(2,3,4,f.ordinal()*9+slot);
                long normalized=CableLayout.centered(legacy.bit(),0);
                assertEquals(n.bit(),normalized);
                assertTrue(CableLayout.hasFace(normalized,f));
            }
        }
    }
    @Test void connectionHalvesUseIdenticalFullPathsAndSharedSeamRings() {
        for(Face f:Face.values()) for(int axis:new int[]{f.uAxis(),f.vAxis()}) for(int sign:new int[]{-1,1}) {
            Node a=center(0,0,0,f);
            Face direction=Face.along(axis,sign);
            Vec adjacent=direction.normal();
            Vec diagonal=adjacent.add(f.normal());
            for(Node b:List.of(center((int)adjacent.x(),(int)adjacent.y(),(int)adjacent.z(),f),
                    center(0,0,0,direction),center((int)diagonal.x(),(int)diagonal.y(),(int)diagonal.z(),direction.opposite()))) {
                Set<Node> occupied=Set.of(a,b);
                var ab=SurfaceTopology.connections(a,occupied::contains).getFirst();
                var ba=SurfaceTopology.connections(b,occupied::contains).getFirst();
                var first=TubeGeometry.connection(a,ab);var second=TubeGeometry.connection(b,ba);
                assertEquals(first.points(),second.points());
                assertEquals(first.split(),second.split());
                assertNotEquals(first.firstHalf(),second.firstHalf());
                var ringsA=TubeGeometry.rings(first.points(),Node.RADIUS);
                var ringsB=TubeGeometry.rings(second.points(),Node.RADIUS);
                assertEquals(ringsA.get(first.split()),ringsB.get(second.split()));
            }
        }
    }
    @Test void coilAndHangingCableHaveOneSharedRingAtEveryBend() {
        Node a=center(0,4,0,Face.DOWN),b=center(4,4,0,Face.DOWN);
        CableSpan span=new CableSpan(UUID.randomUUID(),a,b);
        assertSame(span.points(),span.points(),"A hover must not resample the same suspended curve");
        for(List<Vec> path:List.of(SurfaceTopology.coil(a),span.points())) {
            var rings=TubeGeometry.rings(path,Node.RADIUS);
            assertEquals(path.size(),rings.size());
            for(int i=0;i<rings.size();i++) {
                assertEquals(8,rings.get(i).vertices().size());
                for(Vec p:rings.get(i).vertices()) {
                    assertTrue(Double.isFinite(p.x()) && Double.isFinite(p.y()) && Double.isFinite(p.z()));
                    assertEquals(Node.RADIUS,p.subtract(rings.get(i).center()).length(),1e-8);
                }
            }
        }
    }
    @Test void denseGroupsAndSuspensionsHaveBoundedSelectionGrid() {
        var spans=new ArrayList<CableSpan>();
        for(int i=0;i<8;i++) spans.add(new CableSpan(new UUID(0,i),center(-2,4,0,Face.NORTH),center(2,4,i%2,Face.NORTH)));
        CableSnapshot dense=new CableSnapshot((1L<<54)-1,0,spans);
        var boxes=CableHitGeometry.boxes(0,4,0,dense,n -> true);
        assertFalse(boxes.isEmpty());
        Set<Integer> coordinates=new HashSet<>();
        for(var b:boxes) {
            for(int value:new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()}) {
                assertTrue(value>=0 && value<=16);coordinates.add(value);
            }
        }
        assertTrue(coordinates.size()<=17,"Voxel coordinates must never grow with segment count");
        assertTrue(boxes.size()<150,"Do not build a box for every point of every remote cable");
    }
    @Test void oneHundredThousandHoverQueriesReuseTheSameShapeUntilInvalidated() {
        CableShapeCache<Object> cache=new CableShapeCache<>();AtomicInteger builds=new AtomicInteger();
        Object first=cache.get(() -> { builds.incrementAndGet();return new Object(); });
        for(int i=0;i<100_000;i++) assertSame(first,cache.get(() -> { fail("Hover rebuilt geometry");return null; }));
        assertEquals(1,builds.get());
        cache.invalidate();
        assertNotSame(first,cache.get(() -> { builds.incrementAndGet();return new Object(); }));
        assertEquals(2,builds.get());
    }
}
