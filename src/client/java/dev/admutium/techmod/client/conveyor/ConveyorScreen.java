package dev.admutium.techmod.client.conveyor;
import dev.admutium.techmod.conveyor.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import java.util.*;
public final class ConveyorScreen extends AbstractContainerScreen<ConveyorMenu> {
 private EditBox search;private List<Item> catalog=List.of();private int page,selected;
 public ConveyorScreen(ConveyorMenu menu,Inventory inv,Component title){super(menu,inv,title,300,232);inventoryLabelX=19;inventoryLabelY=134;titleLabelX=12;titleLabelY=9;}
 private void query(String text){String q=text.toLowerCase(Locale.ROOT);catalog=BuiltInRegistries.ITEM.stream().filter(i->i!=Items.AIR&&(BuiltInRegistries.ITEM.getKey(i).toString().contains(q)||new ItemStack(i).getHoverName().getString().toLowerCase(Locale.ROOT).contains(q))).toList();page=0;}
 @Override protected void init(){super.init();search=new EditBox(font,leftPos+100,topPos+40,188,18,Component.translatable("screen.techmod.search"));search.setMaxLength(128);search.setHint(Component.translatable("screen.techmod.search"));search.setResponder(this::query);addRenderableWidget(search);query("");
  addRenderableWidget(Button.builder(Component.literal("←"),b->{page=Math.max(0,page-1);}).bounds(leftPos+100,topPos+125,26,16).build());
  addRenderableWidget(Button.builder(Component.literal("→"),b->{if((page+1)*27<catalog.size())page++;}).bounds(leftPos+262,topPos+125,26,16).build());
 }
 @Override public void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta){
  int x=leftPos,y=topPos;g.fill(x-2,y-2,x+302,y+234,0xff10151b);g.fill(x,y,x+300,y+232,0xff343e49);g.fill(x+6,y+6,x+294,y+27,0xff202932);
  g.fill(x+10,y+31,x+93,y+50,menu.filtering()?0xff567b35:0xff525c65);g.text(font,Component.translatable(menu.filtering()?"screen.techmod.filter_on":"screen.techmod.filter_off"),x+13,y+36,0xffeeeeee);
  g.fill(x+100,y+29,x+288,y+37,0xff1c252d);g.text(font,Component.translatable(menu.reverse()?"screen.techmod.from_belt":"screen.techmod.to_belt"),x+103,y+29,0xffe7b666);
  g.text(font,Component.translatable("conveyor.techmod."+switch(menu.status()){case 0->"running";case 1->"no_drive";case 2->"no_power";case 3->"redstone";default->"blocked";}),x+13,y+125,0xffe7b666);
  for(var slot:menu.slots){g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xff89969e);g.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xff11181e);}
  g.fill(x+17+(selected%3)*20,y+62+(selected/3)*20,x+35+(selected%3)*20,y+64+(selected/3)*20,0xffdba451);
  for(int i=0;i<27;i++){int at=page*27+i,cx=x+101+(i%9)*21,cy=y+63+(i/9)*20;g.fill(cx-1,cy-1,cx+17,cy+17,0xff182129);if(at<catalog.size()){var s=new ItemStack(catalog.get(at));g.item(s,cx,cy);if(mx>=cx&&mx<cx+18&&my>=cy&&my<cy+18)g.setTooltipForNextFrame(font,s,mx,my);}}
  super.extractContents(g,mx,my,delta);
  if(mx>=x+18&&mx<x+76&&my>=y+63&&my<y+121){java.util.List<Component> hint=java.util.List.of(Component.translatable("tooltip.techmod.filter_sample"));g.setTooltipForNextFrame(font,hint,java.util.Optional.empty(),mx,my);}
 }
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,titleLabelX,titleLabelY,0xfff0e7d6);g.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xffd1d9dc);}
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){double x=e.x()-leftPos,y=e.y()-topPos;
  if(x>=10&&x<=93&&y>=31&&y<=50){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);return true;}
  if(x>=100&&x<=288&&y>=28&&y<=38){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);return true;}
  if(x>=18&&x<76&&y>=63&&y<121)selected=Math.clamp((int)((y-63)/20)*3+(int)((x-18)/20),0,8);
  if(x>=100&&x<289&&y>=62&&y<122){int cell=(int)((y-62)/20)*9+(int)((x-100)/21),index=page*27+cell;if(index<catalog.size())ClientPlayNetworking.send(new FilterPayload(menu.containerId,selected,BuiltInRegistries.ITEM.getKey(catalog.get(index)).toString()));return true;}
  return super.mouseClicked(e,twice);
 }
}
