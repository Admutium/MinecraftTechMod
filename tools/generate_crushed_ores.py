"""Distinct mineral colors and silhouettes, derived from the approved ore style."""
from pathlib import Path
from PIL import Image
import json, colorsys
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/techmod'
source=Image.open(Path(__file__).parent/'art/crushed_ores_distinct_source.png').convert('RGBA')
for index,metal in enumerate(['iron','copper','gold']):
 tile=source.crop((index*source.width//3,0,(index+1)*source.width//3,source.height))
 tile.putalpha(tile.getchannel('A').point(lambda a:255 if a>=128 else 0))
 tile=tile.crop(tile.getbbox());tile.thumbnail((122,116),Image.Resampling.NEAREST)
 im=Image.new('RGBA',(128,128));im.paste(tile,((128-tile.width)//2,(128-tile.height)//2))
 name='crushed_'+metal
 im.save(root/f'textures/item/{name}.png')
 for sub,data in [('models/item',{'parent':'minecraft:item/generated','textures':{'layer0':'techmod:item/'+name}}),('items',{'model':{'type':'minecraft:model','model':'techmod:item/'+name}})]:
  (root/f'{sub}/{name}.json').write_text(json.dumps(data,indent=2)+'\n')

 # Processing recipes only; no crafting recipes are introduced.
 recipes=root.parents[1]/'data/techmod/recipe'
 recipes.mkdir(parents=True,exist_ok=True)
 (recipes/f'{metal}_ingot_from_crushed_ore.json').write_text(json.dumps({'type':'minecraft:smelting','cookingtime':200,'experience':.7,'group':metal+'_ingot','ingredient':'techmod:'+name,'result':{'id':'minecraft:'+metal+'_ingot'}},indent=2)+'\n')
