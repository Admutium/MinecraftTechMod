package dev.admutium.techmod.client.suit;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.player.PlayerModel;
/** Immutable submission state: deferred rendering must never read a subsequently posed player model. */
public record SuitPose(PartPose head,PartPose body,PartPose rightArm,PartPose leftArm,PartPose rightLeg,PartPose leftLeg,int wingMode) {
 public static SuitPose capture(PlayerModel m,int wings){return new SuitPose(m.head.storePose(),m.body.storePose(),m.rightArm.storePose(),m.leftArm.storePose(),m.rightLeg.storePose(),m.leftLeg.storePose(),wings);}
}
