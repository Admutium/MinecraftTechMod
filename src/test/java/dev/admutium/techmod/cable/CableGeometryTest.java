package dev.admutium.techmod.cable;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CableGeometryTest {
    private static Node node(int x,int y,int z,Face face) { return new Node(x,y,z,face,1,1); }
    @Test void all54SlotsHaveUniqueBitsAndRoundTrip() {
        long bits=0;
        for (int i=0;i<54;i++) {
            Node n=Node.fromIndex(-12,8,17,i);
            assertEquals(i,n.index());
            assertEquals(n,Node.decode(n.encode()));
            assertEquals(0,bits & n.bit());
            bits|=n.bit();
        }
        assertEquals(54,Long.bitCount(bits));
    }
    @Test void placementSnapsToAllFaceSlotsIncludingNegativeCoordinates() {
        for (Face face:Face.values()) for (int u=0;u<3;u++) for (int v=0;v<3;v++) {
            Node n=new Node(-6,-2,9,face,u,v);
            assertEquals(n,Node.nearest(-6,-2,9,face,n.point()));
        }
    }
    @Test void isolatedWireHasAStableCompactCoil() {
        for (Face face:Face.values()) {
            Node n=node(0,0,0,face);
            assertTrue(SurfaceTopology.connections(n,other -> other.equals(n)).isEmpty());
            assertEquals(SurfaceTopology.coil(n),SurfaceTopology.coil(n));
            for (Vec p:SurfaceTopology.coil(n)) {
                assertEquals(n.point().coordinate(face.axis),p.coordinate(face.axis),1e-9);
                assertTrue(p.subtract(n.point()).length()+Node.RADIUS<=.125);
            }
        }
    }
    @Test void straightCornersTeesAndCrossesHaveCorrectDegree() {
        Node center=node(0,0,0,Face.DOWN);
        List<Node> arms=List.of(new Node(0,0,0,Face.DOWN,0,1),new Node(0,0,0,Face.DOWN,2,1),
                new Node(0,0,0,Face.DOWN,1,0),new Node(0,0,0,Face.DOWN,1,2));
        for (int count=1;count<=4;count++) {
            Set<Node> occupied=new HashSet<>(arms.subList(0,count)); occupied.add(center);
            assertEquals(count,SurfaceTopology.connections(center,occupied::contains).size());
        }
    }
    @Test void coplanarConnectionsAcrossCellBoundariesAreReciprocal() {
        for (Face face:Face.values()) for (int axis:new int[]{face.uAxis(),face.vAxis()}) for (int sign:new int[]{-1,1}) {
            int edge=sign<0 ? 0:2;
            Node a=new Node(0,0,0,face,axis==face.uAxis()?edge:1,axis==face.vAxis()?edge:1);
            Vec delta=Face.along(axis,sign).normal();
            Node b=new Node((int)delta.x(),(int)delta.y(),(int)delta.z(),face,
                    axis==face.uAxis()?2-edge:1,axis==face.vAxis()?2-edge:1);
            assertReciprocal(a,b);
        }
    }
    @Test void clickingCentersOfAdjacentBlocksConnectsWithoutIntermediatePlacements() {
        for (Face face:Face.values()) for (int axis:new int[]{face.uAxis(),face.vAxis()}) {
            Vec step=Face.along(axis,1).normal();
            assertReciprocal(node(0,0,0,face),node((int)step.x(),(int)step.y(),(int)step.z(),face));
        }
    }
    @Test void centeredNodesConnectAroundBothKindsOfCorner() {
        assertReciprocal(node(0,0,0,Face.DOWN),node(0,0,0,Face.NORTH));
        assertReciprocal(node(0,0,0,Face.DOWN),node(1,-1,0,Face.WEST));
    }
    @Test void innerCornersAreReciprocalOnEveryFace() {
        for (Face face:Face.values()) for (int axis:new int[]{face.uAxis(),face.vAxis()}) for (int sign:new int[]{-1,1}) {
            Node a=new Node(0,0,0,face,axis==face.uAxis()?(sign<0?0:2):1,axis==face.vAxis()?(sign<0?0:2):1);
            Face second=Face.along(axis,sign);
            Vec point=a.point().with(axis,sign<0?Node.HEIGHT:1-Node.HEIGHT).with(face.axis,face.sign<0?.25:.75);
            Node b=Node.nearest(0,0,0,second,point);
            assertReciprocal(a,b);
        }
    }
    @Test void outerCornersAreReciprocalOnEveryFace() {
        for (Face face:Face.values()) for (int axis:new int[]{face.uAxis(),face.vAxis()}) for (int sign:new int[]{-1,1}) {
            Node a=new Node(0,0,0,face,axis==face.uAxis()?(sign<0?0:2):1,axis==face.vAxis()?(sign<0?0:2):1);
            Vec origin=face.normal().add(Face.along(axis,sign).normal());
            Vec point=a.point().with(axis,sign<0?-Node.HEIGHT:1+Node.HEIGHT).add(face.normal().scale(.3125));
            Node b=Node.nearest((int)origin.x(),(int)origin.y(),(int)origin.z(),Face.along(axis,-sign),point);
            assertReciprocal(a,b);
        }
    }
    private static void assertReciprocal(Node a,Node b) {
        Set<Node> occupied=Set.of(a,b);
        var forward=SurfaceTopology.connections(a,occupied::contains);
        var reverse=SurfaceTopology.connections(b,occupied::contains);
        assertEquals(1,forward.size(),a+" -> "+b);
        assertEquals(1,reverse.size(),b+" -> "+a);
        assertEquals(b,forward.getFirst().target());
        assertEquals(a,reverse.getFirst().target());
        List<Vec> reversed=new ArrayList<>(reverse.getFirst().path()); Collections.reverse(reversed);
        assertEquals(forward.getFirst().path(),reversed,"Both ends must agree on the same physical path");
    }
    @Test void oppositeFacesNeverConnectThroughABlock() {
        Node a=node(0,0,0,Face.DOWN),b=node(0,-2,0,Face.UP);
        assertTrue(SurfaceTopology.connections(a,Set.of(a,b)::contains).isEmpty());
    }
    @Test void spanLimitIsEuclideanNotManhattanOrArcLength() {
        Node a=node(0,2,0,Face.DOWN);
        CableSpan diagonal=new CableSpan(UUID.randomUUID(),a,node(3,2,4,Face.DOWN));
        assertTrue(diagonal.length()>5);
        assertThrows(IllegalArgumentException.class,()->new CableSpan(UUID.randomUUID(),a,node(4,2,4,Face.DOWN)));
        assertThrows(IllegalArgumentException.class,()->new CableSpan(UUID.randomUUID(),a,a));
    }
    @Test void sagPreservesMountingPointsAndIsBelowStraightLine() {
        CableSpan span=new CableSpan(UUID.randomUUID(),node(0,4,0,Face.DOWN),node(4,5,0,Face.DOWN));
        assertEquals(span.start().point(),span.point(0));
        assertEquals(span.end().point(),span.point(1));
        Vec straightMiddle=span.start().point().add(span.end().point()).scale(.5);
        assertTrue(span.point(.5).y()<straightMiddle.y());
        assertEquals(straightMiddle.x(),span.point(.5).x(),1e-9);
        assertEquals(span,CableSpan.decode(span.encode()));
    }
    @Test void verticalSpanDoesNotInventHorizontalDisplacement() {
        CableSpan span=new CableSpan(UUID.randomUUID(),node(0,0,0,Face.WEST),node(0,4,0,Face.WEST));
        assertEquals(4,span.length(),1e-8);
        assertEquals(span.start().point().x(),span.point(.5).x(),1e-9);
    }
    @Test void corruptDataAndOffCenterHoldersAreRejected() {
        assertThrows(RuntimeException.class,()->Node.decode("broken"));
        assertThrows(RuntimeException.class,()->CableSpan.decode("broken"));
        assertThrows(IllegalArgumentException.class,()->new Node(0,0,0,Face.DOWN,3,0));
        assertThrows(IllegalArgumentException.class,()->new CableSpan(UUID.randomUUID(),new Node(0,0,0,Face.DOWN,0,0),node(1,0,0,Face.DOWN)));
    }
    @Test void pickingFindsTheCableBetweenMountingNodes() {
        Vec a=new Vec(.5,.0625,.5),b=new Vec(1.5,.0625,.5);
        assertEquals(0,new Vec(1,.0625,.5).distanceToSegment(a,b),1e-9);
        assertEquals(.1,new Vec(1,.1625,.5).distanceToSegment(a,b),1e-9);
        assertEquals(.5,new Vec(2,.0625,.5).distanceToSegment(a,b),1e-9);
        assertEquals(1,new Vec(1.5,.0625,.5).distanceToSegment(a,a),1e-9);
    }
}
