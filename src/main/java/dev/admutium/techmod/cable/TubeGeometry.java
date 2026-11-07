package dev.admutium.techmod.cable;

import java.util.ArrayList;
import java.util.List;

/** Shared rings, not independent cylinders: adjacent segments cannot leave open wedges. */
public final class TubeGeometry {
    public static final int SIDES=8;
    private TubeGeometry() {}
    public record Ring(Vec center,List<Vec> vertices) {}
    public static List<Ring> rings(List<Vec> path,double radius) {
        if (path.size()<2) return List.of();
        List<Ring> result=new ArrayList<>(path.size());
        Vec previousU=null;
        for (int i=0;i<path.size();i++) {
            Vec tangent;
            if (i==0) tangent=path.get(1).subtract(path.getFirst()).normalized();
            else if (i==path.size()-1) tangent=path.get(i).subtract(path.get(i-1)).normalized();
            else tangent=path.get(i+1).subtract(path.get(i)).normalized()
                    .add(path.get(i).subtract(path.get(i-1)).normalized()).normalized();
            Vec u=previousU==null ? tangent.cross(Math.abs(tangent.y())<.9 ? new Vec(0,1,0):new Vec(1,0,0)).normalized()
                    : previousU.subtract(tangent.scale(previousU.dot(tangent)));
            if (u.length()<1e-6) u=tangent.cross(Math.abs(tangent.y())<.9 ? new Vec(0,1,0):new Vec(1,0,0));
            u=u.normalized();
            Vec v=tangent.cross(u).normalized();
            List<Vec> vertices=new ArrayList<>(SIDES);
            for (int side=0;side<SIDES;side++) {
                double angle=side*Math.PI*2/SIDES;
                vertices.add(path.get(i).add(u.scale(Math.cos(angle)*radius)).add(v.scale(Math.sin(angle)*radius)));
            }
            result.add(new Ring(path.get(i),List.copyOf(vertices)));
            previousU=u;
        }
        return List.copyOf(result);
    }

    public record ConnectionPath(List<Vec> points,int split,boolean firstHalf) {}
    /** Both endpoints independently produce exactly the same full path and seam ring. */
    public static ConnectionPath connection(Node source,SurfaceTopology.Connection connection) {
        Node target=connection.target();
        boolean first=compare(source,target)<0;
        List<Vec> path=first ? connection.path() : connection.path().reversed();
        double total=0;
        for (int i=1;i<path.size();i++) total+=path.get(i).subtract(path.get(i-1)).length();
        List<Vec> splitPath=new ArrayList<>();splitPath.add(path.getFirst());
        double remaining=total/2;
        int split=-1;
        for (int i=1;i<path.size();i++) {
            Vec a=path.get(i-1),b=path.get(i);double length=b.subtract(a).length();
            if (split<0 && remaining<=length+1e-9) {
                Vec middle=a.add(b.subtract(a).scale(Math.clamp(remaining/length,0,1)));
                if (middle.subtract(splitPath.getLast()).length()>1e-9) splitPath.add(middle);
                split=splitPath.size()-1;
                if (middle.subtract(b).length()>1e-9) splitPath.add(b);
            } else { splitPath.add(b); remaining-=length; }
        }
        // Deterministic shallow bends. The center seam is evaluated from this full shared path.
        Node canonical=first?source:target;
        List<Vec> bent=new ArrayList<>();bent.add(splitPath.getFirst());
        for (int i=1;i<splitPath.size();i++) {
            Vec a=splitPath.get(i-1),b=splitPath.get(i);
            Vec sideways=b.subtract(a).normalized().cross(canonical.face().normal());
            double sign=((canonical.x()^canonical.y()^canonical.z()^canonical.index())&1)==0?1:-1;
            bent.add(a.add(b).scale(.5).add(sideways.scale(.006*sign)));
            bent.add(b);
        }
        return new ConnectionPath(List.copyOf(bent),split*2,first);
    }
    private static int compare(Node a,Node b) {
        int c=Integer.compare(a.x(),b.x());if(c!=0)return c;
        c=Integer.compare(a.y(),b.y());if(c!=0)return c;
        c=Integer.compare(a.z(),b.z());return c!=0?c:Integer.compare(a.index(),b.index());
    }
}
