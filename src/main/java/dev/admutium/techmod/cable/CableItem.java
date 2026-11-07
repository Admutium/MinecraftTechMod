package dev.admutium.techmod.cable;

import dev.admutium.techmod.TechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.*;

public final class CableItem extends Item {
    private final boolean holder;
    private record Selection(Node node, String dimension, long expires) {}
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();
    public CableItem(Properties properties, boolean holder) { super(properties); this.holder=holder; }
    public static void clearSelection(UUID player) { SELECTIONS.remove(player); }
    public static void clearSelections() { SELECTIONS.clear(); }

    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (!player.mayBuild() || !level.mayInteract(player,clicked)) return InteractionResult.FAIL;
        Vec hit = new Vec(context.getClickLocation().x,context.getClickLocation().y,context.getClickLocation().z);
        boolean existing = level.getBlockState(clicked).is(TechMod.CABLE);
        Node nearest = existing ? WireCuttersItem.nearest(level,clicked,hit,false) : null;
        if (!holder && nearest != null && CableWorld.data(level,clicked).holder(nearest.face()) && nearest.u()==1 && nearest.v()==1) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Selection start=SELECTIONS.get(player.getUUID());
            String dimension=level.dimension().toString();
            if (player.isShiftKeyDown()) {
                clearSelection(player.getUUID());
                player.sendOverlayMessage(Component.translatable("message.techmod.cancelled"));
                return InteractionResult.SUCCESS;
            }
            if (start==null || !start.dimension().equals(dimension) || start.expires()<level.getGameTime()) {
                SELECTIONS.put(player.getUUID(),new Selection(nearest,dimension,level.getGameTime()+1200));
                player.sendOverlayMessage(Component.translatable("message.techmod.selected"));
                return InteractionResult.SUCCESS;
            }
            if (start.node().equals(nearest)) {
                clearSelection(player.getUUID());
                player.sendOverlayMessage(Component.translatable("message.techmod.cancelled"));
                return InteractionResult.SUCCESS;
            }
            String error;
            try { error=CableWorld.createSpan(level,player,new CableSpan(UUID.randomUUID(),start.node(),nearest),context.getItemInHand()); }
            catch (IllegalArgumentException e) { error="range"; }
            if (error==null || "lava".equals(error)) clearSelection(player.getUUID());
            player.sendOverlayMessage(Component.translatable("message.techmod."+(error==null ? "connected" : error)));
            return InteractionResult.SUCCESS;
        }

        BlockPos cell;
        Face face;
        if (existing && nearest!=null) { cell=clicked; face=nearest.face(); }
        else { cell=clicked.relative(context.getClickedFace()); face=Face.valueOf(context.getClickedFace().getOpposite().name()); }
        if (!level.mayInteract(player,cell) || !player.mayUseItemAt(cell,Direction.valueOf(face.name()),context.getItemInHand()))
            return InteractionResult.FAIL;
        if (level.getFluidState(cell).is(FluidTags.LAVA) || level.getFluidState(clicked).is(FluidTags.LAVA)) {
            if (!holder) CableWorld.burn(level,level.getFluidState(cell).is(FluidTags.LAVA) ? cell : clicked,context.getItemInHand(),player);
            return InteractionResult.SUCCESS;
        }
        Node node=holder ? new Node(cell.getX(),cell.getY(),cell.getZ(),face,1,1)
                : Node.nearest(cell.getX(),cell.getY(),cell.getZ(),face,hit);
        if (!CableWorld.available(level,cell) || !CableWorld.supported(level,node)) return InteractionResult.FAIL;
        CableSnapshot before=CableWorld.data(level,cell);
        if (holder ? before.holder(face) : before.has(node)) return InteractionResult.PASS;
        // A holder reserves the center mounting slot; other slots on its face remain available.
        if ((holder && before.has(node)) || (!holder && node.u()==1 && node.v()==1 && before.holder(face))) return InteractionResult.FAIL;
        for (CableSpan span : before.spans()) for (Vec p : span.points())
            if (p.subtract(node.point()).length()<Node.RADIUS*2+.03) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        CableBlockEntity be=CableWorld.ensure(level,cell);
        if (be==null) return InteractionResult.FAIL;
        be.update(new CableSnapshot(holder ? before.wires() : before.wires() | node.bit(),
                holder ? before.holders() | (1<<face.ordinal()) : before.holders(),before.spans()));
        context.getItemInHand().consume(1,player);
        level.playSound(null,cell,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.55f,1.4f);
        return InteractionResult.SUCCESS;
    }
}
