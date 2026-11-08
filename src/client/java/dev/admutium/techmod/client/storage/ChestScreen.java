package dev.admutium.techmod.client.storage;
import dev.admutium.techmod.storage.ReinforcedChestMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class ChestScreen extends AbstractContainerScreen<ReinforcedChestMenu> {
 public ChestScreen(ReinforcedChestMenu m,Inventory inv,Component title){super(m,inv,title,218,214);titleLabelX=12;titleLabelY=10;inventoryLabelX=12;inventoryLabelY=114;}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){g.text(font,title,titleLabelX,titleLabelY,0xfff0e7d6);g.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xffd1d9dc);}
 @Override public void extractContents(GuiGraphicsExtractor g,int mx,int my,float dt){int x=leftPos,y=topPos;
  g.fill(x-2,y-2,x+220,y+216,0xff131b22);g.fill(x,y,x+218,y+214,0xff343e49);g.fill(x+5,y+5,x+213,y+23,0xff202932);g.fill(x+10,y+24,x+208,y+26,0xffbf843e);
  for(var s:menu.slots){g.fill(x+s.x-1,y+s.y-1,x+s.x+17,y+s.y+17,0xff83919b);g.fill(x+s.x,y+s.y,x+s.x+16,y+s.y+16,0xff17212b);}
  g.text(font,Component.translatable("screen.techmod.sample"),x+174,y+32,0xffe9c88f);super.extractContents(g,mx,my,dt);
  if(mx>=x+184&&mx<x+212&&my>=y+40&&my<y+70)g.setTooltipForNextFrame(font,java.util.List.of(Component.translatable("tooltip.techmod.sample")),java.util.Optional.empty(),mx,my);
 }
}
