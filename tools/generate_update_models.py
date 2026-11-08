"""Chest case/lid, cutter and directional splitter models using approved industrial materials."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,copy
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/techmod'
def write(path,obj):(A/path).write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n')
def box(a,b,tex):return {'from':a,'to':b,'faces':{f:{'texture':'#'+tex,'uv':[0,0,16,16]}for f in ['north','south','west','east','up','down']}}
# Flat face textures from the approved wood-and-metal chest concept.
source=Image.open(R/'tools/art/chest_source.png')
for i,name in enumerate(['front','side','top']):source.crop((i*source.width//3,0,(i+1)*source.width//3,source.height)).resize((64,64),Image.Resampling.NEAREST).save(A/f'textures/block/chest_{name}.png')
tex={k:f'techmod:block/chest_{k}'for k in ['front','side','top']};tex['particle']=tex['side']
variants={}
for opened in [False,True]:
 base=box([1,0,1],[15,10,15],'side');lid=box([1,10,1],[15,15,15],'side')
 for cube,y1,y2 in [(base,16/3,16),(lid,0,16/3)]:
  for side in ['north','south','west','east']:cube['faces'][side]['uv']=[0,y1,16,y2]
  cube['faces']['north']['texture']='#front';cube['faces']['up']['texture']='#top';cube['faces']['down']['texture']='#top'
 if opened:lid['rotation']={'origin':[8,10,15],'axis':'x','angle':45}
 name='reinforced_chest_'+str(int(opened));write('models/block/'+name+'.json',{'parent':'minecraft:block/block','textures':tex,'elements':[base,lid]})
 for facing,yaw in [('north',0),('east',90),('south',180),('west',270)]:variants[f'facing={facing},open={str(opened).lower()}']={'model':'techmod:block/'+name,'y':yaw}
write('blockstates/reinforced_chest.json',{'variants':variants});write('items/reinforced_chest.json',{'model':{'type':'minecraft:model','model':'techmod:block/reinforced_chest_0'}})
# Cutter is a low chamber with a guarded rotary cutting head and a separate discharge tray.
variants={}
for active in [False,True]:
 for status in range(3):
  model=json.loads((A/'models/block/press_0_0_0.json').read_text());model['textures']['lamp']=f'techmod:block/machine_lamp_{status}';model['textures']['heater']='techmod:block/machine_inside'
  e=model['elements'];e +=[box([3,4,4],[13,5,11],'steel'),box([7,5,6],[9,11,9],'steel'),box([9,5.5,6],[12,10.5,9],'copper'),box([3,3.05,-.2],[13,3.6,3],'steel')]
  # Visually expose input material and moving blade behind a safety comb.
  for x in [3.5,6.5,9.5,12.5]:e.append(box([x,4.5,1.8],[x+.25,10.5,2.2],'steel'))
  for cube in e:
   if any(f['texture']=='#lamp'for f in cube['faces'].values()):cube['light_emission']=10 if status else 0
  name=f'metal_cutter_0_{int(active)}_{status}';write('models/block/'+name+'.json',model)
  for face,yaw in [('north',0),('east',90),('south',180),('west',270)]:
   for upper in [False,True]:variants[f'facing={face},upper={str(upper).lower()},active={str(active).lower()},status={status}']={'model':'techmod:block/'+name,'y':yaw}
write('blockstates/metal_cutter.json',{'variants':variants});write('items/metal_cutter.json',{'model':{'type':'minecraft:model','model':'techmod:block/metal_cutter_0_0_0'}})
# Splitter has a permanent T-shaped top marking (input from south, outputs west/east).
im=Image.open(A/'textures/block/conveyor_belt_off.png').convert('RGB');d=ImageDraw.Draw(im)
d.line((32,59,32,28,8,28),fill=(192,144,64),width=4);d.line((32,28,56,28),fill=(192,144,64),width=4)
d.polygon([(4,28),(13,22),(13,34)],fill=(232,182,89));d.polygon([(60,28),(51,22),(51,34)],fill=(232,182,89));d.polygon([(32,38),(26,47),(38,47)],fill=(232,182,89));im.save(A/'textures/block/splitter_direction.png')
for p in (A/'models/block').glob('splitter_*.json'):
 if 'empty'in p.name:continue
 model=json.loads(p.read_text());model['textures']['belt']='techmod:block/splitter_direction';write('models/block/'+p.name,model)
# Tool recipe help and ore tags are kept in the data pack for cross-mod compatibility.
print('Chest, guarded cutter and persistent splitter direction generated.')
