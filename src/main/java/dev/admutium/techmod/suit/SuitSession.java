package dev.admutium.techmod.suit;
import dev.admutium.techmod.energy.Energy;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import java.util.*;
/** Server-owned runtime; every transfer flushes the item before moving it. */
public final class SuitSession {
 public final UUID id;public final ItemStack stack;
 public long energy,rescueReady;public double shield;
 public final Map<String,Integer> levels=new LinkedHashMap<>();public final Set<String> active=new LinkedHashSet<>();
 public final NonNullList<ItemStack> cargo=NonNullList.withSize(9,ItemStack.EMPTY);public final Set<String> blacklist=new LinkedHashSet<>(),ores=new LinkedHashSet<>();public final List<ItemStack> salvage=new ArrayList<>();
 public Optional<GlobalPos> home;public int nutrients,tint,magnetRange;public boolean sealed;
 public int input,magnetPulse,warpTicks,shieldDelay,revealTicks;public long lastDamage,lastMining=-1000;
 public final long[] spent=new long[5],rates=new long[5];private final long[][] window=new long[20][5];private int cursor;public long spentTick,chargeTick,chargeRate;private final long[] chargeWindow=new long[20];
 public boolean flightGranted,oldMayFly,oldFlying;public float oldFlySpeed;
 public SuitSession(ItemStack stack){this.stack=stack;var s=Suits.ensure(stack);id=s.id();energy=s.energy();rescueReady=s.rescueReady();shield=s.shield()/1000.;levels.putAll(s.levels());active.addAll(s.active());blacklist.addAll(s.contents().blacklist());ores.addAll(s.contents().ores());for(int i=0;i<Math.min(9,s.contents().cargo().size());i++)cargo.set(i,s.contents().cargo().get(i).copy());salvage.addAll(s.contents().salvage().stream().map(ItemStack::copy).toList());nutrients=s.contents().nutrients();home=s.home();tint=s.tint();magnetRange=s.magnetRange();sealed=s.sealed();}
 public int level(SuitUpgrade upgrade){return Math.clamp(levels.getOrDefault(upgrade.id(),0),0,upgrade.max);}
 public boolean on(String key){return active.contains(key);}
 public long capacity(){return (12000L+level(SuitUpgrade.CAPACITY)*6000L)*Energy.UNIT;}
 public double shieldCapacity(){return level(SuitUpgrade.SHIELD)==0?0:20+10*level(SuitUpgrade.SHIELD);}
 public double efficiency(){return 1-.08*level(SuitUpgrade.EFFICIENCY);}
 public double maxPower(){return 20+10*level(SuitUpgrade.POWER);}
 public boolean spend(double te,int category){long n=Math.max(0,Math.round(te*efficiency()*Energy.UNIT));if(energy<n||spentTick+n>maxPower()*Energy.UNIT)return false;energy-=n;spentTick+=n;spent[category]+=n;return true;}
 public void startTick(){spentTick=0;Arrays.fill(spent,0);}
 public void endTick(){chargeRate+=chargeTick-chargeWindow[cursor];chargeWindow[cursor]=chargeTick;chargeTick=0;for(int i=0;i<5;i++){rates[i]+=spent[i]-window[cursor][i];window[cursor][i]=spent[i];}cursor=(cursor+1)%20;}
 public SuitData snapshot(){var l=new LinkedHashMap<>(levels);l.put("_reveal",revealTicks);return new SuitData(id,energy,l,List.copyOf(active),new SuitData.Contents(cargo,List.copyOf(blacklist),List.copyOf(ores),nutrients,salvage),home,rescueReady,Math.round(shield*1000),tint,magnetRange,sealed);}
 public void flush(){energy=Math.clamp(energy,0,capacity());stack.set(Suits.DATA,snapshot());}
 public void upgrade(SuitUpgrade upgrade){levels.put(upgrade.id(),Math.min(upgrade.max,level(upgrade)+1));flush();}
 public boolean blocked(ItemStack item){return blacklist.contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem()).toString());}
 public void insertCargo(ItemStack item){if(level(SuitUpgrade.INVENTORY)==0||item.is(Suits.ITEM))return;for(int i=0;i<9&&!item.isEmpty();i++){var existing=cargo.get(i);if(existing.isEmpty()){cargo.set(i,item.split(Math.min(item.getCount(),item.getMaxStackSize())));}else if(ItemStack.isSameItemSameComponents(existing,item)){int n=Math.min(item.getCount(),existing.getMaxStackSize()-existing.getCount());existing.grow(n);item.shrink(n);}}}
}
