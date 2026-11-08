package dev.admutium.techmod.client.suit;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
public final class SuitPreviewScreen extends Screen {
 private int x,y,w,h,bx,bw;
 public SuitPreviewScreen(){super(Component.translatable("screen.techmod.suit_preview"));}
 @Override protected void init(){w=Math.min(430,width-16);h=Math.min(240,height-16);x=(width-w)/2;y=(height-h)/2;bx=x+w/2+6;bw=w/2-18;
  addRenderableWidget(Button.builder(visibleLabel(),b->{SuitPreview.setEnabled(!SuitPreview.enabled());b.setMessage(visibleLabel());}).bounds(bx,y+42,bw,20).build());
  addRenderableWidget(Button.builder(wingLabel(),b->{SuitPreview.setWingMode((SuitPreview.wingMode()+1)%3);b.setMessage(wingLabel());}).bounds(bx,y+68,bw,20).build());
  addRenderableWidget(Button.builder(colorLabel(),b->{SuitPreview.cycleColor();b.setMessage(colorLabel());}).bounds(bx,y+94,bw,20).build());
  addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(bx,y+h-32,bw,20).build());
 }
 private Component visibleLabel(){return Component.translatable("screen.techmod.suit_visible",Component.translatable(SuitPreview.enabled()?"options.on":"options.off"));}
 private Component wingLabel(){return Component.translatable("screen.techmod.suit_wings",Component.translatable("screen.techmod.suit_wing."+SuitPreview.wingMode()));}
 private Component colorLabel(){return Component.translatable("screen.techmod.suit_color",Component.translatable("screen.techmod.suit_color."+SuitPreview.colorIndex()));}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){
  super.extractRenderState(g,mx,my,dt);g.text(font,title,x+12,y+13,0xfff0e7d6);
  g.text(font,Component.translatable("screen.techmod.suit_visual_only"),bx,y+126,0xffe0b36e);
  g.text(font,Component.translatable("screen.techmod.suit_f5"),bx,y+143,0xffb5c3ce);
  if(minecraft.player!=null)InventoryScreen.extractEntityInInventoryFollowsMouse(g,x+8,y+32,x+w/2-5,y+h-10,Math.min(76,(h-48)/2),.0625f,Math.clamp(mx,x+w/4-35,x+w/4+35),Math.clamp(my,y+h/2-8,y+h/2+8),minecraft.player);
 }
 @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractBackground(g,mx,my,dt);g.fill(x-2,y-2,x+w+2,y+h+2,0xff111820);g.fill(x,y,x+w,y+h,0xff303b47);g.fill(x+5,y+5,x+w-5,y+30,0xff1b252f);g.fill(x+10,y+32,x+w-10,y+34,0xffbd7d35);g.fill(x+8,y+38,x+w/2-5,y+h-10,0xff202b36);}
 @Override public boolean isPauseScreen(){return false;}
}
