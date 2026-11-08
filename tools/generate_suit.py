"""Original Kontur preview suit: editable bone/cuboid definition, palette atlas, Blockbench reference.
Coordinates use Minecraft humanoid model pixels; keep UV regions and material IDs in sync.
"""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random,math,uuid
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/techmod'
# Material 6 is a neutral emissive mask, tinted at runtime; field materials retain their own colors.
colors=[(23,28,35),(48,57,70),(69,82,100),(112,133,156),(172,99,52),(8,12,18),(255,244,219),(86,103,125),(26,173,181),(37,110,199),(101,67,187),(174,58,165),(233,116,56),(255,148,18),(255,221,111),(183,213,220)]
parts=[]
def part(name,parent=None,pivot=(0,0,0),rotation=(0,0,0)):
 p={'name':name,'parent':parent,'pivot':list(pivot),'rotation':list(rotation),'cubes':[]};parts.append(p);return p
bones={name:part(name,None,pivot)for name,pivot in [('head',(0,0,0)),('body',(0,0,0)),('right_arm',(-5,2,0)),('left_arm',(5,2,0)),('right_leg',(-1.9,12,0)),('left_leg',(1.9,12,0))]}
def box(p,mat,a,b,pass_='base'):
 p['cubes'].append({'material':mat,'from':list(a),'to':list(b),'pass':pass_})
def b(p,m,x,y,z,w,h,d,pass_='base'):box(p,m,(x,y,z),(x+w,y+h,z+d),pass_)
def lamp(p,x,y,z,w,h,d):b(p,6,x,y,z,w,h,d,'signal')
h=bones['head']
# Close helmet shell, segmented crown and lower breathing jaw. Front is -Z.
# Chamfered helmet: actual silhouette is independent of the vanilla player skin.
b(h,0,-3.8,-8.05,-4.05,7.6,8.2,8.1);b(h,1,-4.22,-7.4,-3.5,8.44,7.5,7.0)
for side in [-1,1]:
 for z in [-3.6,3.6]:
  bevel=part('helmet_bevel_'+str(side)+'_'+str(z),'head',(side*3.8,-3.8,z),(0,math.pi/4,0));b(bevel,2,-.44,-3.7,-.44,.88,7.4,.88)
 crown=part('helmet_crown_'+str(side),'head',(side*1.75,-8.03,-.25),(0,0,side*.1));b(crown,2,-1.8,-.32,-3.25,3.6,.42,6.5)
 brow=part('helmet_brow_'+str(side),'head',(side*1.95,-6.35,-4.05),(0,-side*.1,side*.07));b(brow,1,-1.95,-1.45,-.27,3.9,2.1,.7);b(brow,3,-1.8,.62,-.3,3.6,.18,.35)
 cheek=part('helmet_cheek_'+str(side),'head',(side*3.3,-2.1,-3.95),(0,-side*.23,-side*.16));b(cheek,2,-.75,-1.5,-.45,1.5,3.0,.85)
 temple=part('temple_'+str(side),'head',(side*4.24,-4.7,-.35));b(temple,3,-.16,-1.4,-1.3,.32,2.8,2.6);b(temple,4,side*.2-.14,-.9,-.8,.28,1.8,1.6)
 for z in [-.4,.2,.8]:b(temple,5,side*.36-.06,-.7,z,.12,1.4,.22)
# Three separate visor segments wrap around the face.
b(h,5,-3.8,-5.25,-4.3,7.6,1.95,.35);lamp(h,-1.4,-5.03,-4.73,2.8,1.42,.13)
for side in [-1,1]:
 lens=part('visor_'+str(side),'head',(side*2.55,-4.35,-4.4),(0,-side*.18,0));lamp(lens,-1.15,-.7,-.32,2.3,1.4,.12)
b(h,1,-2.65,-3.1,-4.36,5.3,2.9,.55);b(h,2,-2.15,-2.7,-4.7,4.3,2.05,.32)
for y in [-2.1,-1.5]:b(h,5,-1.4,y,-5.03,2.8,.23,.08)
b(h,3,-1.8,-.7,-4.64,3.6,.25,.3);lamp(h,-.27,-8.47,-3.0,.54,.15,1.65)
b(h,2,-2.6,-7.5,3.98,5.2,3.1,.35)
# Torso: flexible base, split pectoral plates, abdominal sections and restrained copper fittings.
t=bones['body'];b(t,0,-4.18,-.15,-2.18,8.36,12.3,4.36)
for side in [-1,1]:
 p=part('pectoral_'+str(side),'body',(side*2.08,2.4,-2.28),(0,-side*.07,side*.07));b(p,1,-1.95,-2.25,-.23,3.9,4.5,.55);b(p,2,-1.65,-1.9,-.48,3.3,.65,.32)
 b(t,4,side*3.2-.38,.65,-2.8,.76,1.2,.3);b(t,5,side*3.2-.22,.9,-2.98,.44,.45,.12)
 for y in [5.4,7.15,8.9]:b(t,1,side*2.28-1.4,y,-2.39,2.8,1.35,.35)
 b(t,2,side*4.17-.18,3,-1.9,.36,6.7,3.8)
# Central chest power node, four visible status bars.
b(t,3,-1.45,.8,-2.95,2.9,4.8,.62);b(t,5,-1.13,1.15,-3.1,2.26,4.1,.23)
for y in [1.55,2.45,3.35,4.25]:lamp(t,-.76,y,-3.37,1.52,.42,.12)
b(t,2,-4.35,10.2,-2.43,8.7,1.25,4.86);b(t,3,-1.35,10.15,-2.86,2.7,1.4,.58);b(t,5,-.8,10.42,-3.02,1.6,.84,.18)
# Compact spinal power pack, twin recessed vector emitters, panel ribs.
b(t,1,-2.6,.25,2.15,5.2,8.9,1.55);b(t,2,-1.35,.8,3.62,2.7,7.5,.4);b(t,5,-.85,2,4.03,1.7,4.8,.18)
for y in [2.4,3.4,4.4,5.4,6.4]:lamp(t,-.5,y,4.23,1,.4,.12)
for side in [-1,1]:
 p=part('thruster_'+str(side),'body',(side*2.65,3.4,2.85),(0,0,side*.12));b(p,2,-.85,-1.6,-.35,1.7,3.2,1.9);b(p,3,-.64,-1.15,1.48,1.28,2.35,.18);b(p,5,-.45,-.85,1.68,.9,1.7,.12);lamp(p,-.3,-.35,1.82,.6,.75,.1)
 for y in [7.4,8.2]:b(t,4,side*2.0-.35,y,3.79,.7,.38,.17)
# Arms with narrow joint gaps, glove, elbow cuff and inset wrist readout.
for side,name in [(-1,'right_arm'),(1,'left_arm')]:
 p=bones[name];x=-3.12 if side==-1 else -1.12
 b(p,0,x,-2.12,-2.12,4.24,12.24,4.24)
 shoulder=part('shoulder_'+str(side),name,(x+2.12,-1.15,0),(0,0,-side*.14));b(shoulder,1,-2.35,-1.65,-2.4,4.7,2.9,4.8);b(shoulder,2,-2.1,-1.87,-2.05,4.2,.32,4.1);b(shoulder,3,-1.85,.85,-2.51,3.7,.18,.2)
 b(p,4,x+.55,-2.84,-.9,.65,.18,1.8);lamp(p,x+.4,-.55,-2.43,2.35,.32,.13)
 b(p,1,x+.1,1.7,-2.27,4.04,2.6,4.54);b(p,5,x+.25,4.6,-2.2,3.74,.8,4.4)
 b(p,2,x-.12,5.4,-2.32,4.48,3.45,4.64);b(p,1,x+.28,8.88,-2.28,3.68,1.22,4.56)
 b(p,3,x+.52,5.6,-2.6,3.1,2.85,.3);b(p,5,x+.8,5.85,-2.79,2.55,2.3,.15)
 for y in [6.1,6.75,7.4]:lamp(p,x+1.1,y,-2.96,1.95,.28,.12)
 for z in [-1.1,.3,1.7]:b(p,4,(x-.3 if side==-1 else x+4.24),6.1,z,.3,1.6,.38)
 for xx in [x+.25,x+1.2,x+2.15,x+3.1]:b(p,2,xx,9.1,-2.49,.68,.65,.23)
# Legs: thigh panels, kneecaps, shin plating and close-fitting boots.
for name in ['right_leg','left_leg']:
 p=bones[name];b(p,0,-2.12,-.1,-2.12,4.24,12.2,4.24)
 b(p,1,-2.25,.45,-2.3,4.5,3.65,4.6);b(p,2,-1.75,.8,-2.54,3.5,2.8,.3)
 b(p,5,-2.15,4.3,-2.2,4.3,.65,4.4);b(p,3,-1.95,4.7,-2.49,3.9,1.6,.42);b(p,1,-1.6,4.95,-2.72,3.2,1.12,.29)
 b(p,1,-2.25,6.7,-2.3,4.5,3.5,4.6);b(p,2,-1.5,6.95,-2.65,3,2.95,.4);b(p,5,-.52,7.2,-2.92,1.04,2.4,.12);lamp(p,-.28,7.45,-3.07,.56,1.9,.12)
 b(p,3,-2.18,10.15,-2.3,4.36,.45,4.6);b(p,1,-2.27,10.6,-2.7,4.54,1.6,5.1);b(p,2,-2.33,11.7,-2.9,4.66,.5,5.3)
 b(p,2,-1.35,7.5,2.12,2.7,2.25,.6);b(p,5,-.7,7.8,2.73,1.4,1.7,.14);lamp(p,-.4,8.05,2.89,.8,1.2,.1)
# Optional wing sheets use dedicated alpha-cut, faceted textures; frame geometry is separate.
rainbow=part('rainbow','body',(0,1.5,3.1));gravity=part('gravity','body',(0,1.5,3.1))
def plane(p,x,y,z,w,h,graphic):
 p['cubes'].append({'material':0,'from':[x,y,z],'to':[x+w,y+h,z],'pass':'field','graphic':graphic,'faces':['south']})
def beam(parent,name,side,a,b1):
 dx=b1[0]-a[0];dy=b1[1]-a[1];length=math.hypot(dx,dy);p=part(name,parent,(side*a[0],a[1],-.12),(0,0,math.atan2(dy,side*dx)));b(p,2,-.12,-.17,-.18,length+.24,.34,.36)
for side in [-1,1]:
 wing=part('rainbow_'+str(side),'rainbow',(side*2.9,.5,.8),(0,side*.22,-side*.19))
 plane(wing,-14 if side==-1 else 0,0,.08,14,12,'rainbow_left'if side==-1 else'rainbow_right')
 beam(wing['name'],'wing_spar_'+str(side),side,(0,0),(14,4));beam(wing['name'],'wing_tip_'+str(side),side,(14,4),(7.5,11))
 b(wing,4,-.45,-.4,-.55,.9,1.25,1.0)
 wing=part('gravity_'+str(side),'gravity',(side*2.7,1,.9),(0,side*.12,-side*.12))
 b(wing,2,(-2.4 if side==-1 else 0),-.5,-.4,2.4,1.7,1.8)
 plane(wing,-15 if side==-1 else 0,0,.3,15,4,'gravity_left'if side==-1 else'gravity_right')
 fin=part('gravity_fin_'+str(side),'gravity',(side*2.5,5.7,1),(0,0,side*.35))
 plane(fin,-7.5 if side==-1 else 0,0,.2,7.5,2,'gravity_left'if side==-1 else'gravity_right')
# UV-unwrapped per-cuboid material art: bevels, seams, brushed copper and woven undersuit.
# UV coordinates are model pixels; scale .25 samples four atlas pixels per model pixel.
SCALE=4;W=1024;H=1024;tex=Image.new('RGBA',(W,H),(0,0,0,0));rng=random.Random(144);cursor_x=0;cursor_y=0;row_h=0

def allocate(w,h):
 global cursor_x,cursor_y,row_h
 w=math.ceil(w/16)*16+16;h=math.ceil(h/16)*16+16
 if cursor_x+w>W:cursor_x=0;cursor_y+=row_h;row_h=0
 if cursor_y+h>H:raise ValueError('Suit atlas overflow')
 result=(cursor_x+8,cursor_y+8);cursor_x+=w;row_h=max(row_h,h);return result

def face(rect,mat):
 x0,y0,x1,y1=map(round,rect);ww=x1-x0;hh=y1-y0
 if ww<=0 or hh<=0:return
 im=Image.new('RGBA',(ww,hh));base=colors[mat]
 for y in range(hh):
  for x in range(ww):
   grain=rng.randrange(-2,3)
   if mat==0:grain+=3 if (x+y)%6==0 else -2 if (x-y)%6==0 else 0
   elif mat==4:grain+=5 if (x+y)//2%7==0 else 0
   im.putpixel((x,y),tuple(max(0,min(255,c+grain))for c in base)+(255,))
 d=ImageDraw.Draw(im)
 if mat not in [0,5,6] and min(ww,hh)>=3:
  d.rectangle((0,0,ww-1,hh-1),outline=tuple(max(0,c-22)for c in base)+(255,))
  d.line((1,1,ww-2,1),fill=tuple(min(255,c+28)for c in base)+(255,))
  d.line((1,1,1,hh-2),fill=tuple(min(255,c+15)for c in base)+(255,))
  if min(ww,hh)>=16:
   d.line((3,hh-4,ww-4,hh-4),fill=tuple(max(0,c-10)for c in base)+(255,))
   for x,y in [(3,3),(ww-4,3),(3,hh-4),(ww-4,hh-4)]:d.point((x,y),fill=tuple(min(255,c+38)for c in base)+(255,))
 if mat==5 and min(ww,hh)>5:
  for y in range(3,hh,4):d.line((2,y,ww-3,y),fill=(18,25,32,255))
 tex.paste(im,(x0,y0))

def graphic(kind,left=False):
 import colorsys
 ww,hh=(56,48)if kind=='rainbow'else(60,16)
 im=Image.new('RGBA',(ww,hh));mask=Image.new('L',(ww,hh));draw=ImageDraw.Draw(mask)
 points=[(0,0),(55,16),(52,21),(30,44),(20,47),(5,30),(0,22)]if kind=='rainbow'else[(0,6),(59,0),(47,9),(0,15)]
 draw.polygon(points,fill=235)
 for y in range(hh):
  for x in range(ww):
   if not mask.getpixel((x,y)):continue
   if kind=='rainbow':
    hue=(.48+.37*y/hh+.05*x/ww)%1;sat=.68;val=.78+.15*((x//7+y//6)%2);rgb=colorsys.hsv_to_rgb(hue,sat,val)
    color=tuple(round(c*255)for c in rgb)
   else:color=(255,round(124+85*(1-y/hh)),round(15+45*(1-y/hh)))
   im.putpixel((x,y),color+(235,))
 d=ImageDraw.Draw(im)
 for yy in range(0,hh+1,8):
  for xx in range(0,ww+1,8):
   for a,b1 in [((xx,yy),(xx+8,yy+8)),((xx,yy+8),(xx+8,yy+8))]:
    layer=Image.new('RGBA',im.size);ld=ImageDraw.Draw(layer);ld.line([a,b1],fill=(220,245,255,70)if kind=='rainbow'else(255,243,163,120));layer.putalpha(Image.composite(layer.getchannel('A'),Image.new('L',im.size),mask));im=Image.alpha_composite(im,layer)
 d=ImageDraw.Draw(im);d.line(points+[points[0]],fill=(167,248,245,255)if kind=='rainbow'else(255,235,132,255),width=1)
 if not left:im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
 return im
for p in parts:
 for c in p['cubes']:
  dims=[b1-a for a,b1 in zip(c['from'],c['to'])]
  if 'graphic'in c:
   image=graphic('rainbow'if c['graphic'].startswith('rainbow')else'gravity',c['graphic'].endswith('left'));ww,hh=image.size
   # Fin geometry is half-size, so it samples the same full texture with twice the UV density.
   factor=ww/dims[0];u,v=allocate(ww*2,hh);tex.paste(image,(u,v));tex.paste(image,(u+ww,v));c['uv']=[u/factor,v/factor];c['uv_scale']=factor
  else:
   w,h,d=[v*SCALE for v in dims];u,v=allocate(2*(w+d),h+d);c['uv']=[u/SCALE,v/SCALE];c['uv_scale']=SCALE
   for rect in [(u+d,v,u+d+w,v+d),(u+d+w,v,u+d+2*w,v+d),(u,v+d,u+d,v+d+h),(u+d,v+d,u+d+w,v+d+h),(u+d+w,v+d,u+2*d+w,v+d+h),(u+2*d+w,v+d,u+2*d+2*w,v+d+h)]:face(rect,c['material'])
(A/'textures/entity').mkdir(exist_ok=True);tex.save(A/'textures/entity/kontur_suit.png')
obj={'texture_width':W,'texture_height':H,'parts':parts};(A/'suit/prototype.json').write_text(json.dumps(obj,indent=2)+'\n')
print('Suit generated:',sum(len(p['cubes'])for p in parts),'cuboids,',len(parts),'bones; atlas rows',cursor_y+row_h)
# Editable Blockbench project: convert humanoid Y-down coordinates to editor Y-up.
import base64
absolute={};groups={};elements=[];outliner=[]
for p in parts:
 parent=absolute.get(p['parent'],[0,0,0]);origin=[a+b for a,b in zip(parent,p['pivot'])];absolute[p['name']]=origin
 group={'name':p['name'],'origin':[origin[0],24-origin[1],origin[2]],'rotation':[-math.degrees(p['rotation'][0]),math.degrees(p['rotation'][1]),-math.degrees(p['rotation'][2])],'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'kontur/suit/'+p['name'])),'children':[],'visibility':p['name']not in ['rainbow','gravity']}
 groups[p['name']]=group
 if p['parent']:groups[p['parent']]['children'].append(group)
 else:outliner.append(group)
 for i,c in enumerate(p['cubes']):
  uid=str(uuid.uuid5(uuid.NAMESPACE_URL,'kontur/suit/'+p['name']+'/'+str(i)));a=c['from'];b1=c['to'];factor=c['uv_scale'];u,v=[n*factor for n in c['uv']];w,h,d=[(n-m)*factor for m,n in zip(a,b1)]
  uv={'north':[u+d,v+d,u+d+w,v+d+h],'south':[u+2*d+w,v+d,u+2*d+2*w,v+d+h],'west':[u,v+d,u+d,v+d+h],'east':[u+d+w,v+d,u+2*d+w,v+d+h],'up':[u+d,v,u+d+w,v+d],'down':[u+d+w,v,u+d+2*w,v+d]}
  elements.append({'name':p['name']+'_'+str(i),'from':[a[0]+origin[0],24-b1[1]-origin[1],a[2]+origin[2]],'to':[b1[0]+origin[0],24-a[1]-origin[1],b1[2]+origin[2]],'origin':group['origin'],'uuid':uid,'faces':{f:{'uv':rect,'texture':0}for f,rect in uv.items()}});group['children'].append(uid)
project={'meta':{'format_version':'4.10','model_format':'free','box_uv':False},'name':'Kontur suit prototype','resolution':{'width':W,'height':H},'elements':elements,'outliner':outliner,'textures':[{'name':'kontur_suit.png','width':W,'height':H,'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'kontur/suit/texture')),'source':'data:image/png;base64,'+base64.b64encode((A/'textures/entity/kontur_suit.png').read_bytes()).decode()}]}
(R/'tools/art/kontur_suit.bbmodel').write_text(json.dumps(project,indent=2)+'\n')

for lang in ['ru_ru','en_us']:
 p=A/f'lang/{lang}.json';data=json.loads(p.read_text());ru=lang=='ru_ru'
 strings={'key.techmod.suit_preview':('Предпросмотр костюма','Suit preview'),'screen.techmod.suit_preview':('Контур — примерка костюма','Kontur — suit fitting'),'screen.techmod.suit_visible':('Костюм: %s','Suit: %s'),'screen.techmod.suit_wings':('Крылья: %s','Wings: %s'),'screen.techmod.suit_color':('Акценты: %s','Accents: %s'),'screen.techmod.suit_visual_only':('Визуальный прототип','Visual prototype'),'screen.techmod.suit_f5':('F5 — вид от третьего лица','F5 — third-person view'),'screen.techmod.suit_wing.0':('Сложены','Folded'),'screen.techmod.suit_wing.1':('Радужные','Prismatic'),'screen.techmod.suit_wing.2':('Гравитационные','Gravity'),'screen.techmod.suit_color.0':('Оранжевые','Orange'),'screen.techmod.suit_color.1':('Бирюзовые','Teal'),'screen.techmod.suit_color.2':('Фиолетовые','Violet'),'screen.techmod.suit_color.3':('Белые','White')}
 for key,pair in strings.items():data[key]=pair[0 if ru else 1]
 p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
