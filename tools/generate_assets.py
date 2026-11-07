"""Generate original pixel-art cable assets. Optional tool: requires Pillow."""
from pathlib import Path
import json
import math
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/techmod'

def save(im, path):
    p = ROOT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    im.save(p)

def write(path, data):
    p = ROOT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')

for variant, name in enumerate(['rubber', 'rubber_ribbed', 'rubber_marked']):
    rng = random.Random(712 + variant)
    im = Image.new('RGBA', (32, 32))
    for y in range(32):
        for x in range(32):
            light = int(12 * math.sin(math.pi * x / 31)) + rng.randrange(-2, 3)
            value = 25 + light + (5 if variant == 1 and y % 8 in (0, 1) else 0)
            color = (value, value + 3, value + 5, 255)
            if variant == 2 and x in (23, 24) and y % 16 < 10:
                color = (151, 105, 47, 255)
            im.putpixel((x, y), color)
    save(im, f'textures/block/{name}.png')

im = Image.new('RGBA', (32, 32), (29, 31, 34, 255))
d = ImageDraw.Draw(im)
d.ellipse((3, 3, 28, 28), fill=(62, 63, 64), outline=(14, 16, 19), width=2)
d.ellipse((7, 7, 24, 24), fill=(112, 54, 29), outline=(210, 130, 64))
for x, y in [(12, 11), (19, 11), (9, 17), (16, 17), (23, 17), (13, 23), (20, 23)]:
    d.ellipse((x-2, y-2, x+2, y+2), fill=(188, 99, 45), outline=(237, 162, 84))
    d.point((x-1, y-1), fill=(255, 205, 123))
save(im, 'textures/block/copper_end.png')

im = Image.new('RGBA', (32, 32), (99, 110, 115, 255))
d = ImageDraw.Draw(im)
for x in range(32):
    value = int(94 + 32 * math.sin(x * math.pi / 31))
    d.line((x, 0, x, 31), fill=(value, value+9, value+13))
for y in (0, 1, 15, 16, 30, 31):
    d.line((0, y, 31, y), fill=(55, 64, 69))
save(im, 'textures/block/holder.png')

# User-selected generated artwork, normalized to an atlas-friendly 128x128 PNG.
# Resampling is nearest-neighbor to retain the approved pixel-art appearance.
with Image.open(Path(__file__).parent / 'art/copper_cable_coil_source.png') as approved:
    save(approved.convert('RGBA').resize((128, 128), Image.Resampling.NEAREST), 'textures/item/copper_cable.png')

im = Image.new('RGBA', (32, 32))
d = ImageDraw.Draw(im)
d.polygon([(4, 22), (13, 15), (28, 19), (19, 28)], fill=(59, 70, 78), outline=(19, 25, 31))
d.polygon([(4, 19), (13, 12), (28, 16), (19, 25)], fill=(133, 148, 153), outline=(38, 47, 53))
d.arc((10, 3, 25, 22), 175, 365, fill=(41, 49, 56), width=7)
d.arc((10, 3, 25, 22), 175, 365, fill=(156, 169, 173), width=3)
for x, y in [(8, 19), (23, 18)]:
    d.rectangle((x-1, y-1, x+1, y+1), fill=(44, 51, 56))
    d.point((x, y-1), fill=(219, 224, 222))
save(im, 'textures/item/cable_holder.png')

im = Image.new('RGBA', (32, 32))
d = ImageDraw.Draw(im)
d.line((7, 27, 13, 15, 19, 8), fill=(23, 28, 32), width=7)
d.line((25, 27, 19, 15, 13, 8), fill=(23, 28, 32), width=7)
d.line((7, 27, 13, 17), fill=(174, 75, 33), width=5)
d.line((25, 27, 19, 17), fill=(174, 75, 33), width=5)
d.polygon([(10, 13), (9, 4), (14, 7), (16, 11), (19, 6), (24, 4), (22, 14), (16, 19)], fill=(139, 151, 156), outline=(37, 45, 51))
d.line((10, 5, 14, 9), fill=(219, 225, 221), width=1)
d.ellipse((14, 13, 18, 17), fill=(51, 61, 65), outline=(192, 199, 196))
save(im, 'textures/item/wire_cutters.png')

for name in ['copper_cable', 'cable_holder', 'wire_cutters']:
    write(f'models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'techmod:item/{name}'}})
    write(f'items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'techmod:item/{name}'}})
write('blockstates/cable.json', {'variants': {'': {'fabric:type': 'techmod:cable'}}})
write('lang/en_us.json', {
    'itemGroup.techmod': 'TechMod',
    'block.techmod.cable': 'Cable assembly',
    'item.techmod.copper_cable': 'Insulated Copper Cable',
    'item.techmod.cable_holder': 'Cable Holder',
    'item.techmod.wire_cutters': 'Wire Cutters',
    'message.techmod.selected': 'First holder selected. Click another holder within 5 blocks. Sneak-click to cancel.',
    'message.techmod.cancelled': 'Cable connection cancelled.',
    'message.techmod.connected': 'Cable connected.',
    'message.techmod.range': 'Holders must be within 5 blocks in a straight line.',
    'message.techmod.obstructed': 'The sagging cable would intersect an obstacle or another cable.',
    'message.techmod.unloaded': 'All cable cells must be loaded.',
    'message.techmod.protected': 'You cannot place a cable here.',
    'message.techmod.missing_holder': 'The first holder is no longer available.',
    'message.techmod.duplicate': 'These holders are already connected.',
    'message.techmod.holder_full': 'This holder already has four connections.',
    'message.techmod.cell_full': 'Eight suspended cables already pass through this cell.',
    'message.techmod.lava': 'The cable burned in lava.'
})
write('lang/ru_ru.json', {
    'itemGroup.techmod': 'TechMod',
    'block.techmod.cable': 'Кабельная сборка',
    'item.techmod.copper_cable': 'Изолированный медный провод',
    'item.techmod.cable_holder': 'Кабельный держатель',
    'item.techmod.wire_cutters': 'Кусачки',
    'message.techmod.selected': 'Первый держатель выбран. Нажмите на второй в радиусе 5 блоков. Shift + ПКМ — отмена.',
    'message.techmod.cancelled': 'Соединение отменено.',
    'message.techmod.connected': 'Провод подключён.',
    'message.techmod.range': 'Между держателями должно быть не больше 5 блоков по прямой.',
    'message.techmod.obstructed': 'Провисающий провод пересечётся с препятствием или другим проводом.',
    'message.techmod.unloaded': 'Все ячейки провода должны быть загружены.',
    'message.techmod.protected': 'Здесь нельзя разместить провод.',
    'message.techmod.missing_holder': 'Первый держатель больше недоступен.',
    'message.techmod.duplicate': 'Эти держатели уже соединены.',
    'message.techmod.holder_full': 'У держателя уже четыре подключения.',
    'message.techmod.cell_full': 'Здесь уже проходит восемь подвесных проводов.',
    'message.techmod.lava': 'Провод сгорел в лаве.'
})
