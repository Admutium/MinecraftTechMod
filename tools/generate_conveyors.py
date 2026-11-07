"""Shared steel plate artwork, conveyor shapes and compact drive/manipulator models."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,colorsys,math
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/techmod'
def write(path,obj):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
def save(name,im):im.save(root/f'textures/{name}.png')
source=Image.open(Path(__file__).parent/'art/plate_source.png').convert('RGBA');source.putalpha(source.getchannel('A').point(lambda a:255 if a>=128 else 0));source=source.crop(source.getbbox());source.thumbnail((120,120),Image.Resampling.NEAREST)
for metal,hue,sat in [('iron',.6,.08),('copper',.055,.66),('gold',.13,.66)]:
 im=Image.new('RGBA',(128,128));im.paste(source,((128-source.width)//2,(128-source.height)//2))
 for y in range(128):
  for x in range(128):
   r,g,b,a=im.getpixel((x,y));v=max(r,g,b)/255;rr,gg,bb=colorsys.hsv_to_rgb(hue,sat,v);im.putpixel((x,y),(round(rr*255),round(gg*255),round(bb*255),a))
 name=metal+'_plate';save('item/'+name,im);write('models/item/'+name+'.json',{'parent':'minecraft:item/generated','textures':{'layer0':'techmod:item/'+name}});write('items/'+name+'.json',{'model':{'type':'minecraft:model','model':'techmod:item/'+name}})
for turn in [False,True]:
 frames=Image.new('RGB',(64,256))
 for frame in range(4):
  im=Image.new('RGB',(64,64),(25,29,34));d=ImageDraw.Draw(im)
  if turn:
   for radius in range(12,120,12):d.arc((-radius,-radius,radius,radius),0,90,fill=(60,65,70),width=3)
   for angle in range(0,90,20):
    a=math.radians((angle-frame*5)%90);x,y=round(32*math.cos(a)),round(32*math.sin(a));d.rectangle((x-1,y-1,x+1,y+1),fill=(97,104,109))
  else:
   for y in range(-8,64,8):d.rectangle((3,y-frame*2,60,y-frame*2+2),fill=(48,53,58))
  d.line((5,0,5,63),fill=(13,16,19));d.line((58,0,58,63),fill=(13,16,19))
  frames.paste(im,(0,frame*64))
 name='conveyor_corner'if turn else'conveyor_belt';save('block/'+name+'_on',frames);save('block/'+name+'_off',frames.crop((0,0,64,64)))
 write('textures/block/'+name+'_on.png.mcmeta',{'animation':{'frametime':3,'interpolate':False}})
def box(a,b,tex,faces=None):return {'from':a,'to':b,'faces':{f:{'texture':'#'+tex,'uv':[0,0,16,16]}for f in(faces or ['north','south','east','west','up','down'])}}
textures={'steel':'techmod:block/machine_steel','plate':'techmod:block/machine_plate','copper':'techmod:block/machine_copper','input':'techmod:block/machine_input','lamp':'techmod:block/machine_lamp_1','particle':'techmod:block/machine_steel'}
write('models/block/conveyor_empty.json',{'textures':textures,'elements':[]})
for kind in ['conveyor','manipulator','splitter']:
 variants={}
 for slope in [-1,0,1]:
  for turn in [-1,0,1]:
   for active in [False,True]:
    suffix=f'{slope}_{turn}_{int(active)}';name=kind+'_'+suffix;tex=dict(textures);tex['belt']='techmod:block/'+('conveyor_corner'if turn else'conveyor_belt')+('_on'if active else'_off')
    e=[box([1,1,0],[15,3,16],'steel'),box([2,3,0],[14,4,16],'belt')]
    if turn>0:e[1]['faces']['up']['uv']=[16,0,0,16]
    if kind=='conveyor':
     input_side='south'if turn==0 else'east'if turn==1 else'west'
     rails={'north':([0,3,0],[16,5,1]),'south':([0,3,15],[16,5,16]),'west':([0,3,0],[1,5,16]),'east':([15,3,0],[16,5,16])}
     for side,(a,b)in rails.items():
      if side not in ['north',input_side]:e.append(box(a,b,'steel'))
     if slope:
      e=[box([1,1,-6.627],[15,3,16],'steel'),box([2,3,-6.627],[14,4,16],'belt'),box([0,3,-6.627],[1,5,16],'steel'),box([15,3,-6.627],[16,5,16],'steel')]
      for element in e:element['rotation']={'origin':[8,4,16],'axis':'x','angle':45*slope,'rescale':False}
      for z in [-6,0,6,12]:
       cleat=box([2,4,z],[14,5,z+1],'steel');cleat['rotation']={'origin':[8,4,16],'axis':'x','angle':45*slope};e.append(cleat)
    elif kind=='manipulator':
     e=[box([1,0,0],[15,2,16],'plate'),box([0,2,0],[2,5,16],'steel'),box([14,2,0],[16,5,16],'steel'),box([2,2,0],[14,3,16],'steel'),box([1,5,2],[3,8,14],'copper'),box([13,5,2],[15,8,14],'steel')]
    else:e +=[box([0,3,0],[16,6,2],'steel'),box([0,3,14],[2,5,16],'steel'),box([14,3,14],[16,5,16],'steel')]
    write('models/block/'+name+'.json',{'parent':'minecraft:block/block','textures':tex,'elements':e})
    for facing,yaw in [('north',0),('east',90),('south',180),('west',270)]:
     for upper in [False,True]:variants[f'facing={facing},slope={2 if slope==-1 else slope},turn={2 if turn==-1 else turn},active={str(active).lower()},upper={str(upper).lower()}']={'model':'techmod:block/'+('conveyor_empty'if upper else name),'y':yaw}
 write('blockstates/'+kind+'.json',{'variants':variants});write('items/'+kind+'.json',{'model':{'type':'minecraft:model','model':f'techmod:block/{kind}_0_0_0'}})
variants={}
for active in [False,True]:
 for status in range(3):
  tex=dict(textures);tex['lamp']=f'techmod:block/machine_lamp_{status}'
  e=[box([1,0,2],[15,2,16],'steel'),box([3,2,4],[13,12,16],'plate'),box([5,12,6],[11,13,14],'steel'),box([5,2,1],[11,9,5],'steel'),box([7,2,-1],[9,4,2],'copper')]
  # Rear texture uses world-sized UVs so the electrical socket stays at the standard center.
  e.append({'from':[2,2,16],'to':[14,12,16.02],'faces':{'south':{'texture':'#input','uv':[2,4,14,14]}}})
  lamp=box([3,9,.98],[6,11.5,1],'lamp',['north']);lamp['light_emission']=10 if status else 0;e.append(lamp)
  name=f'conveyor_drive_{int(active)}_{status}';write('models/block/'+name+'.json',{'parent':'minecraft:block/block','textures':tex,'elements':e})
  for face,yaw in [('north',0),('east',90),('south',180),('west',270)]:variants[f'facing={face},active={str(active).lower()},status={status}']={'model':'techmod:block/'+name,'y':yaw}
write('blockstates/conveyor_drive.json',{'variants':variants});write('items/conveyor_drive.json',{'model':{'type':'minecraft:model','model':'techmod:block/conveyor_drive_0_0'}})
p=root.parents[1]/'data/minecraft/tags/block/mineable/pickaxe.json';data=json.loads(p.read_text());data['values']=list(dict.fromkeys(data['values']+['techmod:'+s for s in ['conveyor','manipulator','splitter','press','conveyor_drive']]));p.write_text(json.dumps(data,indent=2)+'\n')
for lang in ['ru_ru','en_us']:
 p=root/f'lang/{lang}.json';data=json.loads(p.read_text());ru=lang=='ru_ru'
 names={'conveyor':('Конвейер','Conveyor'),'manipulator':('Манипулятор','Manipulator'),'splitter':('Разделитель','Splitter'),'conveyor_drive':('Электропривод','Conveyor Drive'),'press':('Пресс','Press')}
 for name,pair in names.items():data['block.techmod.'+name]=pair[0 if ru else 1]
 for name,pair in {'iron':('Железная пластина','Iron Plate'),'copper':('Медная пластина','Copper Plate'),'gold':('Золотая пластина','Gold Plate')}.items():data['item.techmod.'+name+'_plate']=pair[0 if ru else 1]
 strings={
 'tooltip.techmod.conveyor_mode':('Shift + ПКМ в воздухе: обычный / подъём / спуск','Shift + right-click in air: level / up / down'),
 'tooltip.techmod.conveyor_rotate':('Shift + ПКМ пустой рукой: повернуть секцию','Shift + right-click with empty hand: rotate section'),
 'tooltip.techmod.filter_sample':('Образец не расходуется. ПКМ очищает ячейку.','Samples are not consumed. Right-click clears the slot.'),
 'screen.techmod.search':('Название / ID','Name / ID'),'screen.techmod.filter_on':('Фильтр: ВКЛ','Filter: ON'),'screen.techmod.filter_off':('Фильтр: ВЫКЛ','Filter: OFF'),
 'screen.techmod.from_belt':('С ленты → в инвентарь','Belt → inventory'),'screen.techmod.to_belt':('Из инвентаря → на ленту','Inventory → belt'),
 'screen.techmod.drive_load':('Нагрузка: %s / 16','Load: %s / 16'),'screen.techmod.ports':('Стороны','Sides'),
 'screen.techmod.redstone.0':('Всегда включён','Always enabled'),'screen.techmod.redstone.1':('Работа при сигнале','Enabled with signal'),'screen.techmod.redstone.2':('Работа без сигнала','Enabled without signal'),
 'screen.techmod.port.0':('Отключено','Disabled'),'screen.techmod.port.1':('Вход сырья','Item input'),'screen.techmod.port.2':('Выход продукции','Product output'),
 'conveyor.techmod.running':('Линия работает','Line running'),'conveyor.techmod.no_drive':('Нет привода: нужен привод в пределах 16 секций','No drive: place a drive within 16 sections'),
 'conveyor.techmod.no_power':('Недостаточно энергии','Insufficient energy'),'conveyor.techmod.redstone':('Остановлено редстоуном','Stopped by redstone'),'conveyor.techmod.blocked':('Выход занят','Output blocked'),
 'conveyor.techmod.mode.0':('Конвейер: обычный участок','Conveyor: level section'),'conveyor.techmod.mode.1':('Конвейер: подъём','Conveyor: ascending'),'conveyor.techmod.mode.-1':('Конвейер: спуск','Conveyor: descending'),
 'state.techmod.pressing':('Прессование','Pressing'),'state.techmod.drive':('Привод конвейера','Conveyor drive'),
 'subtitles.techmod.press_loop':('Работает пресс','Press operating'),'subtitles.techmod.conveyor_drive_loop':('Гудит электропривод','Conveyor drive humming')}
 for key,pair in strings.items():data[key]=pair[0 if ru else 1]
 for i,pair in enumerate([('Низ','Down'),('Верх','Up'),('Север','North'),('Юг','South'),('Запад','West'),('Восток','East')]):data['side.techmod.'+str(i)]=pair[0 if ru else 1]
 write('lang/'+lang+'.json',data)
