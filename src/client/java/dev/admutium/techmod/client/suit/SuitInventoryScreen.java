package dev.admutium.techmod.client.suit;
import dev.admutium.techmod.suit.*;import net.minecraft.client.gui.GuiGraphicsExtractor;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.network.chat.Component;import net.minecraft.world.entity.player.Inventory;
public final class SuitInventoryScreen extends AbstractContainerScreen<SuitInventoryMenu>{
 public SuitInventoryScreen(SuitInventoryMenu menu,Inventory inv,Component title){super(menu,inv,title,186,206);inventoryLabelX=12;inventoryLabelY=96;titleLabelX=12;titleLabelY=10;}
 @Override public void extractContents(GuiGraphicsExtractor g,int mx,int my,float dt){int x=leftPos,y=topPos;g.fill(x-2,y-2,x+188,y+208,0xff0e1820);g.fill(x,y,x+186,y+206,0xff303e49);for(var slot:menu.slots){g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xff89969e);g.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xff11181e);}g.text(font,SuitClient.homeText(),x+12,y+27,0xffffbb65);g.text(font,"Капсулы: "+SuitClient.data.contents().nutrients(),x+36,y+74,0xffa9dba2);super.extractContents(g,mx,my,dt);}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,12,10,0xffe9eef2);g.text(font,playerInventoryTitle,12,96,0xffbbcbd5);}
}
