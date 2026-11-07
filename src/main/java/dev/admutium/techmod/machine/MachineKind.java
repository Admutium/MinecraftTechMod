package dev.admutium.techmod.machine;
public enum MachineKind {
    CABINET("battery_cabinet",8), GENERATOR("coal_generator",2), FURNACE("electric_furnace",3), CRUSHER("crusher",3), SOLAR("solar_panel",0);
    public final String id; public final int slots;
    MachineKind(String id,int slots){this.id=id;this.slots=slots;}
}
