package dev.admutium.techmod.cable;

/** Click selection must use the exact quantized box used by Minecraft's block raycast. */
public final class CablePicking {
    private CablePicking() {}
    public static Node holder(int x,int y,int z,CableSnapshot snapshot,Vec hit) {
        Vec local=hit.subtract(new Vec(x,y,z));
        Node best=null;
        double distance=Double.POSITIVE_INFINITY;
        for(Face face:Face.values()) {
            if (!snapshot.holder(face)) continue;
            Node node=CableLayout.placement(x,y,z,face);
            if (!CableHitGeometry.mountBox(node,true).contains(local)) continue;
            double d=node.point().subtract(hit).length();
            if (d<distance) { best=node;distance=d; }
        }
        return best;
    }
}
