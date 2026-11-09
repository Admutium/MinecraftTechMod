package dev.admutium.techmod.suit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;
/** Immutable item snapshot. Mutable runtime inventories are copied before entering a data component. */
public record SuitData(UUID id,long energy,Map<String,Integer> levels,List<String> active,Contents contents,Optional<GlobalPos> home,long rescueReady,long shield,int tint,int magnetRange,boolean sealed) {
 public record Contents(List<ItemStack> cargo,List<String> blacklist,List<String> ores,int nutrients,List<ItemStack> salvage){
  public static final Contents EMPTY=new Contents(List.of(),List.of(),List.of(),0,List.of());
  public static final Codec<Contents> CODEC=RecordCodecBuilder.create(i->i.group(ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("cargo",List.of()).forGetter(Contents::cargo),Codec.STRING.listOf().optionalFieldOf("blacklist",List.of()).forGetter(Contents::blacklist),Codec.STRING.listOf().optionalFieldOf("ores",List.of()).forGetter(Contents::ores),Codec.INT.optionalFieldOf("nutrients",0).forGetter(Contents::nutrients),ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("salvage",List.of()).forGetter(Contents::salvage)).apply(i,Contents::new));
  public Contents{cargo=copy(cargo,9);blacklist=blacklist.stream().distinct().limit(64).toList();ores=ores.stream().distinct().limit(64).toList();nutrients=Math.clamp(nutrients,0,192);salvage=copy(salvage,64);}
 }
 public static final UUID EMPTY_ID=new UUID(0,0);
 public static final SuitData EMPTY=new SuitData(EMPTY_ID,0,Map.of(),List.of(),Contents.EMPTY,Optional.empty(),0,0,0xffff9c26,1,false);
 public static final Codec<SuitData> CODEC=RecordCodecBuilder.create(i->i.group(UUIDUtil.CODEC.fieldOf("id").forGetter(SuitData::id),Codec.LONG.optionalFieldOf("energy",0L).forGetter(SuitData::energy),Codec.unboundedMap(Codec.STRING,Codec.INT).optionalFieldOf("levels",Map.of()).forGetter(SuitData::levels),Codec.STRING.listOf().optionalFieldOf("active",List.of()).forGetter(SuitData::active),Contents.CODEC.optionalFieldOf("contents",Contents.EMPTY).forGetter(SuitData::contents),GlobalPos.CODEC.optionalFieldOf("home").forGetter(SuitData::home),Codec.LONG.optionalFieldOf("rescue_ready",0L).forGetter(SuitData::rescueReady),Codec.LONG.optionalFieldOf("shield",0L).forGetter(SuitData::shield),Codec.INT.optionalFieldOf("tint",0xffff9c26).forGetter(SuitData::tint),Codec.INT.optionalFieldOf("magnet_range",1).forGetter(SuitData::magnetRange),Codec.BOOL.optionalFieldOf("sealed",false).forGetter(SuitData::sealed)).apply(i,SuitData::new));
 public SuitData{levels=Map.copyOf(levels);active=List.copyOf(active);energy=Math.max(0,energy);shield=Math.max(0,shield);magnetRange=Math.clamp(magnetRange,0,3);}
 private static List<ItemStack> copy(List<ItemStack> items,int max){return items.stream().limit(max).map(ItemStack::copy).toList();}
 public int level(SuitUpgrade upgrade){return Math.clamp(levels.getOrDefault(upgrade.id(),0),0,upgrade.max);}
 public boolean on(String key){return active.contains(key);}
 public long capacity(){return (12_000L+level(SuitUpgrade.CAPACITY)*6_000L)*dev.admutium.techmod.energy.Energy.UNIT;}
 public static SuitData fresh(){return new SuitData(UUID.randomUUID(),0,Map.of(),List.of(),Contents.EMPTY,Optional.empty(),0,0,0xffff9c26,1,false);}
}
