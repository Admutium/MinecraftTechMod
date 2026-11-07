"""64px materials and low rooftop panel: floor wire and four corner feet share one cell."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/techmod'
def write(path,data):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
def save(name,im):im.save(root/f'textures/block/{name}.png')
im=Image.new('RGB',(64,64),(21,26,32));d=ImageDraw.Draw(im)
d.rectangle((0,0,63,63),outline=(204,126,65),width=5);d.line((5,5,58,5),fill=(240,170,97),width=2);d.rectangle((9,9,54,54),outline=(8,11,15),width=3)
save('plug_face',im)
r=random.Random(914);im=Image.new('RGB',(64,64),(10,17,31));d=ImageDraw.Draw(im)
for row in range(4):
 for col in range(4):
  x,y=col*16,row*16
  for dy in range(15):
   for dx in range(15):
    n=r.randrange(-3,4);im.putpixel((x+dx,y+dy),(max(0,17+n),max(0,29+n),max(0,49+n)))
  d.line((x,y,x+14,y),fill=(49,65,89));d.line((x,y,x,y+14),fill=(39,55,81));d.line((x+4,y+10,x+9,y+5),fill=(42,61,91),width=2)
save('solar_cells',im)
im=Image.open(root/'textures/block/machine_plate.png').copy();d=ImageDraw.Draw(im)
d.rectangle((2,24,61,29),fill=(161,91,44));d.line((2,24,61,24),fill=(216,143,77))
for x in [8,13,18]:d.rectangle((x,37,x+2,52),fill=(12,17,23))
save('solar_side',im)
im=Image.open(root/'textures/block/machine_inside.png').copy();d=ImageDraw.Draw(im)
d.rectangle((23,23,40,40),fill=(9,13,18),outline=(176,107,53),width=3)
d.polygon([(5,31),(13,25),(13,29),(20,29),(20,34),(13,34),(13,38)],fill=(185,194,195))
d.polygon([(63-x,y)for x,y in [(5,31),(13,25),(13,29),(20,29),(20,34),(13,34),(13,38)]],fill=(185,194,195))
save('solar_bottom',im)
def box(a,b,t,faces=None):return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,16,16]}for f in(faces or ['north','south','west','east','up','down'])}}
for status in range(3):
 tex={'plate':'techmod:block/machine_plate','steel':'techmod:block/machine_steel','side':'techmod:block/solar_side','cells':'techmod:block/solar_cells','bottom':'techmod:block/solar_bottom','socket':'techmod:block/plug_face','lamp':f'techmod:block/machine_lamp_{status}','particle':'techmod:block/solar_cells'}
 e=[box([0,4,0],[16,7,16],'side',['north','south','east','west']),box([0,4,0],[16,4.01,16],'bottom',['down'])]
 for a,b in [([0,7,0],[1,8,16]),([15,7,0],[16,8,16]),([1,7,0],[15,8,1]),([1,7,15],[15,8,16])]:e.append(box(a,b,'steel'))
 e.append(box([1,7,1],[15,7.9,15],'cells',['up']))
 for x in [0,14]:
  for z in [0,14]:e.append(box([x,0,z],[x+2,4,z+2],'steel'))
 e.append(box([6,3.25,6],[10,4,10],'socket'))
 lamp=box([11,3.97,6.75],[13,3.99,9.25],'lamp',['down']);lamp['light_emission']=10 if status else 0;e.append(lamp)
 model={'parent':'minecraft:block/block','textures':tex,'elements':e,'display':{'gui':{'rotation':[30,225,0],'translation':[0,2,0],'scale':[.75,.75,.75]}}}
 write(f'models/block/solar_panel_{status}.json',model)
write('blockstates/solar_panel.json',{'variants':{f'status={s}':{'fabric:type':'techmod:solar','model':f'techmod:block/solar_panel_{s}'}for s in range(3)}})
write('items/solar_panel.json',{'model':{'type':'minecraft:model','model':'techmod:block/solar_panel_0'}})
p=root.parents[1]/'data/minecraft/tags/block/mineable/pickaxe.json';data=json.loads(p.read_text());data['values']=list(dict.fromkeys(data['values']+['techmod:solar_panel']));p.write_text(json.dumps(data,indent=2)+'\n')
