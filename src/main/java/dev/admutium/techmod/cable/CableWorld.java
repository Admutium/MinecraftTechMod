package dev.admutium.techmod.cable;

import dev.admutium.techmod.TechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.server.level.ServerLevel;
import java.util.*;

public final class CableWorld {
    public static final int MAX_LINKS_PER_HOLDER=4;
    public static final int MAX_SPANS_PER_CELL=8;
    private CableWorld() {}
    public static BlockPos pos(Node n) { return new BlockPos(n.x(), n.y(), n.z()); }
    public static Direction direction(Face face) { return Direction.valueOf(face.name()); }
    public static boolean isPanel(BlockState s){return s.getBlock() instanceof dev.admutium.techmod.machine.MachineBlock b&&b.kind==dev.admutium.techmod.machine.MachineKind.SOLAR;}
    public static boolean isCableCell(BlockState s){return s.is(TechMod.CABLE)||isPanel(s);}
    public static boolean panelCompatible(CableSnapshot s){return s.holders()==0&&s.spans().isEmpty()&&(s.wires()&~CableLayout.placement(0,0,0,Face.DOWN).bit())==0;}
    public static CableSnapshot data(Level level, BlockPos p) {
        return level.hasChunkAt(p) && level.getBlockEntity(p) instanceof CableBlockEntity be ? be.snapshot() : CableSnapshot.EMPTY;
    }
    public static boolean supported(Level level, Node n) {
        BlockPos support = pos(n).relative(direction(n.face()));
        return level.hasChunkAt(support) && level.getBlockState(support).isFaceSturdy(level, support, direction(n.face().opposite()));
    }
    public static boolean available(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)) return false;
        BlockState s = level.getBlockState(pos);
        return isCableCell(s) || s.isAir() || s.is(Blocks.WATER) || s.is(Blocks.SNOW)&&s.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS)<8;
    }
    public static CableBlockEntity ensure(Level level, BlockPos pos) {
        if (!available(level, pos)) return null;
        if (!isCableCell(level.getBlockState(pos))) {
            boolean water = level.getFluidState(pos).is(FluidTags.WATER);
            int snow=level.getBlockState(pos).is(Blocks.SNOW)?level.getBlockState(pos).getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS):0;
            if (!level.setBlock(pos, TechMod.CABLE.defaultBlockState().setValue(CableBlock.WATERLOGGED, water).setValue(CableBlock.SNOW,snow), Block.UPDATE_ALL)) return null;
            if (water) level.scheduleTick(pos, net.minecraft.world.level.material.Fluids.WATER,
                    net.minecraft.world.level.material.Fluids.WATER.getTickDelay(level));
        }
        return level.getBlockEntity(pos) instanceof CableBlockEntity be ? be : null;
    }
    public static void prune(Level level, BlockPos p) {
        if (level.getBlockState(p).is(TechMod.CABLE) && level.getBlockEntity(p) instanceof CableBlockEntity be && be.snapshot().empty()) {
            boolean water = be.getBlockState().getValue(CableBlock.WATERLOGGED);
            int layers=be.getBlockState().getValue(CableBlock.SNOW);
            var replacement=water?Blocks.WATER.defaultBlockState():layers>0?Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS,layers):Blocks.AIR.defaultBlockState();
            if(layers>0&&!replacement.canSurvive(level,p))replacement=Blocks.AIR.defaultBlockState();
            level.setBlock(p,replacement,Block.UPDATE_ALL);
        }
    }
    public static void drop(Level level, BlockPos p, net.minecraft.world.item.Item item, int count) {
        if (count > 0) Block.popResource(level, p, new ItemStack(item, count));
    }
    public static void burn(Level level, BlockPos p, ItemStack stack, Player player) {
        if (level.isClientSide()) return;
        stack.consume(1, player);
        level.playSound(null, p, SoundEvents.GENERIC_BURN, SoundSource.BLOCKS, .65f, 1.1f);
        if (level instanceof ServerLevel server) server.sendParticles(ParticleTypes.SMOKE,
                p.getX()+.5, p.getY()+.7, p.getZ()+.5, 8, .15, .15, .15, .02);
    }

    /** All occupied cells, including the two endpoint cells. No forced chunk loading. */
    public static Set<BlockPos> cells(CableSpan span) {
        Set<BlockPos> result = new LinkedHashSet<>();
        result.add(pos(span.start()));
        result.add(pos(span.end()));
        List<Vec> points = span.points();
        for (int i = 1; i < points.size(); i++) {
            Vec p = points.get(i-1).add(points.get(i)).scale(.5);
            result.add(BlockPos.containing(p.x(), p.y(), p.z()));
        }
        return result;
    }
    public static String createSpan(Level level, Player player, CableSpan span, ItemStack stack) {
        Set<BlockPos> occupied = cells(span);
        if(!player.isCreative() && stack.getCount()<span.cost())return "not_enough_wire";
        for (Node endpoint : List.of(span.start(), span.end())) {
            if (!data(level, pos(endpoint)).holder(endpoint.face())) return "missing_holder";
            long links=data(level,pos(endpoint)).spans().stream()
                    .filter(s -> s.start().equals(endpoint) || s.end().equals(endpoint)).count();
            if (links>=MAX_LINKS_PER_HOLDER) return "holder_full";
        }
        if (data(level, pos(span.start())).spans().stream().anyMatch(s ->
                (s.start().equals(span.start()) && s.end().equals(span.end())) ||
                (s.start().equals(span.end()) && s.end().equals(span.start())))) return "duplicate";
        for (BlockPos p : occupied) {
            if (!level.hasChunkAt(p)) return "unloaded";
            if (!level.mayInteract(player, p) || !player.mayUseItemAt(p, Direction.UP, stack)) return "protected";
            if (level.getFluidState(p).is(FluidTags.LAVA)) {
                burn(level, p, stack, player);
                return "lava";
            }
            if (!available(level, p)||isPanel(level.getBlockState(p))) return "obstructed";
            if (data(level,p).spans().size()>=MAX_SPANS_PER_CELL) return "cell_full";
        }
        List<Vec> points = span.points();
        for (int i = 1; i < points.size(); i++) {
            Vec a = points.get(i-1), b = points.get(i);
            AABB bounds = new AABB(a.x(),a.y(),a.z(),b.x(),b.y(),b.z()).inflate(Node.RADIUS);
            for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),
                    BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ))) {
                if (!level.hasChunkAt(p)) return "unloaded";
                if (level.getFluidState(p).is(FluidTags.LAVA)) { burn(level,p,stack,player); return "lava"; }
                if (!level.getBlockState(p).is(TechMod.CABLE)) {
                    var collision = level.getBlockState(p).getCollisionShape(level, p, CollisionContext.empty());
                    for (AABB box : collision.toAabbs()) if (box.move(p).intersects(bounds)) return "obstructed";
                } else {
                    CableSnapshot existing = data(level,p);
                    for (CableSpan other : existing.spans()) {
                        // Branches may share a holder, but independent cables must not intersect.
                        if (sharesEndpoint(span, other)) continue;
                        for (Vec q : other.points()) if (bounds.inflate(Node.RADIUS).contains(q.x(),q.y(),q.z())) return "obstructed";
                    }
                    for (int slot=0;slot<54;slot++) {
                        Node n=Node.fromIndex(p.getX(),p.getY(),p.getZ(),slot);
                        if (existing.has(n) && !n.equals(span.start()) && !n.equals(span.end()) &&
                                bounds.inflate(Node.RADIUS).contains(n.point().x(),n.point().y(),n.point().z())) return "obstructed";
                    }
                }
            }
        }
        // Preflight is complete before mutating any cell.
        for (BlockPos p : occupied) {
            CableBlockEntity be = ensure(level,p);
            if (be == null) {
                removeSpan(level,span,false);
                return "obstructed";
            }
            CableSnapshot before = be.snapshot();
            List<CableSpan> spans = new ArrayList<>(before.spans());
            spans.add(span);
            be.update(new CableSnapshot(before.wires(),before.holders(),spans));
        }
        stack.consume(span.cost(),player);
        return null;
    }
    private static boolean sharesEndpoint(CableSpan a, CableSpan b) {
        return a.start().equals(b.start()) || a.start().equals(b.end()) || a.end().equals(b.start()) || a.end().equals(b.end());
    }
    public static void removeSpan(Level level, CableSpan span, boolean refund) {
        // Only the owning endpoint authorizes a refund, preventing duplicate drops from stale chunk fragments.
        BlockPos owner = pos(span.start());
        if (refund && data(level,owner).spans().stream().anyMatch(s -> s.id().equals(span.id())))
            drop(level,owner,TechMod.COPPER_CABLE,span.cost());
        for (BlockPos p : cells(span)) {
            if (!level.hasChunkAt(p) || !(level.getBlockEntity(p) instanceof CableBlockEntity be)) continue;
            CableSnapshot s = be.snapshot();
            if (s.spans().stream().noneMatch(link -> link.id().equals(span.id()))) continue;
            be.update(new CableSnapshot(s.wires(),s.holders(),s.spans().stream().filter(link -> !link.id().equals(span.id())).toList()));
            prune(level,p);
        }
    }
    public static void removeNode(Level level, Node node, boolean holder, boolean refund) {
        BlockPos p = pos(node);
        if (!(level.getBlockEntity(p) instanceof CableBlockEntity be)) return;
        CableSnapshot s = be.snapshot();
        if (holder) {
            for (CableSpan span : s.spans()) if (span.start().equals(node) || span.end().equals(node)) removeSpan(level,span,refund);
            if (!(level.getBlockEntity(p) instanceof CableBlockEntity remaining)) return;
            s=remaining.snapshot(); be=remaining;
        }
        if (refund) drop(level,p,holder ? TechMod.CABLE_HOLDER : TechMod.COPPER_CABLE,1);
        be.update(new CableSnapshot(holder ? s.wires() : s.wires() & ~node.bit(),
                holder ? s.holders() & ~(1 << node.face().ordinal()) : s.holders(),s.spans()));
        prune(level,p);
    }
    public static void removeAll(Level level, BlockPos p, CableSnapshot snapshot, boolean refund) {
        if (refund) {
            drop(level,p,TechMod.COPPER_CABLE,Long.bitCount(snapshot.wires()));
            drop(level,p,TechMod.CABLE_HOLDER,Integer.bitCount(snapshot.holders()));
        }
        for (CableSpan span : snapshot.spans()) {
            // The block has already been removed by the engine; recover its ownership once here.
            boolean ownedHere = pos(span.start()).equals(p);
            if (refund && ownedHere) drop(level,p,TechMod.COPPER_CABLE,span.cost());
            removeSpan(level,span,refund && !ownedHere);
        }
    }
    public static void validate(Level level, BlockPos p, CableBlockEntity be) {
        CableSnapshot snapshot = be.snapshot();
        CableSnapshot centered=snapshot.centered();
        if (centered!=snapshot) {
            int refund=CableLayout.surplus(snapshot.wires(),snapshot.holders());
            be.update(centered);
            drop(level,p,TechMod.COPPER_CABLE,refund);
            snapshot=centered;
        }
        for (Face face : Face.values()) {
            Node center = new Node(p.getX(),p.getY(),p.getZ(),face,1,1);
            if (!level.hasChunkAt(p.relative(direction(face)))) continue;
            if (!supported(level,center)) {
                for (int i=0;i<9;i++) {
                    Node n = Node.fromIndex(p.getX(),p.getY(),p.getZ(),face.ordinal()*9+i);
                    if (data(level,p).has(n)) removeNode(level,n,false,true);
                }
                if (data(level,p).holder(face)) removeNode(level,center,true,true);
            }
        }
        for (CableSpan span : snapshot.spans()) {
            Set<BlockPos> occupied = cells(span);
            if (occupied.stream().anyMatch(cell -> !level.hasChunkAt(cell))) continue;
            boolean invalid = !data(level,pos(span.start())).holder(span.start().face()) ||
                    !data(level,pos(span.end())).holder(span.end().face()) ||
                    occupied.stream().anyMatch(cell -> data(level,cell).spans().stream().noneMatch(s -> s.id().equals(span.id())));
            if (invalid) removeSpan(level,span,true);
        }
        if (be.getBlockState().getValueOrElse(CableBlock.WATERLOGGED,false))
            level.scheduleTick(p,net.minecraft.world.level.material.Fluids.WATER,net.minecraft.world.level.material.Fluids.WATER.getTickDelay(level));
        prune(level,p);
    }
}
