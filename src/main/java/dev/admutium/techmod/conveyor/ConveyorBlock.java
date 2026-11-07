package dev.admutium.techmod.conveyor;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import java.util.*;
public final class ConveyorBlock extends Block implements EntityBlock {
 public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final IntegerProperty SLOPE=IntegerProperty.create("slope",0,2),TURN=IntegerProperty.create("turn",0,2);
 public static final BooleanProperty UPPER=BooleanProperty.create("upper"),ACTIVE=BooleanProperty.create("active");
 private final java.util.Map<BlockState,VoxelShape> shapes=new java.util.concurrent.ConcurrentHashMap<>();
 public final Conveyors.Kind kind;
 public ConveyorBlock(Conveyors.Kind k,Properties p){super(p);kind=k;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SLOPE,0).setValue(TURN,0).setValue(UPPER,false).setValue(ACTIVE,false));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,SLOPE,TURN,UPPER,ACTIVE);}
 public static int slope(BlockState s){int n=s.getValue(SLOPE);return n==2?-1:n;}
 public static int turn(BlockState s){int n=s.getValue(TURN);return n==2?-1:n;}
 public static BlockPos owner(BlockPos p,BlockState s){return s.getValue(UPPER)?p.offset(0,-slope(s),0):p;}
 public static Direction input(BlockState s){var d=s.getValue(FACING);return turn(s)==0?d.getOpposite():turn(s)>0?d.getClockWise():d.getCounterClockWise();}
 public static BlockPos output(BlockPos p,BlockState s){return p.relative(s.getValue(FACING)).offset(0,slope(s),0);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){
  var d=c.getHorizontalDirection();int slope=kind==Conveyors.Kind.BELT?ConveyorItem.mode(c.getPlayer()):0;int turn=0;var p=c.getClickedPos();
  if(slope!=0&&!c.getLevel().getBlockState(p.offset(0,slope,0)).isAir())return null;
  if(kind==Conveyors.Kind.BELT&&slope==0)for(var side:Direction.Plane.HORIZONTAL){for(int y=-1;y<=1;y++){
   var at=p.relative(side).offset(0,y,0);var s=c.getLevel().getBlockState(at);
   if(s.getBlock() instanceof ConveyorBlock b&&b.kind==Conveyors.Kind.BELT&&!s.getValue(UPPER)&&output(at,s).equals(p)){
    if(d==side)d=side.getOpposite();if(side!=d.getOpposite())turn=side==d.getClockWise()?1:-1;
   }
  }}
  return defaultBlockState().setValue(FACING,d).setValue(SLOPE,slope<0?2:slope).setValue(TURN,turn<0?2:turn);
 }
 @Override public void setPlacedBy(Level w,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){
  int slope=slope(s);if(!w.isClientSide()&&slope!=0)w.setBlock(p.offset(0,slope,0),s.setValue(UPPER,true),UPDATE_ALL);
  ConveyorNetwork.invalidate(w);
 }
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(UPPER)?null:new ConveyorEntity(p,s);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level w,BlockState s,BlockEntityType<T> type){return !w.isClientSide()&&!s.getValue(UPPER)&&type==Conveyors.ENTITY?(world,p,state,be)->ConveyorNetwork.register((ServerLevel)world,(ConveyorEntity)be):null;}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return shape(s);}
 @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return shape(s);}
 private VoxelShape shape(BlockState s){return shapes.computeIfAbsent(s,this::buildShape);}
 private VoxelShape buildShape(BlockState s){
  int slope=slope(s);if(slope==0)return Block.box(0,0,0,16,kind==Conveyors.Kind.MANIPULATOR?8:4,16);
  var d=s.getValue(FACING);double shift=s.getValue(UPPER)?slope:0;VoxelShape result=Shapes.empty();
  for(int i=0;i<16;i++){double t=(i+.5)/16.0;double height=.25+slope*t-shift;double lo=Math.max(0,height-.16),hi=Math.min(1,height);if(hi<=lo)continue;
   double a=d.getAxisDirection()==Direction.AxisDirection.POSITIVE?i:15-i;
   result=Shapes.joinUnoptimized(result,d.getAxis()==Direction.Axis.X?Shapes.box(a/16,lo,0,(a+1)/16,hi,1):Shapes.box(0,lo,a/16,1,hi,(a+1)/16),BooleanOp.OR);
  }return result;
 }
 @Override protected java.util.List<ItemStack> getDrops(BlockState s,LootParams.Builder b){return s.getValue(UPPER)?List.of():List.of(new ItemStack(asItem()));}
 @Override public BlockState playerWillDestroy(Level w,BlockPos p,BlockState s,Player player){if(!w.isClientSide()&&s.getValue(UPPER))w.destroyBlock(owner(p,s),!player.isCreative(),player);return super.playerWillDestroy(w,p,s,player);}
 @Override protected void neighborChanged(BlockState s,Level w,BlockPos p,Block source,Orientation o,boolean moved){
  if(w.isClientSide()||slope(s)==0)return;var other=s.getValue(UPPER)?owner(p,s):p.offset(0,slope(s),0);
  if(!w.getBlockState(other).is(this))w.destroyBlock(p,!s.getValue(UPPER));
 }
 @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos p,Player player,BlockHitResult hit){
  p=owner(p,s);if(w.isClientSide())return InteractionResult.SUCCESS;
  if(w.getBlockEntity(p) instanceof ConveyorEntity be){
   if(player.isShiftKeyDown()&&player.getMainHandItem().isEmpty()){var state=be.getBlockState();w.setBlock(p,state.setValue(FACING,state.getValue(FACING).getClockWise()).setValue(TURN,0),UPDATE_ALL);if(slope(state)!=0)w.setBlock(p.offset(0,slope(state),0),w.getBlockState(p).setValue(UPPER,true),UPDATE_ALL);ConveyorNetwork.invalidate(w);}
   else if(kind==Conveyors.Kind.MANIPULATOR)player.openMenu(be);else player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("conveyor.techmod."+be.statusKey()));
  }return InteractionResult.SUCCESS;
 }
 @Override protected VoxelShape getEntityInsideCollisionShape(BlockState s,BlockGetter w,BlockPos p,Entity e){return Shapes.block();}
 @Override protected void entityInside(BlockState s,Level w,BlockPos p,Entity e,InsideBlockEffectApplier effects,boolean onEntity){
  if(kind!=Conveyors.Kind.BELT||!s.getValue(ACTIVE)||!(e instanceof LivingEntity)||e.isShiftKeyDown())return;
  var base=owner(p,s);var d=s.getValue(FACING);double t=d.getAxis()==Direction.Axis.X?e.getX()-base.getX():e.getZ()-base.getZ();if(d.getAxisDirection()==Direction.AxisDirection.NEGATIVE)t=1-t;double top=base.getY()+.25+slope(s)*t;
  if(Math.abs(e.getY()-top)>.16)return;var v=e.getDeltaMovement();if(v.x*d.getStepX()+v.z*d.getStepZ()<.12)e.setDeltaMovement(v.add(d.getStepX()*.02,0,d.getStepZ()*.02));
 }
 @Override protected void onPlace(BlockState s,Level w,BlockPos p,BlockState old,boolean moved){if(!old.is(this))ConveyorNetwork.invalidate(w);}
}
