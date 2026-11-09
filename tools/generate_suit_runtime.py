#!/usr/bin/env python3
"""Placeholder station housing and prototype item art. Suit model/atlas stay in generate_suit.py."""
from pathlib import Path
from PIL import Image,ImageDraw
import json
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/techmod'
def save(path,data):
 p=A/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
def box(a,b,texture='steel'):
 return {'from':a,'to':b,'faces':{f:{'texture':'#'+texture} for f in ['up','down','east','west','north','south']}}
for name in ['suit_station','creative_source']:
 tex={'steel':'techmod:block/machine_steel','front':'techmod:block/'+name,'socket':'techmod:block/energy_output' if name=='creative_source' else 'techmod:block/energy_input'}
 # Reuse the established input/output socket art so connection directions remain familiar.
 tex['socket']='techmod:block/machine_output' if name=='creative_source' else 'techmod:block/machine_input'
 elements=[box([0,0,0],[16,16,16])] if name=='creative_source' else [box([0,0,0],[16,3,16]),box([0,3,12],[3,16,16]),box([13,3,12],[16,16,16]),box([3,13,13],[13,16,16])]
 if name=='creative_source':elements[0]['faces']['north']['texture']='#front';elements[0]['faces']['south']['texture']='#socket'
 else:elements.append(box([3,1,0],[13,2,12],'front'));elements.append(box([5,4,14],[11,10,16],'socket'))
 save(Path('models/block')/(name+'.json'),{'textures':{'particle':tex['steel'],**tex},'elements':elements})
 save(Path('blockstates')/(name+'.json'),{'variants':{f'facing={f}':{'model':'techmod:block/'+name,'y':y}for f,y in [('north',0),('east',90),('south',180),('west',270)]}})
 save(Path('models/item')/(name+'.json'),{'parent':'techmod:block/'+name});save(Path('items')/(name+'.json'),{'model':{'type':'minecraft:model','model':'techmod:item/'+name}})
 im=Image.new('RGBA',(64,64),(37,48,58,255));d=ImageDraw.Draw(im);d.rectangle((3,3,60,60),outline='#8a969d',width=2);d.rectangle((10,10,53,53),fill='#111b23',outline='#b8c2c6',width=2)
 if name=='creative_source':
  d.ellipse((15,23,36,41),outline='#ffb650',width=4);d.ellipse((29,23,50,41),outline='#ffb650',width=4);d.line((21,27,44,37),fill='#ffb650',width=4)
 else:
  for y in range(16,48,8):d.rectangle((16,y,47,y+3),fill='#ec9f46')
 im.save(A/'textures/block'/f'{name}.png')
for name in ['kontur_suit','nutrient_capsule']:
 im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
 if name=='kontur_suit':
  d.polygon([(20,5),(43,5),(50,14),(48,40),(38,49),(25,49),(15,40),(13,15)],fill='#172732',outline='#718793',width=2);d.polygon([(18,19),(46,19),(43,28),(21,28)],fill='#ffad43');d.line((22,33,28,39,36,39,42,33),fill='#697f8e',width=3);d.rectangle((25,43,38,46),fill='#f6a644');d.polygon([(15,45),(25,51),(38,51),(48,45),(55,59),(9,59)],fill='#2f4554',outline='#82939e',width=2)
 else:
  d.rounded_rectangle((19,5,44,58),radius=9,fill='#c7dbdd',outline='#718d9d',width=2);d.rounded_rectangle((23,15,40,47),radius=4,fill='#344757');d.rectangle((25,19,38,43),fill='#92be4d');d.rectangle((25,7,38,11),fill='#ecad43');d.line((27,21,27,38),fill='#c8e890',width=2)
 im.save(A/'textures/item'/f'{name}.png');save(Path('models/item')/(name+'.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'techmod:item/'+name}});save(Path('items')/(name+'.json'),{'model':{'type':'minecraft:model','model':'techmod:item/'+name}})
ru_names=['Дополнительный инвентарь','Чёрный список','Магнит','Автопитание','Отсек капсул','Компас','Полёт','Супербег','Автоподъём','Прыжок','Защита от падения','Предел урона','Плавание','Запас воздуха','Подводная добыча','Скорость добычи','Сканер руд','Добыча жилы','Личная яркость','Ночное зрение','Броня','Энергощит','Стойкость щита','Экономичность щита','Магическая защита','Защита от снарядов','Защита от взрывов','Защита от ожогов','Стабилизация','Усиление урона','Сканер врагов','Предупреждение','Аварийное лечение','Хамелеон','Здоровье цели','Маяк','Техновизор','Варп','Анализ растений','Ёмкость','Зарядка','Экономичность','Мощность']
import re
src=(R/'src/main/java/dev/admutium/techmod/suit/SuitUpgrade.java').read_text();ids=re.findall(r'([A-Z_]+)\("',src)
assert len(ids)==len(ru_names),(len(ids),len(ru_names))
for lang in ['ru_ru','en_us']:
 p=A/'lang'/f'{lang}.json';data=json.loads(p.read_text());ru=lang=='ru_ru'
 data.update({'item.techmod.kontur_suit':'Костюм «Контур»' if ru else 'Kontur suit','item.techmod.nutrient_capsule':'Питательная капсула' if ru else 'Nutrient capsule','block.techmod.suit_station':'Станция обслуживания костюма' if ru else 'Suit service station','block.techmod.creative_source':'Творческий источник энергии' if ru else 'Creative energy source','entity.techmod.suit_remains':'Повреждённый костюм' if ru else 'Suit remains','suit.techmod.station_required':'Надевать костюм можно только на станции' if ru else 'Use a suit service station to equip this suit','suit.techmod.upgrades_count':'Улучшений: %s' if ru else 'Upgrade levels: %s','suit.techmod.sealed':'Запечатан: верните на станцию для извлечения вещей' if ru else 'Sealed: recover contents at a service station','key.techmod.suit_menu':'Системы костюма' if ru else 'Suit systems','key.techmod.suit_magnet':'Импульс магнита' if ru else 'Magnet pulse','key.techmod.suit_players':'Список игроков в костюме' if ru else 'Player list while wearing suit'})
 for id,name in zip(ids,ru_names):data['suit.upgrade.'+id.lower()]=name if ru else id.replace('_',' ').title()
 for id,name in zip(['storage','movement','water','mining','defense','combat','utility','energy'],['Груз','Ход','Вода','Шахта','Щит','Бой','Сервис','ТЕ']):data['suit.branch.'+id]=name if ru else id.title()[:6]
 p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
for tag in ['mineable/pickaxe']:
 p=R/'src/main/resources/data/minecraft/tags/block'/f'{tag}.json';data=json.loads(p.read_text())
 for name in ['suit_station','creative_source']:
  if 'techmod:'+name not in data['values']:data['values'].append('techmod:'+name)
 p.write_text(json.dumps(data,indent=2)+'\n')
