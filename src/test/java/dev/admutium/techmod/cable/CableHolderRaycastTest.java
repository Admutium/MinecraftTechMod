package dev.admutium.techmod.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CableHolderRaycastTest {
    @Test void actualMinecraftRaycastSelectsHolderOnAllSixMountingFaces() {
        for(Face face:Face.values()) {
            Node node=CableLayout.placement(-2,3,5,face);
            CableSnapshot snapshot=new CableSnapshot(0,1<<face.ordinal(),List.of());
            var boxes=CableHitGeometry.boxes(node.x(),node.y(),node.z(),snapshot,n -> false);
            var shape=CableShapes.fromBoxes(boxes);
            Vec start=node.point().subtract(face.normal().scale(2));
            Vec end=node.point().add(face.normal().scale(.1));
            var hit=shape.clip(new Vec3(start.x(),start.y(),start.z()),new Vec3(end.x(),end.y(),end.z()),
                    new BlockPos(node.x(),node.y(),node.z()));
            assertNotNull(hit);
            Vec clicked=new Vec(hit.getLocation().x,hit.getLocation().y,hit.getLocation().z);
            assertTrue(clicked.subtract(node.point()).length()>.18,"Old interaction would ignore this real raycast");
            assertEquals(node,CablePicking.holder(node.x(),node.y(),node.z(),snapshot,clicked));
        }
    }
}
