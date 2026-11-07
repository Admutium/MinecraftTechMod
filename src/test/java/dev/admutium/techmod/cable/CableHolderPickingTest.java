package dev.admutium.techmod.cable;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CableHolderPickingTest {
    @Test void frontOfQuantizedHolderWasOutsideOldDistanceThreshold() {
        Node n=CableLayout.placement(0,0,0,Face.DOWN);
        Vec front=new Vec(.5,.25,.5);
        assertTrue(front.subtract(n.point()).length()>.18,"Reproduce the 0.1.1 regression");
        assertEquals(n,CablePicking.holder(0,0,0,new CableSnapshot(0,1,List.of()),front));
    }
    @Test void everyPointOnEveryHolderBoxFaceCanBeSelected() {
        for(Face face:Face.values()) {
            Node node=CableLayout.placement(-3,4,9,face);
            var snapshot=new CableSnapshot(0,1<<face.ordinal(),List.of());
            var box=CableHitGeometry.mountBox(node,true);
            double[] lo={box.minX()/16.0,box.minY()/16.0,box.minZ()/16.0};
            double[] hi={box.maxX()/16.0,box.maxY()/16.0,box.maxZ()/16.0};
            for(int axis=0;axis<3;axis++) for(int side=0;side<2;side++)
                for(int u=0;u<=4;u++) for(int v=0;v<=4;v++) {
                    int a=(axis+1)%3,b=(axis+2)%3;
                    double[] p={(lo[0]+hi[0])/2,(lo[1]+hi[1])/2,(lo[2]+hi[2])/2};
                    p[axis]=side==0?lo[axis]:hi[axis];
                    p[a]=lo[a]+(hi[a]-lo[a])*u/4;
                    p[b]=lo[b]+(hi[b]-lo[b])*v/4;
                    Vec hit=node.origin().add(new Vec(p[0],p[1],p[2]));
                    assertEquals(node,CablePicking.holder(node.x(),node.y(),node.z(),snapshot,hit));
                }
        }
    }
    @Test void emptySpaceAndOtherFacesAreNotHolders() {
        var snapshot=new CableSnapshot(0,1<<Face.NORTH.ordinal(),List.of());
        assertNull(CablePicking.holder(0,0,0,snapshot,new Vec(.5,.5,.8)));
        assertNull(CablePicking.holder(0,0,0,CableSnapshot.EMPTY,new Vec(.5,.5,.125)));
    }
    @Test void multipleHoldersSelectTheClickedFace() {
        var snapshot=new CableSnapshot(0,63,List.of());
        for(Face f:Face.values()) {
            Node n=CableLayout.placement(0,0,0,f);
            Vec front=n.point().subtract(f.normal().scale(.1875));
            assertEquals(n,CablePicking.holder(0,0,0,snapshot,front));
        }
    }
}
