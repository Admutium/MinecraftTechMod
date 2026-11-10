package dev.admutium.techmod.suit;
import net.minecraft.world.*;import net.minecraft.world.entity.player.*;import net.minecraft.world.inventory.*;import net.minecraft.world.item.*;

/** Virtual reserves expose at most one legal item stack; their full capacity lives in suit data. */
public final class SuitInventoryMenu extends AbstractContainerMenu {
 public static final int FULL=9,EQUIPPED=10,EMPTY=11,PLAYER=12;
 public final SuitSession session;public final Container cargo;public final boolean expanded,food;
 public SuitInventoryMenu(int id,Inventory inv){this(id,inv,null);}
 public SuitInventoryMenu(int id,Inventory inv,SuitSession s){super(Suits.INVENTORY_MENU,id);session=s;var data=s==null?Suits.data(inv.player):s.snapshot();expanded=data.level(SuitUpgrade.INVENTORY)>0;food=data.level(SuitUpgrade.NUTRIENTS)>0;
  cargo=new SimpleContainer(9){public ItemStack getItem(int i){return s==null?super.getItem(i):s.cargo.get(i);}public void setItem(int i,ItemStack stack){if(s==null)super.setItem(i,stack);else{s.cargo.set(i,stack);s.flush();}}public ItemStack removeItem(int i,int n){if(s==null)return super.removeItem(i,n);var stack=s.cargo.get(i).split(n);s.flush();return stack;}public void setChanged(){if(s!=null)s.flush();}};
  for(int i=0;i<9;i++)addSlot(new Slot(cargo,i,12+i*18,146){public boolean isActive(){return expanded;}public boolean mayPlace(ItemStack item){return expanded&&!item.is(Suits.ITEM);}public boolean mayPickup(Player p){return expanded;}});
  addReserve(s,false,12);var equipped=new SimpleContainer(1);if(s!=null)equipped.setItem(0,s.stack);addSlot(new Slot(equipped,0,156,29){public boolean mayPlace(ItemStack item){return false;}public boolean mayPickup(Player p){return false;}});addReserve(s,true,94);
  for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,12+col*18,92+row*18));
  for(int col=0;col<9;col++)addSlot(new Slot(inv,col,12+col*18,expanded?170:152));
 }
 private void addReserve(SuitSession s,boolean empty,int x){var reserve=new SimpleContainer(1){public ItemStack getItem(int i){if(s==null)return super.getItem(i);int n=empty?s.emptyCans:s.nutrients;return n==0?ItemStack.EMPTY:new ItemStack(empty?Suits.EMPTY_CAN:Suits.NUTRIENT,Math.min(64,n));}};addSlot(new Slot(reserve,0,x,56){public boolean isActive(){return food;}public boolean mayPlace(ItemStack item){return false;}public boolean mayPickup(Player p){return false;}});}
 @Override public void clicked(int slot,int button,ContainerInput action,Player p){if(slot==FULL||slot==EMPTY){if(session==null||!stillValid(p)||!food)return;boolean empty=slot==EMPTY;int count=empty?session.emptyCans:session.nutrients;Item item=empty?Suits.EMPTY_CAN:Suits.NUTRIENT;
  if(action==ContainerInput.PICKUP){var carried=getCarried();if(carried.is(item)){int n=Math.min(carried.getCount(),32*session.level(SuitUpgrade.NUTRIENTS)-count);if(button==1)n=Math.min(1,n);if(n>0){carried.shrink(n);count+=n;}}else if(carried.isEmpty()){int n=Math.min(button==1?1:64,count);if(n>0){setCarried(new ItemStack(item,n));count-=n;}}}
  else if(action==ContainerInput.QUICK_MOVE){var stack=new ItemStack(item,Math.min(64,count));int n=stack.getCount();p.getInventory().add(stack);count-=n-stack.getCount();}
  if(empty)session.emptyCans=count;else session.nutrients=count;session.flush();return;}super.clicked(slot,button,action,p);}
 public boolean stillValid(Player p){return session==null||Suits.session(p)==session;}
 public ItemStack quickMoveStack(Player p,int i){var slot=slots.get(i);if(!slot.hasItem()||!slot.isActive())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();if(i>=FULL&&i<PLAYER)return ItemStack.EMPTY;
  if(i<FULL){if(!moveItemStackTo(stack,PLAYER,slots.size(),true))return ItemStack.EMPTY;}
  else if(food&&session!=null&&(stack.is(Suits.NUTRIENT)||stack.is(Suits.EMPTY_CAN))){boolean empty=stack.is(Suits.EMPTY_CAN);int count=empty?session.emptyCans:session.nutrients;int n=Math.min(stack.getCount(),32*session.level(SuitUpgrade.NUTRIENTS)-count);if(n<=0)return ItemStack.EMPTY;stack.shrink(n);if(empty)session.emptyCans+=n;else session.nutrients+=n;session.flush();}
  else if(!expanded||stack.is(Suits.ITEM)||!moveItemStackTo(stack,0,9,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;
 }
 public void removed(Player p){super.removed(p);if(session!=null)session.flush();}
}
