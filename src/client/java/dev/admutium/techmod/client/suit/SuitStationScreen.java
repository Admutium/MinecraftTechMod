package dev.admutium.techmod.client.suit;
import dev.admutium.techmod.suit.*;import dev.admutium.techmod.energy.Energy;import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.*;import net.minecraft.client.gui.components.*;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;import net.minecraft.client.input.MouseButtonEvent;import net.minecraft.network.chat.Component;import net.minecraft.world.entity.player.Inventory;import net.minecraft.world.item.*;import net.minecraft.core.registries.BuiltInRegistries;import java.util.*;
/** Compact station: service bay and a full-width graph share the same window. */
public final class SuitStationScreen extends AbstractContainerScreen<SuitStationMenu> {
 private final Map<Button,SuitUpgrade> nodes=new LinkedHashMap<>();private SuitUpgrade selected=SuitUpgrade.INVENTORY;
 private String branch="storage",filter="";private EditBox search;private List<Item> catalog=List.of();private int page;private Button buy;private boolean tree;
 public SuitStationScreen(SuitStationMenu menu,Inventory inv,Component title){super(menu,inv,title,380,240);inventoryLabelX=12;inventoryLabelY=136;titleLabelX=12;titleLabelY=10;}
 void send(String key,String value){ClientPlayNetworking.send(new SuitPackets.Action(key,value,menu.containerId));}
 Button button(String text,int x,int y,int w,Runnable action){return addRenderableWidget(SuitUi.button(text,leftPos+x,topPos+y,w,18,action));}
 void view(boolean value){tree=value;filter="";rebuildWidgets();}
 @Override protected void init(){super.init();menu.treeView=tree||!filter.isEmpty();nodes.clear();buy=null;
 if(!tree&&filter.isEmpty()){
 button("Создать прототип",36,29,140,()->send("create",""));button("Надеть",12,69,78,()->send("equip",""));button("Снять",94,69,82,()->send("remove",""));button("Открыть всё бесплатно",12,89,164,()->{send("all","");view(true);});button("Капсулы / вернуть вещи",12,109,164,()->send(SuitClient.data.sealed()?"recover":"feed",""));button("Цвет",12,127,48,()->{filter="color";rebuildWidgets();});button("Фильтр",64,127,54,()->{filter="blacklist";rebuildWidgets();});button("Руды",122,127,54,()->{filter="ores";rebuildWidgets();});button("Модификации →",190,29,178,()->view(true));button("Радиус магнита",190,176,178,()->send("range",Integer.toString((SuitClient.data.magnetRange()+1)%4)));return;
 }
 button("← Станция",270,7,98,()->view(false));
 if(tree&&filter.isEmpty()){
 for(int i=0;i<8;i++){String id=SuitUpgrade.BRANCHES[i];addRenderableWidget(new SuitUi.Control(Component.translatable("suit.branch."+id).getString(),leftPos+12+i*44,topPos+34,40,18,()->{branch=id;selected=Arrays.stream(SuitUpgrade.values()).filter(u->u.branch.equals(id)).findFirst().orElseThrow();rebuildWidgets();},()->branch.equals(id)));}
 for(var u:SuitUpgrade.values())if(u.branch.equals(branch)){var n=SuitTree.node(u);var b=addRenderableWidget(new SuitUi.Control(SuitTree.shortName(u),leftPos+12+n.column()*88,topPos+60+n.row()*24,80,21,()->selected=u,()->selected==u));nodes.put(b,u);}
 buy=button("Улучшить бесплатно",12,218,356,()->send("upgrade",selected.id()));
 }else {
 search=new EditBox(font,leftPos+12,topPos+35,356,18,Component.literal("Название / mod:id / HEX"));search.setMaxLength(128);search.setResponder(this::query);addRenderableWidget(search);query("");if(filter.equals("color")){button("Применить #RRGGBB",12,68,356,()->send("color",search.getValue()));button("Оранжевый",12,94,172,()->send("color","ff9c26"));button("Бирюзовый",196,94,172,()->send("color","5ddfd0"));}else{button("←",12,228,30,()->page=Math.max(0,page-1));button("→",338,228,30,()->{if((page+1)*64<catalog.size())page++;});}}
 }
 void query(String value){String q=value.toLowerCase(Locale.ROOT);catalog=BuiltInRegistries.ITEM.stream().filter(i->i!=Items.AIR&&(!filter.equals("ores")||i instanceof BlockItem bi&&SuitRuntime.ore(bi.getBlock().defaultBlockState()))&&(BuiltInRegistries.ITEM.getKey(i).toString().contains(q)||new ItemStack(i).getHoverName().getString().toLowerCase(Locale.ROOT).contains(q))).toList();page=0;}
 @Override public void extractContents(GuiGraphicsExtractor g,int mx,int my,float dt){int x=leftPos,y=topPos;var d=SuitClient.data;SuitUi.panel(g,x,y,380,240);
 if(!tree&&filter.isEmpty()){
  g.fill(x+181,y+27,x+182,y+228,SuitUi.LINE);for(var slot:menu.slots)SuitUi.slot(g,x+slot.x,y+slot.y);g.text(font,Energy.format(d.energy())+" / "+Energy.format(d.capacity())+" ТЕ",x+12,y+53,SuitUi.ACCENT);
  g.text(font,"ЭНЕРГОСИСТЕМА",x+190,y+61,SuitUi.ACCENT);g.text(font,"Зарядка +"+Energy.format(SuitClient.chargeRate,true)+" ТЕ/с",x+190,y+79,SuitUi.GREEN);g.text(font,"Вход +"+Energy.format(SuitClient.stationIn,true)+" ТЕ/с",x+190,y+94,SuitUi.MUTED);g.text(font,"Макс. зарядка: "+(64+64*d.level(SuitUpgrade.CHARGE)),x+190,y+109,SuitUi.MUTED);g.text(font,"Отдача: "+(20+10*d.level(SuitUpgrade.POWER))+" ТЕ/тик",x+190,y+124,SuitUi.MUTED);g.text(font,"Еда: "+d.contents().nutrients()+" / "+32*d.level(SuitUpgrade.NUTRIENTS),x+190,y+145,SuitUi.TEXT);g.text(font,"Тара: "+d.contents().emptyCans()+" / "+32*d.level(SuitUpgrade.NUTRIENTS),x+190,y+158,SuitUi.TEXT);g.text(font,"Радиус: "+Math.min(d.magnetRange(),d.level(SuitUpgrade.MAGNET))+" м",x+190,y+199,SuitUi.MUTED);g.text(font,"Прототип: улучшения бесплатны",x+12,y+228,SuitUi.MUTED);
 }else if(tree&&filter.isEmpty()){
  for(var e:nodes.entrySet()){var u=e.getValue();var n=SuitTree.node(u);if(n.parent()!=null&&n.parent().branch.equals(branch)){var p=SuitTree.node(n.parent());int ax=x+52+p.column()*88,ay=y+70+p.row()*24,bx=x+52+n.column()*88,by=y+70+n.row()*24;int color=d.level(n.parent())>=n.required()?0xff547c79:SuitUi.LINE;link(g,x,y,p,n,color);}}
  g.fill(x+12,y+154,x+368,y+216,SuitUi.PANEL);g.text(font,Component.translatable("suit.upgrade."+selected.id()).getString()+"  "+d.level(selected)+"/"+selected.max,x+18,y+158,SuitUi.ACCENT);var lines=font.split(Component.literal(SuitDescriptions.detail(selected)),344);for(int i=0;i<Math.min(2,lines.size());i++)g.text(font,lines.get(i),x+18,y+170+i*10,SuitUi.TEXT);
  var n=SuitTree.node(selected);String pre=n.parent()==null?"Самостоятельный модуль":"Нужно: "+Component.translatable("suit.upgrade."+n.parent().id()).getString()+" "+n.required();g.text(font,pre,x+18,y+192,SuitTree.available(d,selected)?SuitUi.GREEN:SuitUi.MUTED);int cx=x+18;g.text(font,"Проект цены:",cx,y+208,SuitUi.MUTED);cx+=font.width("Проект цены:")+7;for(var cost:SuitTree.cost(selected,Math.min(selected.max,d.level(selected)+1))){g.item(cost,cx,y+200);g.text(font,"×"+cost.getCount(),cx+17,y+208,SuitUi.TEXT);if(mx>=cx&&mx<cx+35&&my>=y+200&&my<y+216)g.setTooltipForNextFrame(font,cost,mx,my);cx+=49;}
  buy.active=!d.id().equals(SuitData.EMPTY_ID)&&SuitTree.available(d,selected);buy.setMessage(Component.literal(d.level(selected)>=selected.max?"Полностью открыто":buy.active?"Улучшить бесплатно":"Сначала откройте предыдущий узел"));
 }else if(!filter.equals("color")){var selectedIds=filter.equals("ores")?d.contents().ores():d.contents().blacklist();for(int i=0;i<64;i++){int index=page*64+i,cx=x+14+(i%16)*22,cy=y+66+(i/16)*31;if(index>=catalog.size())break;var item=catalog.get(index);var stack=new ItemStack(item);g.fill(cx-1,cy-1,cx+17,cy+17,selectedIds.contains(BuiltInRegistries.ITEM.getKey(item).toString())?0xff8a6839:SuitUi.PANEL);g.item(stack,cx,cy);if(mx>=cx&&mx<cx+18&&my>=cy&&my<cy+18)g.setTooltipForNextFrame(font,stack,mx,my);}g.text(font,filter.equals("ores")?"В поле: Tab → Фильтр ресурсов":"Выбранные предметы не подбираются",x+12,y+207,SuitUi.MUTED);}
 super.extractContents(g,mx,my,dt);
 for(var e:nodes.entrySet()){var b=e.getKey();var u=e.getValue();int filled=(int)(74.*d.level(u)/u.max);g.fill(b.getX()+3,b.getY()+18,b.getX()+77,b.getY()+20,SuitUi.LINE);g.fill(b.getX()+3,b.getY()+18,b.getX()+3+filled,b.getY()+20,SuitUi.GREEN);if(mx>=b.getX()&&mx<b.getX()+80&&my>=b.getY()&&my<b.getY()+21){var tip=new ArrayList<Component>();tip.add(Component.translatable("suit.upgrade."+u.id()).append(" "+d.level(u)+"/"+u.max));tip.add(Component.literal(SuitDescriptions.detail(u)));if(minecraft.player.isCreative())for(int level=1;level<=u.max;level++)tip.add(Component.literal((level<=d.level(u)?"✓ ":"○ ")+SuitTree.gradeId(u,level)));g.setTooltipForNextFrame(font,tip,Optional.empty(),mx,my);}}
 if(tree&&filter.isEmpty()&&minecraft.player.isCreative())g.text(font,SuitTree.gradeId(selected,Math.min(selected.max,d.level(selected)+1)),x+12,y+21,SuitUi.MUTED);
 }
 private static void line(GuiGraphicsExtractor g,int ax,int ay,int bx,int by,int color){g.fill(Math.min(ax,bx),Math.min(ay,by),Math.max(ax,bx)+1,Math.max(ay,by)+1,color);}
 private static void link(GuiGraphicsExtractor g,int x,int y,SuitTree.Node p,SuitTree.Node n,int color){int px=x+12+p.column()*88,py=y+60+p.row()*24,cx=x+12+n.column()*88,cy=y+60+n.row()*24;
  if(n.column()==p.column()){
   if(n.row()==p.row()+1)line(g,px+40,py+21,cx+40,cy,color);
   else{line(g,px,py+10,px-4,py+10,color);line(g,px-4,py+10,px-4,cy+10,color);line(g,px-4,cy+10,cx,cy+10,color);}
  }else if(n.column()>p.column()){
   int a=px+83,b=cx-4;line(g,px+80,py+10,a,py+10,color);
   if(n.column()==p.column()+1){line(g,a,py+10,a,cy+10,color);line(g,a,cy+10,cx,cy+10,color);}
   else{int bridge=py-4;line(g,a,py+10,a,bridge,color);line(g,a,bridge,b,bridge,color);line(g,b,bridge,b,cy+10,color);line(g,b,cy+10,cx,cy+10,color);}
  }else{int a=px-4,b=cx+83;line(g,px,py+10,a,py+10,color);line(g,a,py+10,a,cy+10,color);line(g,a,cy+10,b,cy+10,color);line(g,b,cy+10,cx+80,cy+10,color);}
 }
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){double x=e.x()-leftPos,y=e.y()-topPos;if(!filter.isEmpty()&&!filter.equals("color")&&x>=12&&x<368&&y>=65&&y<177){int index=page*64+(int)((y-65)/31)*16+(int)((x-12)/22);if(index<catalog.size())send(filter,BuiltInRegistries.ITEM.getKey(catalog.get(index)).toString());return true;}return super.mouseClicked(e,twice);}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,tree?"КОНТУР / МОДИФИКАЦИИ":"КОНТУР / СТАНЦИЯ",12,11,SuitUi.TEXT);}
}
