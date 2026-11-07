package dev.admutium.techmod.client.sound;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
public final class MachineAudioScreen extends Screen {
 private final Screen parent;
 public MachineAudioScreen(Screen parent){super(Component.translatable("options.techmod.sounds"));this.parent=parent;}
 @Override protected void init(){
  addRenderableWidget(new AbstractSliderButton(width/2-150,height/2-25,300,20,Component.empty(),MachineAudioConfig.volume){
   {updateMessage();}
   @Override protected void updateMessage(){setMessage(Component.translatable("options.techmod.machine_volume",(int)Math.round(value*100)));}
   @Override protected void applyValue(){MachineAudioConfig.volume=value;}
  });
  addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(width/2-75,height/2+45,150,20).build());
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float delta){
  super.extractRenderState(g,x,y,delta);g.text(font,title,(width-font.width(title))/2,height/2-65,0xffeeeeee);
  var hint=Component.translatable("options.techmod.sound_hint");g.text(font,hint,(width-font.width(hint))/2,height/2+8,0xffb9c4ce);
 }
 @Override public void onClose(){MachineAudioConfig.save();minecraft.setScreenAndShow(parent);}
}
