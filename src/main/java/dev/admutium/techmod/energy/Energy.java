package dev.admutium.techmod.energy;

/** 18000 exact units per TE: both 100 TE/900 ticks and 16 TE/160 ticks are integral. */
public final class Energy {
    private Energy() {}
    public static final long UNIT=18_000, BATTERY=300*UNIT, BUFFER=300*UNIT;
    public static final long GENERATION=2_000, SMELT_TICK=1_800, BATTERY_RATE=3_600, PORT_RATE=7_200;
    public static final int BURN_TICKS=900, SMELT_TICKS=160;
    public static String format(long units) { return Long.toString(Math.round(Math.max(0,units)/(double)UNIT)); }
    public static String format(long units,boolean detailed) { return detailed?String.format(java.util.Locale.ROOT,"%.2f",units/(double)UNIT):format(units); }
    public static long delivered(long sent,double length) { return Math.max(0,(long)Math.floor(sent*Math.max(0,1-length*.001))); }
    public static int wireCost(double distance) { return Math.max(1,(int)Math.ceil(distance-1e-9)); }
    public static long clamp(long value,long capacity) { return Math.clamp(value,0,capacity); }
}
