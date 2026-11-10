package dev.admutium.techmod.client.suit;
import dev.admutium.techmod.suit.*;import net.minecraft.client.Minecraft;import net.minecraft.client.multiplayer.ClientLevel;import net.minecraft.core.BlockPos;
/** Immutable snapshot read by chunk workers. Only nearby sections are invalidated, at most 5 times/s. */
public final class SuitLocalLight {
 public record Emitter(BlockPos pos,int strength,int radius) {public int at(BlockPos p){double square=pos.distSqr(p);if(square>=radius*radius)return 0;double d=Math.sqrt(square);return Math.max(0,(int)Math.round(strength*(1-d/radius)));}}
 private static volatile Emitter emitter;private static ClientLevel level;
 public static int at(BlockPos p){var e=emitter;return e==null?0:e.at(p);}
 public static void tick(Minecraft c){if(c.level!=level){emitter=null;level=c.level;}if(c.player==null||c.level==null)return;var d=SuitClient.wornData();int grade=d.energy()>0&&d.on("light")?d.level(SuitUpgrade.LIGHT):0;var old=emitter;var pos=c.player.blockPosition().above();if(grade==0){if(old!=null){emitter=null;dirty(c.level,old);}return;}int strength=5+grade,radius=3+(grade*4+9)/10;if(old!=null&&old.pos().equals(pos)&&old.strength()==strength)return;if(old!=null&&c.player.tickCount%4!=0)return;var next=new Emitter(pos,strength,radius);emitter=next;if(old!=null)dirty(c.level,old);dirty(c.level,next);}
 private static void dirty(ClientLevel level,Emitter e){var p=e.pos();int r=e.radius()+1;level.setSectionRangeDirty((p.getX()-r)>>4,(p.getY()-r)>>4,(p.getZ()-r)>>4,(p.getX()+r)>>4,(p.getY()+r)>>4,(p.getZ()+r)>>4);}
}
