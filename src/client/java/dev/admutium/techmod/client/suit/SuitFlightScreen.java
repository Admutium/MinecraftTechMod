package dev.admutium.techmod.client.suit;

import dev.admutium.techmod.energy.Energy;
import dev.admutium.techmod.suit.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Every flight mode is visible and selectable directly, with the actual rebound control keys. */
public final class SuitFlightScreen extends Screen {
 public SuitFlightScreen(){super(Component.literal("Режим полёта"));}
 @Override protected void init(){
  String[] ids={"off","jet","elytra","gravity"};String[] names={"Выключен","Ранец","Элитра","Гравитация"};var d=SuitClient.wornData();
  for(int i=0;i<4;i++){String id=ids[i];var button=Button.builder(Component.literal(names[i]),b->{SuitClient.action("flight",id);onClose();}).bounds(width/2-140,height/2-60+i*23,280,20).build();button.active=i==0||d.level(SuitUpgrade.FLIGHT)>=i;addRenderableWidget(button);}
  addRenderableWidget(Button.builder(Component.literal("Назад"),b->minecraft.setScreenAndShow(new SuitRadialScreen())).bounds(width/2-60,height/2+75,120,18).build());
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);var d=SuitClient.wornData();int x=width/2;g.text(font,title,x-font.width(title)/2,height/2-84,0xffffc078);String jump=minecraft.options.keyJump.getTranslatedKeyMessage().getString(),sprint=minecraft.options.keySprint.getTranslatedKeyMessage().getString(),down=minecraft.options.keyShift.getTranslatedKeyMessage().getString();String[] hints={"Ранец: удерживай "+jump+" для подъёма","Элитра: "+jump+" в воздухе; "+sprint+" — разгон","Гравитация: "+jump+" вверх, "+down+" вниз"};for(int i=0;i<3;i++)g.text(font,hints[i],x-140,height/2+39+i*11,0xffc7d8e1);g.text(font,"Выбран: "+SuitClient.flightLabel()+" · "+Energy.format(d.energy())+" ТЕ",x-140,height/2-74,d.energy()>0?0xffc7dfb1:0xffff9c83);}
 @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){if(SuitClient.menuKey.matches(e)){onClose();return true;}return super.keyPressed(e);}
 @Override public boolean isPauseScreen(){return false;}
}
