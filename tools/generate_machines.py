"""Reproducible 64px machine materials and battery models using the approved artwork."""
from pathlib import Path
import json, random
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/techmod'
# Editable charge window, in Minecraft model coordinates (0..16), independent of the casing image.
BATTERY_GAUGE=(3.0,5.25,13.0,6.75)
def write(path,obj):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
def save(im,name):
 p=ROOT/f'textures/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
with Image.open(Path(__file__).parent/'art/battery_source.png') as im:save(im.convert('RGBA').resize((128,128),Image.Resampling.NEAREST),'item/battery')
for name,color in [('plate',(43,49,58)),('steel',(100,113,127)),('inside',(23,28,34)),('heater_off',(48,43,38)),('heater_on',(153,62,17)),('copper',(157,87,41))]:
 rng=random.Random(199+sum(map(ord,name)));im=Image.new('RGB',(64,64));d=ImageDraw.Draw(im)
 for y in range(64):
  for x in range(64):
   n=rng.randrange(-3,4);im.putpixel((x,y),tuple(max(0,min(255,c+n))for c in color))
 if name=='plate':
  d.rectangle((1,1,62,62),outline=(25,31,38),width=2);d.line((4,4,59,4),fill=(71,80,91),width=2)
  for x,y in [(6,6),(56,6),(6,56),(56,56)]:d.rectangle((x-1,y-1,x+1,y+1),fill=(106,113,119));d.point((x,y),fill=(33,36,40))
 elif name.startswith('heater'):
  for y in range(8,62,10):d.rectangle((7,y,56,y+3),fill=(245,130,25)if name.endswith('on')else(76,68,54))
 elif name=='steel':
  for x in (0,1,62,63):d.line((x,0,x,63),fill=(44,54,66))
  d.line((3,2,60,2),fill=(170,180,185),width=2)
 save(im,'block/machine_'+name)
for state,color in [(0,(32,38,32)),(1,(119,199,49)),(2,(235,165,31))]:
 im=Image.new('RGB',(64,64),(24,29,34));d=ImageDraw.Draw(im);d.rectangle((3,3,60,60),fill=(8,12,16));d.rectangle((7,7,56,56),fill=(117,128,135));d.rectangle((11,11,52,52),fill=color);d.rectangle((16,15,47,25),fill=tuple(min(255,c+45)for c in color));d.line((17,15,46,15),fill=(228,248,190)if state==1 else(255,226,126)if state==2 else(51,61,51),width=3);save(im,f'block/machine_lamp_{state}')
# Paired arrows point relative to the connector, not relative to the neighboring face.
for direction in ('input','output'):
 im=Image.open(ROOT/'textures/block/machine_plate.png').copy();d=ImageDraw.Draw(im)
 d.rectangle((21,21,42,42),fill=(17,20,24),outline=(180,103,47),width=3)
 d.rectangle((26,26,37,37),fill=(85,47,26));d.line((29,27,29,36),fill=(236,160,77),width=3);d.line((35,27,35,36),fill=(236,160,77),width=2)
 left=[(5,29),(12,29),(12,25),(18,32),(12,39),(12,35),(5,35)]
 if direction=='output':left=[(23-x,y)for x,y in left]
 d.polygon(left,fill=(224,150,51));d.polygon([(63-x,y)for x,y in left],fill=(224,150,51))
 save(im,'block/machine_'+direction)
for name,color in [('low',(220,76,38)),('mid',(224,165,42)),('high',(127,208,48))]:save(Image.new('RGBA',(16,16),color+(255,)),'item/battery_bar_'+name)
display={'gui':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]},'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]},'ground':{'rotation':[0,0,0],'translation':[0,3,0],'scale':[.5,.5,.5]},'firstperson_righthand':{'rotation':[0,-90,25],'translation':[1,3,1],'scale':[.65,.65,.65]},'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[0,2,1],'scale':[.5,.5,.5]}}
entries=[]
for pct in range(101):
 faces={'north':{'uv':[0,0,16,16],'texture':'#base'},'south':{'uv':[0,0,16,16],'texture':'#base'}}
 elements=[{'from':[0,0,8],'to':[16,16,8],'faces':faces}]
 if pct:
  left,bottom,right,top=BATTERY_GAUGE
  elements.append({'from':[left,bottom,7.98],'to':[left+(right-left)*pct/100,top,8.02],'shade':False,'faces':{'south':{'uv':[0,0,16,16],'texture':'#bar'},'north':{'uv':[0,0,16,16],'texture':'#bar'}}})
 write(f'models/item/battery_{pct}.json',{'textures':{'base':'techmod:item/battery','particle':'techmod:item/battery','bar':'techmod:item/battery_bar_'+('low'if pct<=20 else'mid'if pct<=50 else'high')},'elements':elements,'display':display})
 entries.append({'threshold':pct,'model':{'type':'minecraft:model','model':f'techmod:item/battery_{pct}'}})
write('items/battery.json',{'model':{'type':'minecraft:range_dispatch','property':'minecraft:custom_model_data','index':0,'fallback':{'type':'minecraft:model','model':'techmod:item/battery_0'},'entries':entries}})

def box(a,b,texture,faces=None):
 uv={'north':[16-b[0],16-b[1],16-a[0],16-a[1]],'south':[a[0],16-b[1],b[0],16-a[1]],'west':[a[2],16-b[1],b[2],16-a[1]],'east':[16-b[2],16-b[1],16-a[2],16-a[1]],'up':[a[0],a[2],b[0],b[2]],'down':[a[0],16-b[2],b[0],16-a[2]]}
 return {'from':a,'to':b,'faces':{f:{'texture':'#'+texture,'uv':[max(0,min(16,v))for v in uv[f]]}for f in(faces or ['north','south','east','west','up','down'])}}
tex={k:'techmod:block/machine_'+k for k in ['plate','steel','inside','heater_off','heater_on','input','output','copper']}
def model(kind,upper,active,status):
 textures=dict(tex);textures['lamp']=f'techmod:block/machine_lamp_{status}';textures['heater']=tex['heater_on'if active else'heater_off'];textures['particle']=tex['plate']
 e=[]
 if kind=='battery_cabinet':
  e +=[box([0,0,0],[1,16,16],'steel'),box([15,0,0],[16,16,16],'steel'),box([7.5,0,0],[8.5,16,16],'steel'),box([1,1,5],[15,15,11],'inside'),box([0,0,0],[16,1,16],'steel'),box([0,15,0],[16,16,16],'steel')]
  # Recessed backing, horizontal shelves and copper contacts for four bays per face.
  e += [box([1,7.5,0],[15,8.5,16],'steel')]
  for z in [1,14.5]:
   for x in [1.5,9]:
    for y in [1.5,9]:
     e.append(box([x,y,z],[x+5.5,y+.4,z+.5],'copper'))
  e +=[box([-.02,1,1],[0,15,15],'plate'if upper else'input', ['west']),box([16,1,1],[16.02,15,15],'plate'if upper else'output',['east'])]
 else:
  e +=[box([0,0,0],[16,2,16],'steel'),*( [box([0,14,0],[16,16,3],'steel'),box([0,14,13],[16,16,16],'steel'),box([0,14,3],[3,16,13],'steel'),box([13,14,3],[16,16,13],'steel')] if kind=='crusher' else [box([0,14,0],[16,16,16],'steel')]),box([0,2,0],[2,14,16],'plate'),box([14,2,0],[16,14,16],'plate'),box([2,2,14],[14,14,16],'output'if kind=='coal_generator'else'input')]
  # Recessed open chamber, front door frame; actual input item is rendered behind the opening.
  e +=[box([2,2,1],[14,4,3],'steel'),box([2,11,1],[14,14,3],'plate'),box([2,4,1],[3,11,3],'steel'),box([13,4,1],[14,11,3],'steel'),box([3,4,11],[13,11,12],'heater'),box([3,3,3],[13,4,12],'inside')]
  e +=[box([0,0,-.06],[1,16,.1],'steel'),box([15,0,-.06],[16,16,.1],'steel'),box([3,11,-.02],[13,12,1],'steel'),box([3,3,-.02],[13,4,1],'steel'),box([5,.5,-.05],[11,1.2,.05],'copper')]
  e +=[box([3,11.5,.78],[6,14,.95],'lamp',['north']),box([13,6,.4],[14,10,1.2],'copper')]
  if kind=='coal_generator':
   for x in [4,6.5,9,11.5]:e.append(box([x,4,1],[x+.55,8,2],'steel'))
   if active:
    textures['fire']='minecraft:block/fire_0';e.append(box([3,4,9],[13,10,9.01],'fire',['north']))
  if kind=='crusher':
   # Feed hopper on top, inset crushing rollers and bottom collection drawer.
   for a,b in [([3,14,3],[5,16,13]),([11,14,3],[13,16,13]),([5,14,3],[11,16,5]),([5,14,11],[11,16,13])]:e.append(box(a,b,'steel'))
   e += [box([3,2.15,.2],[13,2.9,1],'plate'),box([6,2.35,-.1],[10,2.7,.4],'copper')]
   textures['heater']=tex['inside']
   e += [box([-.03,5,1],[0,6,15],'copper',['west']),box([16,5,1],[16.03,6,15],'copper',['east'])]
  # The lamp uses its whole texture; world-space UV clipping made the old indicator tiny.
  lamp=next(v for v in e if any(f['texture']=='#lamp' for f in v['faces'].values()))
  for face in lamp['faces'].values():face['uv']=[0,0,16,16]
  if status:lamp['light_emission']=10
  for x in [3,6,9,12]:e.append(box([x,15.9,5],[x+1,16.05,11],'inside'))
 return {'parent':'minecraft:block/block','textures':textures,'elements':e,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]}}}
for kind in ['battery_cabinet','coal_generator','electric_furnace','crusher']:
 variants={}
 for facing,yaw in [('north',0),('east',90),('south',180),('west',270)]:
  for upper in [False,True]:
   for active in [False,True]:
    for status in range(3):
     suffix=f'{int(upper)}_{int(active)}_{status}';name=f'{kind}_{suffix}'
     write('models/block/'+name+'.json',model(kind,upper,active,status))
     variants[f'facing={facing},upper={str(upper).lower()},active={str(active).lower()},status={status}']={'model':'techmod:block/'+name,'y':yaw}
 write('blockstates/'+kind+'.json',{'variants':variants})
 itemmodel=f'techmod:block/{kind}_0_0_0'
 if kind=='battery_cabinet':write('models/item/battery_cabinet.json',model(kind,False,False,0))
 if kind=='crusher':
  item=model(kind,False,False,0)
  for x in [4,9]:
   item['elements'].append(box([x,4.15,2],[x+3,10.85,5],'steel'))
   for y in [4,6,8,10]:item['elements'].append(box([x-.5,y,1.5],[x+3.5,y+1,5.5],'steel'))
  write('models/item/crusher.json',item);itemmodel='techmod:item/crusher'
 write('items/'+kind+'.json',{'model':{'type':'minecraft:model','model':itemmodel}})
# Append translations without touching existing cable identifiers.
for lang in ['ru_ru','en_us']:
 p=ROOT/f'lang/{lang}.json';d=json.loads(p.read_text());ru=lang=='ru_ru'
 names={'battery':('Аккумулятор','Battery'),'battery_cabinet':('Аккумуляторный шкаф АШ-8','AS-8 Battery Cabinet'),'coal_generator':('Угольный генератор','Coal Generator'),'electric_furnace':('Электропечь','Electric Furnace'),'crusher':('Дробитель','Crusher'),'solar_panel':('Солнечная панель','Solar Panel')}
 for key,pair in names.items():d[('item.'if key=='battery'else'block.')+'techmod.'+key]=pair[0 if ru else 1]
 for metal,pair in {'iron':('железная','Iron'),'copper':('медная','Copper'),'gold':('золотая','Gold')}.items():
  d['item.techmod.crushed_'+metal]=('Дроблёная '+pair[0]+' руда') if ru else ('Crushed '+pair[1]+' Ore')
 d['itemGroup.techmod']='Контур'if ru else'Kontur'
 strings={
 'screen.techmod.solar_rate':('Выработка: %s ТЕ/с','Generation: %s TE/s'),
 'screen.techmod.solar_efficiency':('Эффективность: %s%%','Efficiency: %s%%'),
 'screen.techmod.solar_output':('Выход: %s ТЕ/с','Output: %s TE/s'),
 'state.techmod.no_sun':('Здесь нет солнечного света','No sunlight in this dimension'),
 'state.techmod.shaded':('Панель затенена','Panel is shaded'),
 'state.techmod.night':('Ночь','Night'),
 'state.techmod.solar_generating':('Солнечная генерация','Solar generation'),
 'options.techmod.sounds':('Звуки Контура','Kontur sounds'),
 'options.techmod.machine_volume':('Громкость механизмов: %s%%','Machine volume: %s%%'),
 'options.techmod.sound_hint':('Звуки затихают к 6 блокам; стены их приглушают.','Sounds fade within 6 blocks; walls muffle them.'),
 'subtitles.techmod.coal_generator_loop':('Работает угольный генератор','Coal generator running'),
 'subtitles.techmod.electric_furnace_loop':('Гудит электропечь','Electric furnace humming'),
 'subtitles.techmod.crusher_loop':('Дробится руда','Ore being crushed'),
 'tooltip.techmod.hold_shift':('Зажмите Shift для подробностей','Hold Shift for details'),
 'tooltip.techmod.limits':('Макс. вход: %s · выход: %s ТЕ/с','Max input: %s · output: %s TE/s'),
 'tooltip.techmod.current_limit':('Текущий предел: %s ТЕ/с','Current limit: %s TE/s'),
 'state.techmod.crushing':('Дробление','Crushing'),
 'tooltip.techmod.charge':('Заряд: %s / %s ТЕ','Charge: %s / %s TE'),
 'tooltip.techmod.battery_rate':('Мощность аккумулятора: до %s ТЕ/с','Battery output: up to %s TE/s'),
 'tooltip.techmod.installed':('Установлено аккумуляторов: %s','Installed batteries: %s'),
 'tooltip.techmod.active_fuel':('Осталось горения: %s с','Remaining fuel burn: %s s'),
 'screen.techmod.front':('Передняя','Front'),'screen.techmod.back':('Задняя','Rear'),
 'screen.techmod.fuel':('Уголь','Coal'),'screen.techmod.input':('Сырьё','Input'),
 'screen.techmod.battery':('Питание','Battery'),'screen.techmod.charge_slot':('Зарядка','Charge'),
 'screen.techmod.energy':('Запас: %s / %s ТЕ','Energy: %s / %s TE'),
 'screen.techmod.flow':('Вход %s · Выход %s ТЕ/с','In %s · Out %s TE/s'),
 'message.techmod.not_enough_wire':('Недостаточно проводов для этой длины.','Not enough cable items for this span.'),
 'state.techmod.generating':('Выработка энергии','Generating power'),'state.techmod.full':('Нет свободного места для энергии','Energy storage full'),
 'state.techmod.no_fuel':('Нет топлива','No fuel'),'state.techmod.no_batteries':('Вставьте аккумуляторы','Insert batteries'),
 'state.techmod.storage':('Хранилище готово','Storage ready'),'state.techmod.no_input':('Нет сырья','No input'),
 'state.techmod.no_recipe_or_full':('Нет рецепта / выход заполнен','No recipe / output full'),
 'state.techmod.smelting':('Переплавка','Smelting'),'state.techmod.no_power':('Недостаточно энергии','Insufficient power')}
 for key,pair in strings.items():d[key]=pair[0 if ru else 1]
 write(f'lang/{lang}.json',d)
