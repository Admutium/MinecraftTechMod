package dev.admutium.techmod.cable;

/** One paid surface run per face. Legacy slot bits remain readable for lossless migration. */
public final class CableLayout {
    private CableLayout() {}
    public static Node placement(int x, int y, int z, Face face) { return new Node(x,y,z,face,1,1); }
    public static boolean hasFace(long wires, Face face) { return (wires & (511L << (face.ordinal()*9))) != 0; }
    public static long centered(long wires, int holders) {
        long result=0;
        for (Face face:Face.values()) if (hasFace(wires,face) && (holders & (1<<face.ordinal()))==0)
            result |= 1L << (face.ordinal()*9+4);
        return result;
    }
    public static int surplus(long wires, int holders) { return Long.bitCount(wires)-Long.bitCount(centered(wires,holders)); }
}
