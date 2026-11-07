package dev.admutium.techmod.client.machine;
import dev.admutium.techmod.energy.Energy;
import dev.admutium.techmod.machine.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
 public MachineScreen(MachineMenu m,Inventory i,Component title){super(m,i,title,218,228);inventoryLabelX=28;inventoryLabelY=126;titleLabelX=12;titleLabelY=10;}
 @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){super.extractBackground(g,mx,my,delta);}
 @Override public void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta){
  int x=leftPos,y=topPos;
  g.fill(x-2,y-2,x+220,y+230,0xff10151b);g.fill(x,y,x+218,y+228,0xff343e49);
  g.fill(x+4,y+4,x+214,y+25,0xff202932);g.fill(x+8,y+29,x+210,y+124,0xff182129);
  g.fill(x+10,y+26,x+208,y+28,0xffc48436);
  for(var slot:menu.slots){g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xff89969e);g.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xff11181e);}
  if(menu.kind==MachineKind.SOLAR){
   g.text(font,Component.translatable("screen.techmod.solar_rate",Energy.format(menu.solarRate(),true)),x+12,y+36,0xffedf0e5);
   g.text(font,Component.translatable("screen.techmod.solar_efficiency",String.format(java.util.Locale.ROOT,"%.1f",menu.solarEfficiency()/100.0)),x+12,y+52,0xffe7b666);
  }else if(menu.kind!=MachineKind.CABINET){
   g.text(font,Component.translatable(menu.kind==MachineKind.GENERATOR?"screen.techmod.fuel":"screen.techmod.input"),x+30,y+36,0xffa9b6be);
   g.text(font,Component.translatable(menu.kind==MachineKind.GENERATOR?"screen.techmod.charge_slot":"screen.techmod.battery"),x+126,y+36,0xffa9b6be);
   int width=menu.kind!=MachineKind.GENERATOR?menu.progress()*24/menu.processTicks():Math.min(24,menu.fuel()*24/Math.max(1,menu.fuelTotalTicks()));
   g.fill(x+74,y+57,x+100,y+65,0xff0b1115);g.fill(x+75,y+58,x+75+width,y+64,0xffd9993b);
  }
  long energy=menu.value(0),capacity=menu.value(1);int width=capacity==0?0:(int)(194*energy/capacity);
  g.fill(x+11,y+90,x+207,y+100,0xff070c10);g.fill(x+12,y+91,x+12+width,y+99,0xff80b83d);
  g.text(font,Component.translatable("screen.techmod.energy",Energy.format(energy,minecraft.hasShiftDown()),Energy.format(capacity,minecraft.hasShiftDown())),x+12,y+79,0xffedf0e5);
  g.text(font,menu.kind==MachineKind.SOLAR?Component.translatable("screen.techmod.solar_output",Energy.format(menu.value(3),true)):Component.translatable("screen.techmod.flow",Energy.format(menu.value(2),minecraft.hasShiftDown()),Energy.format(menu.value(3),minecraft.hasShiftDown())),x+12,y+104,0xffc4d0d5);
  g.text(font,Component.translatable("state.techmod."+menu.stateKey()),x+12,y+115,0xffe7b666);
  super.extractContents(g,mx,my,delta);
  if(minecraft.hasShiftDown() && mx>=x+8&&mx<x+210&&my>=y+78&&my<y+115){
   java.util.List<Component> details=java.util.List.of(Component.translatable("tooltip.techmod.limits",menu.kind==MachineKind.GENERATOR||menu.kind==MachineKind.SOLAR?"0.00":"8.00",menu.kind==MachineKind.SOLAR?"2.00":menu.kind==MachineKind.GENERATOR||menu.kind==MachineKind.CABINET?"8.00":"0.00"));
   g.setTooltipForNextFrame(font,details,java.util.Optional.empty(),mx,my);
  }
 }
 @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){g.text(font,title,titleLabelX,titleLabelY,0xfff0e7d6);g.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xffd1d9dc);}
}
