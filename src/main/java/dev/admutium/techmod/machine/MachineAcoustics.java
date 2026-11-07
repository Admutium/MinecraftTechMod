package dev.admutium.techmod.machine;
/** Shared, testable gain curve; the client additionally obeys Minecraft's Blocks and Master sliders. */
public final class MachineAcoustics {
 public static double gain(double distance,double setting,boolean wall,int voices){
  if(distance>=6||setting<=0)return 0;
  double fade=distance<=2?1:Math.pow((6-distance)/4,2);
  return .7*Math.clamp(setting,0,1)*fade*(wall?.2:1)/Math.sqrt(Math.clamp(voices,1,12));
 }
 private MachineAcoustics(){}
}
