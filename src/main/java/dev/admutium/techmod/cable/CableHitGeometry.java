package dev.admutium.techmod.cable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Selection boxes use a fixed 1/16 grid: never an unbounded voxel coordinate cross product. */
public final class CableHitGeometry {
    public static final int GRID=16;
    private CableHitGeometry() {}
    public record Box(int minX,int minY,int minZ,int maxX,int maxY,int maxZ) {}
    public static List<Box> boxes(int x,int y,int z,CableSnapshot raw,Predicate<Node> exists) {
        CableSnapshot data=raw.centered();
        Vec origin=new Vec(x,y,z);
        List<Box> result=new ArrayList<>();
        for (Face face:Face.values()) {
            Node node=CableLayout.placement(x,y,z,face);
            if (!data.has(node) && !data.holder(face)) continue;
            add(result,node.point(),node.point(),origin,data.holder(face)?.145:.12);
            for (var link:SurfaceTopology.connections(node,exists)) {
                List<Vec> path=link.path();
                for (int i=1;i<path.size();i++) add(result,path.get(i-1),path.get(i),origin,Node.RADIUS+.01);
            }
        }
        for (CableSpan span:data.spans()) {
            List<Vec> points=span.points();
            for (int i=1;i<points.size();i++) add(result,points.get(i-1),points.get(i),origin,Node.RADIUS);
        }
        return List.copyOf(result);
    }
    private static void add(List<Box> output,Vec worldA,Vec worldB,Vec origin,double r) {
        Vec a=worldA.subtract(origin),b=worldB.subtract(origin);
        int minX=lower(Math.min(a.x(),b.x())-r),minY=lower(Math.min(a.y(),b.y())-r),minZ=lower(Math.min(a.z(),b.z())-r);
        int maxX=upper(Math.max(a.x(),b.x())+r),maxY=upper(Math.max(a.y(),b.y())+r),maxZ=upper(Math.max(a.z(),b.z())+r);
        if (minX>=maxX || minY>=maxY || minZ>=maxZ) return;
        Box box=new Box(minX,minY,minZ,maxX,maxY,maxZ);
        if (!output.contains(box)) output.add(box);
    }
    private static int lower(double value) { return Math.clamp((int)Math.floor(value*GRID),0,GRID); }
    private static int upper(double value) { return Math.clamp((int)Math.ceil(value*GRID),0,GRID); }
}
