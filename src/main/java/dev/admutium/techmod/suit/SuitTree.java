package dev.admutium.techmod.suit;
import java.util.*;import net.minecraft.world.item.*;
/** One shared specification for graph rendering, prerequisite checks and proposed material prices. */
public final class SuitTree {
 public record Node(int column,int row,SuitUpgrade parent,int required) {}
 public static Node node(SuitUpgrade u){return switch(u){
  case INVENTORY,RUN,SWIM,MINING,ARMOR,DAMAGE,VISOR,CAPACITY->new Node(0,0,null,0);
  case BLACKLIST->new Node(1,0,SuitUpgrade.INVENTORY,1);
  case MAGNET->new Node(0,1,null,0);case NUTRIENTS->new Node(0,2,null,0);case FEED->new Node(1,2,SuitUpgrade.NUTRIENTS,1);case COMPASS->new Node(0,3,null,0);
  case STEP->new Node(1,0,SuitUpgrade.RUN,2);case JUMP->new Node(2,0,SuitUpgrade.RUN,1);case FALL->new Node(0,1,null,0);case HEALTH_LIMIT->new Node(1,1,SuitUpgrade.FALL,3);case FLIGHT->new Node(0,2,null,0);
  case AIR->new Node(0,1,null,0);case AQUA_MINING->new Node(1,1,SuitUpgrade.AIR,1);
  case VEIN->new Node(1,0,SuitUpgrade.MINING,2);case ORE_SCAN->new Node(0,1,null,0);case LIGHT->new Node(0,2,null,0);case NIGHT->new Node(1,2,SuitUpgrade.LIGHT,1);
  case SHIELD->new Node(1,0,SuitUpgrade.ARMOR,1);case SHIELD_STRENGTH->new Node(2,0,SuitUpgrade.SHIELD,1);case SHIELD_EFFICIENCY->new Node(3,0,SuitUpgrade.SHIELD,1);case MAGIC->new Node(0,1,SuitUpgrade.ARMOR,1);case PROJECTILE->new Node(1,1,SuitUpgrade.ARMOR,1);case EXPLOSION->new Node(2,1,SuitUpgrade.ARMOR,1);case FIRE->new Node(0,2,SuitUpgrade.ARMOR,2);case KNOCKBACK->new Node(1,2,SuitUpgrade.EXPLOSION,1);
  case ENEMY_SCAN->new Node(0,1,null,0);case CREEPER->new Node(1,1,SuitUpgrade.ENEMY_SCAN,1);case TARGET->new Node(2,1,SuitUpgrade.ENEMY_SCAN,1);case RESCUE->new Node(1,0,SuitUpgrade.DAMAGE,3);case CLOAK->new Node(0,2,null,0);
  case PLANT->new Node(1,0,SuitUpgrade.VISOR,1);
  case BEACON->new Node(0,1,null,0);case WARP->new Node(1,1,SuitUpgrade.BEACON,1);
  case CHARGE->new Node(1,0,SuitUpgrade.CAPACITY,1);case EFFICIENCY->new Node(1,1,SuitUpgrade.CAPACITY,1);case POWER->new Node(2,0,SuitUpgrade.CHARGE,1);
 };}
 public static String gradeId(SuitUpgrade u,int level){return "techmod:suit/"+u.id()+"/"+Math.clamp(level,1,u.max);}
 public static boolean available(SuitData d,SuitUpgrade u){var n=node(u);return d.level(u)<u.max&&(n.parent()==null||d.level(n.parent())>=n.required());}
 public static List<ItemStack> cost(SuitUpgrade u,int next){int tier=Math.max(1,next);Item material=switch(u.branch){case "movement"->Items.GOLD_INGOT;case "water"->Items.COPPER_INGOT;case "mining"->Items.LAPIS_LAZULI;case "defense","combat"->Items.IRON_INGOT;case "energy"->Items.GOLD_INGOT;default->Items.COPPER_INGOT;};if(u==SuitUpgrade.FLIGHT)return List.of(new ItemStack(Items.DIAMOND,2*tier),new ItemStack(tier==3?Items.NETHER_STAR:Items.ENDER_PEARL,tier==3?1:4));if(u==SuitUpgrade.WARP)return List.of(new ItemStack(Items.NETHER_STAR),new ItemStack(Items.ENDER_PEARL,16));return List.of(new ItemStack(material,2+tier*2),new ItemStack(Items.REDSTONE,2+tier));}
 public static String shortName(SuitUpgrade u){return switch(u){case INVENTORY->"Груз";case BLACKLIST->"Фильтр";case NUTRIENTS->"Капсулы";case FEED->"Питание";case HEALTH_LIMIT->"Предел";case AQUA_MINING->"Добыча";case MINING->"Бурение";case ORE_SCAN->"Ресурсы";case VEIN->"Жила";case LIGHT->"Фонарь";case NIGHT->"Ночь";case SHIELD_STRENGTH->"Стойкость";case SHIELD_EFFICIENCY->"Расход";case MAGIC->"Магия";case PROJECTILE->"Снаряды";case EXPLOSION->"Взрывы";case FIRE->"Ожоги";case KNOCKBACK->"Устойчив.";case ENEMY_SCAN->"Цели";case CREEPER->"Угрозы";case RESCUE->"Спасение";case TARGET->"Здоровье";case EFFICIENCY->"КПД";default->net.minecraft.network.chat.Component.translatable("suit.upgrade."+u.id()).getString();};}
}
