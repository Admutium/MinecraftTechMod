package dev.admutium.techmod.cable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Fixed endpoints, gravity sag and stable geometry; no per-tick rope simulation. */
public final class CableSpan {
    private final UUID id;
    private final Node start;
    private final Node end;
    private final List<Vec> points;
    private final int paid;
    public int cost(){return paid;}
    public UUID id() { return id; }
    public Node start() { return start; }
    public Node end() { return end; }
    public static final double MAX_DISTANCE = 5;
    public static final int STEPS = 96;
    public CableSpan(UUID id, Node start, Node end) { this(id,start,end,Math.max(1,(int)Math.ceil(start.point().subtract(end.point()).length()-1e-9))); }
    private CableSpan(UUID id,Node start,Node end,int paid){
        if(paid<1||paid>5)throw new IllegalArgumentException("invalid paid wire count");
        this.paid=paid;
        this.id=java.util.Objects.requireNonNull(id);
        this.start=java.util.Objects.requireNonNull(start);
        this.end=java.util.Objects.requireNonNull(end);
        double distance = start.point().subtract(end.point()).length();
        if (distance < .15 || distance > MAX_DISTANCE + 1e-8) throw new IllegalArgumentException("span out of range");
        if (start.u() != 1 || start.v() != 1 || end.u() != 1 || end.v() != 1)
            throw new IllegalArgumentException("holders must be centered");
        List<Vec> samples=new ArrayList<>(STEPS+1);
        for (int i=0;i<=STEPS;i++) samples.add(point((double)i/STEPS));
        points=List.copyOf(samples);
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
    public List<Vec> points() { return points; }
    @Override public boolean equals(Object other) {
        return other instanceof CableSpan s && id.equals(s.id) && start.equals(s.start) && end.equals(s.end) && paid==s.paid;
    }
    @Override public int hashCode() { return java.util.Objects.hash(id,start,end,paid); }
    public double length() {
        List<Vec> points = points();
        double result = 0;
        for (int i = 1; i < points.size(); i++) result += points.get(i).subtract(points.get(i - 1)).length();
        return result;
    }
    public String encode() { return id + ";" + start.encode() + ";" + end.encode() + ";" + paid; }
    public static CableSpan decode(String text) {
        String[] p = text.split(";", -1);
        if (p.length != 3 && p.length != 4) throw new IllegalArgumentException("invalid span");
        return new CableSpan(UUID.fromString(p[0]), Node.decode(p[1]), Node.decode(p[2]),p.length==4?Integer.parseInt(p[3]):1);
    }
}
