"""Normalize approved sprite atlases; create data-driven ore, crop and processing assets."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,colorsys,zipfile
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/techmod';D=R/'src/main/resources/data';ART=R/'tools/art'
def write(path,obj):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
def asset(path,obj):write(A/path,obj)
def save(name,im):
 p=A/f'textures/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def clean(im):
 im=im.convert('RGBA');im.putalpha(im.getchannel('A').point(lambda a:255 if a>=230 else 0))
 # Discard disconnected generator edge noise; all approved icons form one main component.
 from collections import deque
 mask=im.getchannel('A');seen=set();components=[]
 for y in range(im.height):
  for x in range(im.width):
   if (x,y) in seen or not mask.getpixel((x,y)):continue
   seen.add((x,y));q=deque([(x,y)]);group=[]
   while q:
    p=q.popleft();group.append(p)
    for nx,ny in [(p[0]-1,p[1]),(p[0]+1,p[1]),(p[0],p[1]-1),(p[0],p[1]+1)]:
     if 0<=nx<im.width and 0<=ny<im.height and (nx,ny)not in seen and mask.getpixel((nx,ny)):seen.add((nx,ny));q.append((nx,ny))
   components.append(group)
 if components:
  minimum=max(map(len,components))*.004
  for group in components:
   if len(group)<minimum:
    for p in group:im.putpixel(p,(0,0,0,0))
 return im
atlas=Image.open(ART/'materials_source.png')
names=['hammer','cutters','copper_wire','raw_tin','crushed_tin','iron_plate','caoutchouc','rubber']
for i,name in enumerate(names):
 x=i%4;y=i//4;im=clean(atlas.crop((x*atlas.width//4,y*atlas.height//2,(x+1)*atlas.width//4,(y+1)*atlas.height//2)))
 im=im.crop(im.getbbox());im.thumbnail((116,116),Image.Resampling.NEAREST);out=Image.new('RGBA',(128,128));out.paste(im,((128-im.width)//2,(128-im.height)//2));save('item/'+name,out)
 if name=='iron_plate':out.save(ART/'plate_source.png')
for metal,hue,sat in [('copper',.055,.66),('gold',.13,.7),('tin',.60,.14)]:
 im=Image.open(A/'textures/item/iron_plate.png').convert('RGBA')
 for y in range(128):
  for x in range(128):
   r,g,b,a=im.getpixel((x,y));v=max(r,g,b)/255;rgb=colorsys.hsv_to_rgb(hue,sat,v);im.putpixel((x,y),tuple(round(v*255)for v in rgb)+(a,))
 save('item/'+metal+'_plate',im)
names+=['copper_plate','gold_plate','tin_plate']
tin=Image.open(ART/'tin_source.png');im=clean(tin.crop((0,0,tin.width//2,tin.height)));im=im.crop(im.getbbox());im.thumbnail((116,116),Image.Resampling.NEAREST);out=Image.new('RGBA',(128,128));out.paste(im,((128-im.width)//2,(128-im.height)//2));save('item/tin_ingot',out);names+=['tin_ingot']
save('block/tin_block',tin.crop((951,94,1708,838)).convert('RGB').resize((32,32),Image.Resampling.NEAREST))
plant=Image.open(ART/'dandelion_source.png')
for i,name in enumerate(['dandelion_stage_0','dandelion_stage_1','dandelion_stage_2','dandelion_stage_3','dandelion_roots','dandelion_seeds']):
 x=i%4;y=i//4;cell=clean(plant.crop((x*plant.width//4,y*plant.height//2,(x+1)*plant.width//4,(y+1)*plant.height//2)))
 if i<4:
  # Keep growth scale and ground line; leaves do not inflate to full adult size.
  cell=cell.resize((64,64),Image.Resampling.NEAREST);save('block/'+name,cell)
 else:
  cell=cell.crop(cell.getbbox());cell.thumbnail((116,116),Image.Resampling.NEAREST);out=Image.new('RGBA',(128,128));out.paste(cell,((128-cell.width)//2,(128-cell.height)//2));save('item/'+name,out);names.append(name)
for name in names:
 asset('models/item/'+name+'.json',{'parent':'minecraft:item/generated','textures':{'layer0':'techmod:item/'+name}});asset('items/'+name+'.json',{'model':{'type':'minecraft:model','model':'techmod:item/'+name}})
for stage in range(4):asset(f'models/block/dandelion_stage_{stage}.json',{'parent':'minecraft:block/cross','textures':{'cross':f'techmod:block/dandelion_stage_{stage}'}})
asset('blockstates/rubber_dandelion.json',{'variants':{f'age={i}':{'model':f'techmod:block/dandelion_stage_{min(3,i//2)}'}for i in range(8)}})
# Ore stone backgrounds come from Minecraft itself, so they match vanilla resource colors exactly.
import os
cache_roots=[R.parent/'.gradle',Path(os.environ.get('GRADLE_USER_HOME',Path.home()/'.gradle'))]
vanilla=next((p/'caches/fabric-loom/26.3/minecraft-client.jar' for p in cache_roots if (p/'caches/fabric-loom/26.3/minecraft-client.jar').exists()),None)
if vanilla is None:raise SystemExit('Minecraft 26.3 asset cache missing; run Gradle with Fabric Loom first.')
z=zipfile.ZipFile(vanilla)
from io import BytesIO
for name,base in [('tin_ore','stone'),('deepslate_tin_ore','deepslate')]:
 im=Image.open(BytesIO(z.read(f'assets/minecraft/textures/block/{base}.png'))).convert('RGB');d=ImageDraw.Draw(im)
 # Small asymmetric deposits, separate from iron's tan and diamond's saturated cyan.
 for x,y in [(3,3),(10,2),(7,7),(2,11),(12,12)]:
  d.rectangle((x-1,y,x+2,y+1),fill=(64,72,79));d.rectangle((x,y-1,x+1,y+1),fill=(145,159,174));d.point((x,y-1),fill=(209,219,226));d.point((x+2,y),fill=(171,187,199))
 save('block/'+name,im)
for name in ['tin_ore','deepslate_tin_ore','tin_block']:
 asset('models/block/'+name+'.json',{'parent':'minecraft:block/cube_all','textures':{'all':'techmod:block/'+name}});asset('blockstates/'+name+'.json',{'variants':{'':{'model':'techmod:block/'+name}}});asset('items/'+name+'.json',{'model':{'type':'minecraft:model','model':'techmod:block/'+name}})
# Vanilla loot dialect changed in 26.3: entry condition/modifier are singular fields.
for name in ['tin_ore','deepslate_tin_ore']:
 loot=json.loads(z.read('data/minecraft/loot_table/blocks/iron_ore.json'));loot['random_sequence']='techmod:blocks/'+name
 children=loot['pools'][0]['entries'][0]['children'];children[0]['name']='techmod:'+name;children[1]['name']='techmod:raw_tin';write(D/f'techmod/loot_table/blocks/{name}.json',loot)
write(D/'techmod/loot_table/blocks/tin_block.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'techmod:tin_block','condition':{'type':'minecraft:survives_explosion'}}]}]})
# Crop intentionally returns nothing before maturity, including seeds.
write(D/'techmod/loot_table/blocks/rubber_dandelion.json',{'type':'minecraft:block','pools':[{'rolls':1,'condition':{'type':'minecraft:match_block','blocks':'techmod:rubber_dandelion','state':{'age':'7'}},'entries':[{'type':'minecraft:item','name':'techmod:dandelion_roots'}]},{'rolls':1,'condition':{'type':'minecraft:match_block','blocks':'techmod:rubber_dandelion','state':{'age':'7'}},'entries':[{'type':'minecraft:item','name':'techmod:dandelion_seeds'}]},{'rolls':1,'condition':{'type':'minecraft:all_of','terms':[{'type':'minecraft:match_block','blocks':'techmod:rubber_dandelion','state':{'age':'7'}},{'type':'minecraft:random_chance','chance':.6}]},'entries':[{'type':'minecraft:item','name':'techmod:dandelion_seeds'}]}]})
# Generation is data-driven and only affects newly generated chunks.
write(D/'techmod/worldgen/feature/tin_ore.json',{'type':'minecraft:ore','size':7,'discard_chance_on_air_exposure':0,'targets':[{'state':'techmod:tin_ore','target':{'predicate_type':'minecraft:tag_match','tag':'minecraft:stone_ore_replaceables'}},{'state':'techmod:deepslate_tin_ore','target':{'predicate_type':'minecraft:tag_match','tag':'minecraft:deepslate_ore_replaceables'}}]})
write(D/'techmod/worldgen/placed_feature/tin_ore.json',{'feature':'techmod:tin_ore','placement':[{'type':'minecraft:count','count':8},{'type':'minecraft:in_square'},{'type':'minecraft:height_range','height':{'type':'minecraft:trapezoid','min_inclusive':{'absolute':-32},'max_inclusive':{'absolute':80}}},{'type':'minecraft:biome'}]})
write(D/'techmod/worldgen/feature/dandelion_patch.json',{'type':'techmod:dandelion_patch'})
write(D/'techmod/worldgen/placed_feature/dandelion_patch.json',{'feature':'techmod:dandelion_patch','placement':[{'type':'minecraft:rarity_filter','chance':16},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'MOTION_BLOCKING_NO_LEAVES'},{'type':'minecraft:biome'}]})
def recipe(name,obj):write(D/f'techmod/recipe/{name}.json',obj)
recipe('manual_processing',{'type':'techmod:manual_processing'})
recipe('hammer',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':['ISI',' S ',' S '],'key':{'I':'minecraft:iron_ingot','S':'minecraft:stick'},'result':{'id':'techmod:hammer'}})
# Temporary practical recipe for the manual cutters; other machine recipes remain deferred.
recipe('cutters',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':['I I',' I ','S S'],'key':{'I':'minecraft:iron_ingot','S':'minecraft:stick'},'result':{'id':'techmod:cutters'}})
recipe('insulated_copper_cable',{'type':'minecraft:crafting_shaped','category':'misc','pattern':[' W ','WRW',' W '],'key':{'W':'techmod:copper_wire','R':'techmod:rubber'},'result':{'id':'techmod:copper_cable','count':4}})
for source,name,target in [('raw_tin','tin_ingot_from_raw_tin','tin_ingot'),('tin_ore','tin_ingot_from_ore','tin_ingot'),('deepslate_tin_ore','tin_ingot_from_deepslate','tin_ingot'),('crushed_tin','tin_ingot_from_crushed_tin','tin_ingot'),('caoutchouc','rubber','rubber')]:
 for kind,ticks in [('smelting',200),('blasting',100)] if source!='caoutchouc' else [('smelting',200)]:recipe(name+('_blasting'if kind=='blasting'else''),{'type':'minecraft:'+kind,'ingredient':'techmod:'+source,'result':{'id':'techmod:'+target},'experience':.1 if source=='caoutchouc'else .7,'cookingtime':ticks})
recipe('tin_block',{'type':'minecraft:crafting_shaped','category':'building','pattern':['###']*3,'key':{'#':'techmod:tin_ingot'},'result':{'id':'techmod:tin_block'}})
recipe('tin_ingots_from_block',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':['techmod:tin_block'],'result':{'id':'techmod:tin_ingot','count':9}})
for tag,values in [('mineable/axe',['reinforced_chest']),('mineable/pickaxe',['tin_ore','deepslate_tin_ore','tin_block','metal_cutter']),('needs_stone_tool',['tin_ore','deepslate_tin_ore','tin_block'])]:
 p=D/f'minecraft/tags/block/{tag}.json';obj=json.loads(p.read_text())if p.exists()else{'replace':False,'values':[]};obj['values']=list(dict.fromkeys(obj['values']+['techmod:'+v for v in values]));write(p,obj)
for lang in ['ru_ru','en_us']:
 p=A/f'lang/{lang}.json';obj=json.loads(p.read_text());ru=lang=='ru_ru'
 items={'hammer':('Молот','Sledgehammer'),'cutters':('Кусачки','Wire Cutters'),'copper_wire':('Медная проволока','Bare Copper Wire'),'dandelion_roots':('Корни каучукового одуванчика','Rubber Dandelion Roots'),'dandelion_seeds':('Семена каучукового одуванчика','Rubber Dandelion Seeds'),'caoutchouc':('Каучук','Caoutchouc'),'rubber':('Резина','Rubber'),'raw_tin':('Рудное олово','Raw Tin'),'tin_ingot':('Оловянный слиток','Tin Ingot'),'crushed_tin':('Дроблёная оловянная руда','Crushed Tin Ore'),'tin_plate':('Оловянная пластина','Tin Plate')}
 blocks={'tin_ore':('Оловянная руда','Tin Ore'),'deepslate_tin_ore':('Глубинная оловянная руда','Deepslate Tin Ore'),'tin_block':('Оловянный блок','Tin Block'),'rubber_dandelion':('Каучуковый одуванчик','Rubber Dandelion'),'metal_cutter':('Резчик металла','Metal Cutter'),'reinforced_chest':('Усиленный сундук','Reinforced Chest')}
 strings={'key.techmod.conveyor_slope':('Уклон конвейера','Conveyor slope'),'key.category.techmod.controls':('Контур','Kontur'),'tooltip.techmod.conveyor_mode':('G: ровно / подъём / спуск (клавишу можно изменить)','G: level / up / down (rebindable)'),'tooltip.techmod.dandelion_alt':('Кок-сагыз','Kok-saghyz'),'state.techmod.cutting':('Нарезка металла','Cutting metal'),'screen.techmod.sample':('Образец','Sample'),'tooltip.techmod.sample':('Образец не расходуется. ПКМ — очистить.','Sample is not consumed. Right-click to clear.'),'subtitles.techmod.metal_cutter_loop':('Работает резчик металла','Metal cutter running'),'screen.techmod.side_front':('Фасад','Front'),'screen.techmod.side_back':('Сзади','Rear'),'screen.techmod.side_left':('Слева','Left'),'screen.techmod.side_right':('Справа','Right'),'screen.techmod.side_top':('Сверху','Top'),'screen.techmod.side_bottom':('Снизу','Below'),'screen.techmod.side_hint':('Стороны относительно фасада','Sides relative to front'),'screen.techmod.port_short.0':('Нет','Off'),'screen.techmod.port_short.1':('Вход','Input'),'screen.techmod.port_short.2':('Выход','Output')}
 for prefix,values in [('item.techmod.',items),('block.techmod.',blocks),('',strings)]:
  for key,pair in values.items():obj[prefix+key]=pair[0 if ru else 1]
 write(p,obj)
print('Material sprites, crop stages, loot, recipes, generation and translations generated.')

for lang in ['ru_ru','en_us']:
 p=A/f'lang/{lang}.json';obj=json.loads(p.read_text());ru=lang=='ru_ru'
 hints={'hammer':('Слиток → пластина. 2 корня → каучук. 32 операции.','Ingot → plate. 2 roots → caoutchouc. 32 operations.'),'cutters':('Медная пластина → 4 проволоки. 64 операции.','Copper plate → 4 bare wires. 64 operations.'),'roots_processing':('Пресс: 1 корень → каучук. В крафте с молотом нужны 2 корня.','Press: 1 root → caoutchouc. Manual crafting needs 2 roots and a hammer.'),'caoutchouc_processing':('Переплавьте в печи, чтобы получить резину.','Smelt in a furnace to obtain rubber.'),'seeds_growing':('Посев на пашне. Незрелое растение ничего не даёт.','Plant on farmland. Immature plants drop nothing.')}
 for key,pair in hints.items():obj['tooltip.techmod.'+key]=pair[0 if ru else 1]
 write(p,obj)
for kind,values in [('ingots/tin',['tin_ingot']),('raw_materials/tin',['raw_tin']),('plates/tin',['tin_plate']),('storage_blocks/tin',['tin_block']),('ores/tin',['tin_ore','deepslate_tin_ore'])]:
 for registry in ['item','block'] if kind.startswith(('ores/','storage_blocks/')) else ['item']:
  write(D/f'c/tags/{registry}/{kind}.json',{'replace':False,'values':['techmod:'+v for v in values]})
# Recipe-book discovery for the explicitly approved early processing recipes.
for name,item in [('hammer','minecraft:iron_ingot'),('cutters','minecraft:iron_ingot'),('insulated_copper_cable','techmod:rubber'),('rubber','techmod:caoutchouc'),('tin_block','techmod:tin_ingot'),('tin_ingots_from_block','techmod:tin_block')]:
 write(D/f'techmod/advancement/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_item':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':item}]}}},'requirements':[['has_item']],'rewards':{'recipes':['techmod:'+name]}})
