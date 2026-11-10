package dev.admutium.techmod.client.suit;

import dev.admutium.techmod.energy.Energy;
import dev.admutium.techmod.suit.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;

/** Compact fixed layout. Crouching never opens details or changes numeric precision. */
public final class SuitHud {
 public record Layout(int x,int y,int width) {}
 public static Layout layout(int width,int height){return new Layout((width-140)/2,Math.max(8,height-94),140);}
 public static String rate(long value){return value>0&&value<Energy.UNIT?"<1":Energy.format(value);}
 public static void energy(GuiGraphicsExtractor g,Minecraft c,SuitData d){
  var a=layout(c.getWindow().getGuiScaledWidth(),c.getWindow().getGuiScaledHeight());int x=a.x(),y=a.y(),w=a.width();
  int used=0;for(int i=0;i<36;i++)if(!c.player.getInventory().getItem(i).isEmpty())used++;for(var item:d.contents().cargo())if(!item.isEmpty())used++;
  String cargo=used+"/"+(36+(d.level(SuitUpgrade.INVENTORY)>0?9:0));
  g.fill(x-3,y-3,x+w+3,y+(d.level(SuitUpgrade.SHIELD)>0?27:16),0x990d1820);
  g.text(c.font,Energy.format(d.energy())+"/"+Energy.format(d.capacity())+" ТЕ",x,y,0xffffbf68);
  int icon=x+w-c.font.width(cargo)-8;for(int row=0;row<2;row++)for(int col=0;col<2;col++)g.fill(icon+col*3,y+1+row*3,icon+col*3+2,y+3+row*3,0xff8ba4b1);g.text(c.font,cargo,x+w-c.font.width(cargo),y,used>=36+(d.level(SuitUpgrade.INVENTORY)>0?9:0)?0xffff8b73:0xffbac9d2);
  g.fill(x,y+10,x+w,y+13,0xff243640);int fill=(int)Math.clamp(w*d.energy()/Math.max(1,d.capacity()),0,w);g.fill(x,y+10,x+fill,y+13,d.energy()<d.capacity()/10?0xffda6751:0xffe9ab50);
  if(d.level(SuitUpgrade.SHIELD)>0){String shield="Щит "+d.shield()/1000;g.text(c.font,shield,x,y+16,0xff99d8d3);}

 }
 public static void target(GuiGraphicsExtractor g,Minecraft c,LivingEntity target){
  int x=c.getWindow().getGuiScaledWidth()/2,y=c.getWindow().getGuiScaledHeight()/2-29;
  String hp=(int)Math.ceil(target.getHealth())+"/"+(int)Math.ceil(target.getMaxHealth());int hpWidth=c.font.width(hp);
  String name=c.font.plainSubstrByWidth(target.getName().getString(),Math.max(12,96-hpWidth-6));
  g.fill(x-52,y-3,x+52,y+19,0x990d1820);g.text(c.font,name,x-48,y,0xffedf0ed);g.text(c.font,hp,x+48-hpWidth,y,0xffe1b3a8);
  g.fill(x-48,y+12,x+48,y+15,0xff583434);g.fill(x-48,y+12,x-48+(int)(96*Math.clamp(target.getHealth()/target.getMaxHealth(),0,1)),y+15,0xffd77c68);
 }
}
