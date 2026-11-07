package dev.admutium.techmod.cable;

/** Nine non-overlapping mounting positions on each full face; 54 per air cell. */
public record Node(int x, int y, int z, Face face, int u, int v) {
    public static final double HEIGHT = 1.0 / 16;
    public static final double RADIUS = 0.047;
    public Node {
        if (u < 0 || u > 2 || v < 0 || v > 2) throw new IllegalArgumentException("slot outside face");
    }
    public Vec origin() { return new Vec(x, y, z); }
    public Vec point() {
        return origin().with(face.axis, origin().coordinate(face.axis) + (face.sign < 0 ? HEIGHT : 1 - HEIGHT))
                .with(face.uAxis(), origin().coordinate(face.uAxis()) + (u + 1) / 4.0)
                .with(face.vAxis(), origin().coordinate(face.vAxis()) + (v + 1) / 4.0);
    }
    public int index() { return face.ordinal() * 9 + v * 3 + u; }
    public long bit() { return 1L << index(); }
    public static Node fromIndex(int x, int y, int z, int index) {
        return new Node(x, y, z, Face.values()[index / 9], index % 3, index % 9 / 3);
    }
    public static Node nearest(int x, int y, int z, Face face, Vec point) {
        Vec local = point.subtract(new Vec(x, y, z));
        int u = Math.clamp((int) Math.round(local.coordinate(face.uAxis()) * 4) - 1, 0, 2);
        int v = Math.clamp((int) Math.round(local.coordinate(face.vAxis()) * 4) - 1, 0, 2);
        return new Node(x, y, z, face, u, v);
    }
    public String encode() { return x + "," + y + "," + z + "," + face.ordinal() + "," + u + "," + v; }
    public static Node decode(String text) {
        String[] p = text.split(",", -1);
        if (p.length != 6) throw new IllegalArgumentException("invalid node");
        return new Node(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                Face.values()[Integer.parseInt(p[3])], Integer.parseInt(p[4]), Integer.parseInt(p[5]));
    }
}
