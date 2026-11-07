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
 im=Image.new('RGB',(64,64),(24,29,34));d=ImageDraw.Draw(im);d.rectangle((8,8,55,55),fill=(92,104,111));d.rectangle((14,14,49,49),fill=color);d.line((17,16,45,16),fill=tuple(min(255,c+28)for c in color),width=3);save(im,f'block/machine_lamp_{state}')
# Paired arrows point relative to the connector, not relative to the neighboring face.
for direction in ('input','output'):
 im=Image.open(ROOT/'textures/block/machine_plate.png').copy();d=ImageDraw.Draw(im)
 d.rectangle((21,18,42,45),fill=(17,20,24),outline=(180,103,47),width=3)
 d.rectangle((26,23,37,40),fill=(85,47,26));d.line((29,25,29,37),fill=(236,160,77),width=3);d.line((35,25,35,37),fill=(236,160,77),width=2)
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
  e +=[box([0,0,0],[1,16,16],'steel'),box([15,0,0],[16,16,16],'steel'),box([7.5,0,0],[8.5,16,16],'steel'),box([0,0,7.5],[16,16,8.5],'inside'),box([0,0,0],[16,1,16],'steel'),box([0,15,0],[16,16,16],'steel')]
  for x in [2,6,10,14]: e.append(box([x,1.5,.5],[x+.35,2,7.5],'copper'))
  e +=[box([1,2,2],[7,2.5,14],'inside'),box([9,2,2],[15,2.5,14],'inside')]
  e +=[box([-.02,1,1],[0,15,15],'plate'if upper else'input', ['west']),box([16,1,1],[16.02,15,15],'plate'if upper else'output',['east'])]
 else:
  e +=[box([0,0,0],[16,2,16],'steel'),box([0,14,0],[16,16,16],'steel'),box([0,2,0],[2,14,16],'plate'),box([14,2,0],[16,14,16],'plate'),box([2,2,14],[14,14,16],'output'if kind=='coal_generator'else'input')]
  # Recessed open chamber, front door frame; actual input item is rendered behind the opening.
  e +=[box([2,2,1],[14,4,3],'steel'),box([2,11,1],[14,14,3],'plate'),box([2,4,1],[3,11,3],'steel'),box([13,4,1],[14,11,3],'steel'),box([3,4,11],[13,11,12],'heater'),box([3,3,3],[13,4,12],'inside')]
  e +=[box([0,0,-.06],[1,16,.1],'steel'),box([15,0,-.06],[16,16,.1],'steel'),box([3,11,-.02],[13,12,1],'steel'),box([3,3,-.02],[13,4,1],'steel'),box([5,.5,-.05],[11,1.2,.05],'copper')]
  e +=[box([3,12,.9],[5,14,1],'lamp',['north']),box([13,6,.4],[14,10,1.2],'copper')]
  if kind=='coal_generator':
   for x in [4,6.5,9,11.5]:e.append(box([x,4,1],[x+.55,8,2],'steel'))
   if active:
    textures['fire']='minecraft:block/fire_0';e.append(box([3,4,9],[13,10,9.01],'fire',['north']))
  for x in [3,6,9,12]:e.append(box([x,15.9,5],[x+1,16.05,11],'inside'))
 return {'parent':'minecraft:block/block','textures':textures,'elements':e,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.625,.625,.625]}}}
for kind in ['battery_cabinet','coal_generator','electric_furnace']:
 variants={}
 for facing,yaw in [('north',0),('east',90),('south',180),('west',270)]:
  for upper in [False,True]:
   for active in [False,True]:
    for status in range(3):
     suffix=f'{int(upper)}_{int(active)}_{status}';name=f'{kind}_{suffix}'
     write('models/block/'+name+'.json',model(kind,upper,active,status))
     variants[f'facing={facing},upper={str(upper).lower()},active={str(active).lower()},status={status}']={'model':'techmod:block/'+name,'y':yaw}
 write('blockstates/'+kind+'.json',{'variants':variants})
 if kind=='battery_cabinet':
  lo=model(kind,False,False,0);hi=model(kind,True,False,0)
  for part in hi['elements']:
   part['from'][1]+=16;part['to'][1]+=16
  lo['elements']+=hi['elements'];lo['display']['gui']={'rotation':[20,225,0],'translation':[0,-4,0],'scale':[.38,.38,.38]}
  for context in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand','ground','fixed']:
   lo['display'][context]={'rotation':[0,45,0],'translation':[0,0,0],'scale':[.25,.25,.25]}
  write('models/item/'+kind+'.json',lo);itemmodel='techmod:item/'+kind
 else:itemmodel=f'techmod:block/{kind}_0_0_0'
 write('items/'+kind+'.json',{'model':{'type':'minecraft:model','model':itemmodel}})
# Append translations without touching existing cable identifiers.
for lang in ['ru_ru','en_us']:
 p=ROOT/f'lang/{lang}.json';d=json.loads(p.read_text());ru=lang=='ru_ru'
 names={'battery':('Аккумулятор','Battery'),'battery_cabinet':('Аккумуляторный шкаф АШ-8','AS-8 Battery Cabinet'),'coal_generator':('Угольный генератор','Coal Generator'),'electric_furnace':('Электропечь','Electric Furnace')}
 for key,pair in names.items():d[('item.'if key=='battery'else'block.')+'techmod.'+key]=pair[0 if ru else 1]
 d['itemGroup.techmod']='Контур'if ru else'Kontur'
 strings={
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
