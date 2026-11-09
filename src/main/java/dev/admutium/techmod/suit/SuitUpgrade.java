package dev.admutium.techmod.suit;
/** Free permanent upgrades in this testing release; maxima and costs are provisional balance values. */
public enum SuitUpgrade {
 INVENTORY("storage",1),BLACKLIST("storage",1),MAGNET("storage",3),FEED("storage",1),NUTRIENTS("storage",6),COMPASS("storage",1),
 FLIGHT("movement",3),RUN("movement",10),STEP("movement",1),JUMP("movement",10),FALL("movement",10),HEALTH_LIMIT("movement",1),
 SWIM("water",10),AIR("water",10),AQUA_MINING("water",1),
 MINING("mining",20),ORE_SCAN("mining",1),VEIN("mining",4),LIGHT("mining",10),NIGHT("mining",1),
 ARMOR("defense",10),SHIELD("defense",5),SHIELD_STRENGTH("defense",5),SHIELD_EFFICIENCY("defense",5),MAGIC("defense",5),PROJECTILE("defense",5),EXPLOSION("defense",5),FIRE("defense",1),KNOCKBACK("defense",5),
 DAMAGE("combat",10),ENEMY_SCAN("combat",1),CREEPER("combat",1),RESCUE("combat",1),CLOAK("combat",1),TARGET("combat",1),
 BEACON("utility",1),VISOR("utility",1),WARP("utility",1),PLANT("utility",1),
 CAPACITY("energy",5),CHARGE("energy",5),EFFICIENCY("energy",5),POWER("energy",5);
 public final String branch;public final int max;
 SuitUpgrade(String branch,int max){this.branch=branch;this.max=max;}
 public String id(){return name().toLowerCase(java.util.Locale.ROOT);}
 public static SuitUpgrade byId(String id){try{return valueOf(id.toUpperCase(java.util.Locale.ROOT));}catch(IllegalArgumentException e){return null;}}
 public static final String[] BRANCHES={"storage","movement","water","mining","defense","combat","utility","energy"};
}
