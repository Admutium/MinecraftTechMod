"""Original, deterministic synthesized industrial loops; no third-party audio samples."""
from pathlib import Path
import math,random,wave,struct,subprocess,tempfile,json
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/techmod'
rate=44100;n=rate*4
names=['coal_generator','electric_furnace','crusher','press','conveyor_drive'];events={}
for index,name in enumerate(names):
 rng=random.Random(903+index);samples=[];low=0;crackle=0
 for i in range(n):
  t=i/rate;noise=rng.uniform(-1,1);low+=.055*(noise-low)
  if name=='coal_generator':
   if rng.random()<.00035:crackle+=rng.uniform(.02,.06)
   crackle*=.997
   v=.075*math.sin(2*math.pi*52*t)+.035*math.sin(2*math.pi*78*t)+low*.15+noise*crackle
  elif name=='electric_furnace':v=.055*math.sin(2*math.pi*90*t)+.015*math.sin(2*math.pi*180*t)+low*.12
  elif name=='conveyor_drive':v=.03*math.sin(2*math.pi*65*t)+low*.04
  elif name=='press':
   pulse=max(0,math.sin(2*math.pi*t/4))**20
   v=.04*math.sin(2*math.pi*70*t)+low*(.08+.25*pulse)
  else:
   rhythm=.5+.5*math.sin(2*math.pi*3*t)
   v=.065*math.sin(2*math.pi*55*t)+.028*math.sin(2*math.pi*110*t)+low*(.15+.30*rhythm**6)
  envelope=min(1,i/(rate*.012),(n-1-i)/(rate*.012));samples.append(v*envelope)
 out=root/f'sounds/machines/{name}.ogg';out.parent.mkdir(parents=True,exist_ok=True)
 with tempfile.TemporaryDirectory()as tmp:
  wav=Path(tmp)/'loop.wav'
  with wave.open(str(wav),'wb')as f:
   f.setparams((1,2,rate,0,'NONE','not compressed'));f.writeframes(b''.join(struct.pack('<h',round(max(-1,min(1,v))*32767))for v in samples))
  subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4',str(out)],check=True)
 events[name+'_loop']={'subtitle':'subtitles.techmod.'+name+'_loop','sounds':[{'name':'techmod:machines/'+name,'attenuation_distance':6}]}
(root/'sounds.json').write_text(json.dumps(events,indent=2)+'\n')
