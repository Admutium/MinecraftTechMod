package dev.admutium.techmod.cable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Pure surface graph: coplanar runs, concave corners and convex block edges. */
public final class SurfaceTopology {
    private SurfaceTopology() {}
    public record Connection(Node target, List<Vec> path) {}

    public static List<Connection> connections(Node node, Predicate<Node> exists) {
        if (node.u()==1 && node.v()==1) return centeredConnections(node,exists);
        List<Connection> result = new ArrayList<>(4);
        for (int axis : new int[]{node.face().uAxis(), node.face().vAxis()}) {
            for (int sign : new int[]{-1, 1}) {
                int coordinate = axis == node.face().uAxis() ? node.u() : node.v();
                Node local = null;
                for (int next = coordinate + sign; next >= 0 && next <= 2; next += sign) {
                    Node candidate = new Node(node.x(), node.y(), node.z(), node.face(),
                            axis == node.face().uAxis() ? next : node.u(),
                            axis == node.face().vAxis() ? next : node.v());
                    if (exists.test(candidate)) { local = candidate; break; }
                }
                if (local != null) {
                    result.add(new Connection(local, List.of(node.point(), local.point())));
                    continue;
                }
                Vec step = Face.along(axis, sign).normal();
                Vec adjacent = node.origin().add(step);
                Node flat = null;
                for (int slot = sign < 0 ? 2 : 0; slot >= 0 && slot <= 2; slot += sign < 0 ? -1 : 1) {
                    Vec point = node.point().add(step).with(axis, adjacent.coordinate(axis) + (slot + 1) / 4.0);
                    Node candidate = Node.nearest((int) adjacent.x(), (int) adjacent.y(), (int) adjacent.z(), node.face(), point);
                    if (exists.test(candidate)) { flat = candidate; break; }
                }
                if (flat != null) {
                    result.add(new Connection(flat, List.of(node.point(), flat.point())));
                    continue;
                }
                Face innerFace = Face.along(axis, sign);
                Vec corner = node.point().with(axis, node.origin().coordinate(axis) + (sign < 0 ? Node.HEIGHT : 1 - Node.HEIGHT));
                Vec innerPoint = corner.with(node.face().axis,
                        node.origin().coordinate(node.face().axis) + (node.face().sign < 0 ? .25 : .75));
                Node inner = null;
                for (int slot = node.face().sign < 0 ? 0 : 2; slot >= 0 && slot <= 2; slot += node.face().sign < 0 ? 1 : -1) {
                    Node candidate = Node.nearest(node.x(),node.y(),node.z(),innerFace,innerPoint.with(node.face().axis,
                            node.origin().coordinate(node.face().axis) + (slot+1)/4.0));
                    if (exists.test(candidate)) { inner = candidate; break; }
                }
                if (inner != null) {
                    result.add(new Connection(inner, List.of(node.point(), corner, inner.point())));
                    continue;
                }
                Vec diagonal = adjacent.add(node.face().normal());
                Vec outerCorner = node.point().with(axis,
                        node.origin().coordinate(axis) + (sign < 0 ? -Node.HEIGHT : 1 + Node.HEIGHT));
                Node outer = null;
                for (int slot = node.face().sign < 0 ? 2 : 0; slot >= 0 && slot <= 2; slot += node.face().sign < 0 ? -1 : 1) {
                    Vec point = outerCorner.with(node.face().axis,diagonal.coordinate(node.face().axis)+(slot+1)/4.0);
                    Node candidate = Node.nearest((int) diagonal.x(), (int) diagonal.y(), (int) diagonal.z(),innerFace.opposite(),point);
                    if (exists.test(candidate)) { outer = candidate; break; }
                }
                if (outer != null) {
                    result.add(new Connection(outer, List.of(node.point(), outerCorner, outer.point())));
                }
            }
        }
        return List.copyOf(result);
    }

    private static List<Connection> centeredConnections(Node node,Predicate<Node> exists) {
        List<Connection> result=new ArrayList<>(4);
        for (int axis:new int[]{node.face().uAxis(),node.face().vAxis()}) for (int sign:new int[]{-1,1}) {
            Face tangent=Face.along(axis,sign);
            Vec adjacent=node.origin().add(tangent.normal());
            Node flat=CableLayout.placement((int)adjacent.x(),(int)adjacent.y(),(int)adjacent.z(),node.face());
            if (exists.test(flat)) { result.add(new Connection(flat,List.of(node.point(),flat.point()))); continue; }
            Node inner=CableLayout.placement(node.x(),node.y(),node.z(),tangent);
            Vec corner=node.point().with(axis,node.origin().coordinate(axis)+(sign<0?Node.HEIGHT:1-Node.HEIGHT));
            if (exists.test(inner)) { result.add(new Connection(inner,List.of(node.point(),corner,inner.point()))); continue; }
            Vec diagonal=adjacent.add(node.face().normal());
            Node outer=CableLayout.placement((int)diagonal.x(),(int)diagonal.y(),(int)diagonal.z(),tangent.opposite());
            Vec outerCorner=node.point().with(axis,node.origin().coordinate(axis)+(sign<0?-Node.HEIGHT:1+Node.HEIGHT));
            if (exists.test(outer)) result.add(new Connection(outer,List.of(node.point(),outerCorner,outer.point())));
        }
        return List.copyOf(result);
    }

    public static List<Vec> coil(Node node) {
        List<Vec> points = new ArrayList<>();
        for (int i = 0; i <= 24; i++) {
            double angle = i * Math.PI * 1.65 / 24;
            double radius = .05 + .025 * i / 24;
            points.add(node.point().with(node.face().uAxis(), node.point().coordinate(node.face().uAxis()) + Math.cos(angle) * radius)
                    .with(node.face().vAxis(), node.point().coordinate(node.face().vAxis()) + Math.sin(angle) * radius));
        }
        return List.copyOf(points);
    }
}
