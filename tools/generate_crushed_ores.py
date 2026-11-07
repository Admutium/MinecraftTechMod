"""Approved option 6, same geometry with user-requested mineral color correction."""
from pathlib import Path
from PIL import Image
import json, colorsys
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/techmod'
source=Image.open(Path(__file__).parent/'art/crushed_ore_source.png').convert('RGBA')
source=source.crop(source.getbbox()).resize((112,80),Image.Resampling.NEAREST)
for metal,hue,saturation in [('iron',.065,.52),('copper',.035,.72),('gold',.125,.72)]:
 im=Image.new('RGBA',(128,128));im.paste(source,(8,24))
 for y in range(128):
  for x in range(128):
   r,g,b,a=im.getpixel((x,y));h,s,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
   if s>.2 and r>g and g>b:
    nr,ng,nb=colorsys.hsv_to_rgb(hue,saturation,v);r,g,b=round(nr*255),round(ng*255),round(nb*255)
   im.putpixel((x,y),(r,g,b,255 if a>=128 else 0))
 name='crushed_'+metal
 im.save(root/f'textures/item/{name}.png')
 for sub,data in [('models/item',{'parent':'minecraft:item/generated','textures':{'layer0':'techmod:item/'+name}}),('items',{'model':{'type':'minecraft:model','model':'techmod:item/'+name}})]:
  (root/f'{sub}/{name}.json').write_text(json.dumps(data,indent=2)+'\n')

 # Processing recipes only; no crafting recipes are introduced.
 recipes=root.parents[1]/'data/techmod/recipe'
 recipes.mkdir(parents=True,exist_ok=True)
 (recipes/f'{metal}_ingot_from_crushed_ore.json').write_text(json.dumps({'type':'minecraft:smelting','cookingtime':200,'experience':.7,'group':metal+'_ingot','ingredient':'techmod:'+name,'result':{'id':'minecraft:'+metal+'_ingot'}},indent=2)+'\n')
