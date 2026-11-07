package dev.admutium.techmod.cable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Fixed endpoints, gravity sag and stable geometry; no per-tick rope simulation. */
public record CableSpan(UUID id, Node start, Node end) {
    public static final double MAX_DISTANCE = 5;
    public static final int STEPS = 96;
    public CableSpan {
        double distance = start.point().subtract(end.point()).length();
        if (distance < .15 || distance > MAX_DISTANCE + 1e-8) throw new IllegalArgumentException("span out of range");
        if (start.u() != 1 || start.v() != 1 || end.u() != 1 || end.v() != 1)
            throw new IllegalArgumentException("holders must be centered");
    }
    public Vec point(double t) {
        if (t < 0 || t > 1) throw new IllegalArgumentException("t outside span");
        Vec a = start.point(), b = end.point();
        Vec delta = b.subtract(a);
        double horizontal = Math.hypot(delta.x(), delta.z());
        double sag = Math.min(.65, horizontal * .12);
        // A parabolic approximation of a catenary, with exact mounting endpoints.
        return a.add(delta.scale(t)).add(new Vec(0, -4 * sag * t * (1 - t), 0));
    }
    public List<Vec> points() {
        List<Vec> points = new ArrayList<>(STEPS + 1);
        for (int i = 0; i <= STEPS; i++) points.add(point((double) i / STEPS));
        return List.copyOf(points);
    }
    public double length() {
        List<Vec> points = points();
        double result = 0;
        for (int i = 1; i < points.size(); i++) result += points.get(i).subtract(points.get(i - 1)).length();
        return result;
    }
    public String encode() { return id + ";" + start.encode() + ";" + end.encode(); }
    public static CableSpan decode(String text) {
        String[] p = text.split(";", -1);
        if (p.length != 3) throw new IllegalArgumentException("invalid span");
        return new CableSpan(UUID.fromString(p[0]), Node.decode(p[1]), Node.decode(p[2]));
    }
}
