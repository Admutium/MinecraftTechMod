package dev.admutium.techmod.cable;

/** Immutable coordinates shared by topology, rendering and collision checks. */
public record Vec(double x, double y, double z) {
    public Vec add(Vec b) { return new Vec(x + b.x, y + b.y, z + b.z); }
    public Vec subtract(Vec b) { return new Vec(x - b.x, y - b.y, z - b.z); }
    public Vec scale(double s) { return new Vec(x * s, y * s, z * s); }
    public double dot(Vec b) { return x * b.x + y * b.y + z * b.z; }
    public Vec cross(Vec b) { return new Vec(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x); }
    public double length() { return Math.sqrt(dot(this)); }
    public Vec normalized() { double n = length(); return n < 1e-9 ? new Vec(0, 1, 0) : scale(1 / n); }
    public double coordinate(int axis) { return axis == 0 ? x : axis == 1 ? y : z; }
    public Vec with(int axis, double value) {
        return new Vec(axis == 0 ? value : x, axis == 1 ? value : y, axis == 2 ? value : z);
    }
    public double distanceToSegment(Vec start, Vec end) {
        Vec delta=end.subtract(start);
        double lengthSquared=delta.dot(delta);
        if (lengthSquared<1e-12) return subtract(start).length();
        double t=Math.clamp(subtract(start).dot(delta)/lengthSquared,0,1);
        return subtract(start.add(delta.scale(t))).length();
    }
}
