package dev.admutium.techmod.suit;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import java.util.*;

/** Resource keys group host-rock variants; old saves containing block IDs remain valid. */
public final class SuitOres {
 public record Resource(String id,Item icon,List<String> blocks) {}
 private static final Map<Block,String> KEYS=new java.util.concurrent.ConcurrentHashMap<>();
 public static String key(Block block){return KEYS.computeIfAbsent(block,b->{
  var id=BuiltInRegistries.BLOCK.getKey(b);String material=id.getPath().replaceFirst("^(deepslate_|nether_|end_|stone_)","").replaceFirst("_ore$","");
  String path=switch(material){case "iron","gold","copper","tin"->"raw_"+material;case "quartz"->"quartz";case "lapis"->"lapis_lazuli";default->material;};
  var resource=Identifier.fromNamespaceAndPath(id.getNamespace(),path);if(BuiltInRegistries.ITEM.containsKey(resource))return resource.toString();
  return id.getNamespace()+":"+material;
 });}
 public static List<Resource> catalog(){var groups=new LinkedHashMap<String,List<String>>();for(var b:BuiltInRegistries.BLOCK)if(SuitRuntime.ore(b.defaultBlockState()))groups.computeIfAbsent(key(b),k->new ArrayList<>()).add(BuiltInRegistries.BLOCK.getKey(b).toString());var result=new ArrayList<Resource>();groups.forEach((key,blocks)->{var id=Identifier.tryParse(key);Item icon=id!=null&&BuiltInRegistries.ITEM.containsKey(id)?BuiltInRegistries.ITEM.getValue(id):BuiltInRegistries.BLOCK.getValue(Identifier.parse(blocks.getFirst())).asItem();result.add(new Resource(key,icon,List.copyOf(blocks)));});return List.copyOf(result);}
 public static boolean matches(List<String> filter,Block block){return filter.isEmpty()||filter.contains(key(block))||filter.contains(BuiltInRegistries.BLOCK.getKey(block).toString());}
 public static boolean toggle(Set<String> filter,String key){var resource=catalog().stream().filter(r->r.id().equals(key)).findFirst();if(resource.isEmpty())return false;var r=resource.get();boolean selected=filter.remove(r.id());for(String variant:r.blocks())selected=filter.remove(variant)||selected;if(!selected&&filter.size()<64)filter.add(r.id());return true;}
}
