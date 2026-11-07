package dev.admutium.techmod.cable;

/** The direction FROM the cable's air cell TOWARDS its supporting block. */
public enum Face {
    DOWN(1, -1), UP(1, 1), NORTH(2, -1), SOUTH(2, 1), WEST(0, -1), EAST(0, 1);

    public final int axis;
    public final int sign;
    Face(int axis, int sign) { this.axis = axis; this.sign = sign; }
    public Vec normal() { return new Vec(0, 0, 0).with(axis, sign); }
    public int uAxis() { return axis == 0 ? 2 : 0; }
    public int vAxis() { return axis == 1 ? 2 : 1; }
    public Face opposite() { return values()[ordinal() ^ 1]; }
    public static Face along(int axis, int sign) {
        for (Face f : values()) if (f.axis == axis && f.sign == sign) return f;
        throw new IllegalArgumentException("axis");
    }
}
