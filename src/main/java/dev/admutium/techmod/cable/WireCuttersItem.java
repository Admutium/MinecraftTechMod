package dev.admutium.techmod.cable;

import dev.admutium.techmod.TechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class WireCuttersItem extends Item {
    public WireCuttersItem(Properties properties) { super(properties); }
    public static Node nearest(Level level, BlockPos pos, Vec hit, boolean holdersOnly) {
        CableSnapshot data=CableWorld.data(level,pos);
        Node selectedHolder=CablePicking.holder(pos.getX(),pos.getY(),pos.getZ(),data,hit);
        if(selectedHolder!=null || holdersOnly) return selectedHolder;
        Node best=null;
        double distance=Double.POSITIVE_INFINITY;
        for (int i=0;i<54;i++) {
            Node node=Node.fromIndex(pos.getX(),pos.getY(),pos.getZ(),i);
            boolean holder=node.u()==1 && node.v()==1 && data.holder(node.face());
            if (!holder && (holdersOnly || !data.has(node))) continue;
            double d=node.point().subtract(hit).length();
            if (!holder) {
                var links=SurfaceTopology.connections(node, other -> {
                    CableSnapshot s=CableWorld.data(level,CableWorld.pos(other));
                    return s.has(other) || (other.u()==1 && other.v()==1 && s.holder(other.face()));
                });
                var paths=new java.util.ArrayList<java.util.List<Vec>>();
                if (links.isEmpty()) paths.add(SurfaceTopology.coil(node));
                else for (var link:links) paths.add(link.path());
                for (var path:paths) for (int j=1;j<path.size();j++)
                    d=Math.min(d,hit.distanceToSegment(path.get(j-1),path.get(j)));
            }
            if (d<distance) { best=node; distance=d; }
        }
        return distance<=.18 ? best : null;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player=context.getPlayer();
        if (player==null) return InteractionResult.PASS;
        Level level=context.getLevel();
        BlockPos pos=context.getClickedPos();
        if (!level.getBlockState(pos).is(TechMod.CABLE)) return InteractionResult.PASS;
        if (!player.mayBuild() || !level.mayInteract(player,pos) || !player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand()))
            return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        CableItem.clearSelection(player.getUUID());
        Vec hit=new Vec(context.getClickLocation().x,context.getClickLocation().y,context.getClickLocation().z);
        Node node=nearest(level,pos,hit,false);
        CableSnapshot data=CableWorld.data(level,pos);
        if (node!=null) {
            boolean holder=node.u()==1 && node.v()==1 && data.holder(node.face());
            if (holder && player.isShiftKeyDown()) {
                for (CableSpan span:data.spans()) if (span.start().equals(node) || span.end().equals(node)) {
                    CableWorld.removeSpan(level,span,!player.isCreative());
                    return InteractionResult.SUCCESS;
                }
            }
            CableWorld.removeNode(level,node,holder,!player.isCreative());
        } else {
            CableSpan best=null;
            double distance=.2;
            for (CableSpan span:data.spans()) for (Vec p:span.points()) {
                double d=p.subtract(hit).length();
                if (d<distance) { distance=d; best=span; }
            }
            if (best!=null) CableWorld.removeSpan(level,best,!player.isCreative());
        }
        return InteractionResult.SUCCESS;
    }
}
