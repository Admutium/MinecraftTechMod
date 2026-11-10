package dev.admutium.techmod.client.suit;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiGraphicsExtractor;import net.minecraft.client.gui.components.Button;import net.minecraft.network.chat.Component;
/** Shared industrial palette, inset panels and flat controls, independent of resource-pack buttons. */
final class SuitUi {
 static final int BG=0xff101820,PANEL=0xff192731,LINE=0xff354956,TEXT=0xffd7e5ec,MUTED=0xff829baa,ACCENT=0xffffa94e,GREEN=0xff76d9bd;
 static void panel(GuiGraphicsExtractor g,int x,int y,int w,int h){g.fill(x-1,y-1,x+w+1,y+h+1,LINE);g.fill(x,y,x+w,y+h,BG);g.fill(x,y,x+w,y+2,ACCENT);}
 static void slot(GuiGraphicsExtractor g,int x,int y){g.fill(x-1,y-1,x+17,y+17,LINE);g.fill(x,y,x+16,y+16,0xff090f15);g.fill(x,y,x+16,y+1,0xff03090d);}
 static final class Control extends Button {
  private final java.util.function.BooleanSupplier selected;
  Control(String text,int x,int y,int w,int h,Runnable press,java.util.function.BooleanSupplier selected){super(x,y,w,h,Component.literal(text),b->press.run(),DEFAULT_NARRATION);this.selected=selected;}
  @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float dt){int x=getX(),y=getY(),w=getWidth(),h=getHeight();boolean on=selected.getAsBoolean();g.fill(x,y,x+w,y+h,!active?0xff121c24:isHoveredOrFocused()?0xff344958:on?0xff304a50:PANEL);g.fill(x,y,x+2,y+h,on?GREEN:active?ACCENT:LINE);var f=Minecraft.getInstance().font;String text=f.plainSubstrByWidth(getMessage().getString(),w-10);g.text(f,text,x+6,y+(h-8)/2,!active?0xff657b87:on?GREEN:TEXT);}
 }
 static Control button(String text,int x,int y,int w,int h,Runnable press){return new Control(text,x,y,w,h,press,()->false);}
}
