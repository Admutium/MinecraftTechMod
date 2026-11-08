package dev.admutium.techmod.client.suit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
/** Two batches for the suit; the third batch is submitted only for deployed field wings. */
public final class SuitModels {
 private final SuitModel[] base=new SuitModel[2],signal=new SuitModel[2],field=new SuitModel[2];
 public SuitModels(){var definition=SuitModel.definition();for(int i=0;i<2;i++){base[i]=new SuitModel(definition,i==1,SuitModel.Pass.BASE);signal[i]=new SuitModel(definition,i==1,SuitModel.Pass.SIGNAL);field[i]=new SuitModel(definition,i==1,SuitModel.Pass.FIELD);}}
 public void submit(SuitPose state,boolean slim,PoseStack poses,SubmitNodeCollector out,int light){int i=slim?1:0;
  var type=RenderTypes.entityCutout(SuitModel.TEXTURE);
  out.submitModel(base[i],state,poses,type,light,OverlayTexture.NO_OVERLAY,0xffffffff,null,0);
  out.submitModel(signal[i],state,poses,RenderTypes.eyes(SuitModel.TEXTURE),0x00f000f0,OverlayTexture.NO_OVERLAY,SuitPreview.accent(),null,0);
  if(state.wingMode()>0)out.submitModel(field[i],state,poses,state.wingMode()==2?RenderTypes.eyes(SuitModel.TEXTURE):RenderTypes.entityTranslucent(SuitModel.TEXTURE),0x00f000f0,OverlayTexture.NO_OVERLAY,0xffffffff,null,0);
 }
}
