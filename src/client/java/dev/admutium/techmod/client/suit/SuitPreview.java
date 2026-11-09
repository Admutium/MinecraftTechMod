package dev.admutium.techmod.client.suit;
import dev.admutium.techmod.TechMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
/** Client-only visual fitting: never equips items, writes player data or grants gameplay abilities. */
public final class SuitPreview {
 private static boolean enabled;private static int wings,color;
 private static final int[] COLORS={0xffff9c26,0xff35d9dd,0xffa882ff,0xffedf2ff};
 public static void init(){
  LivingEntityRenderLayerRegistrationCallback.EVENT.register((type,renderer,helper,context)->{if(renderer instanceof AvatarRenderer<?> avatar)helper.register(new SuitLayer(avatar));});
  var key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.techmod.suit_preview",75,KeyMapping.Category.MISC));
  ClientTickEvents.END_CLIENT_TICK.register(c->{while(key.consumeClick())if(c.player!=null&&c.gui.screen()==null){if(!dev.admutium.techmod.suit.Suits.worn(c.player).isEmpty())c.setScreenAndShow(new SuitRadialScreen());else{enabled=true;c.setScreenAndShow(new SuitPreviewScreen());}}});
  ClientPlayConnectionEvents.DISCONNECT.register((handler,c)->{enabled=false;wings=0;color=0;});
 }
 public static boolean shownFor(int id){var p=Minecraft.getInstance().player;return enabled&&p!=null&&p.getId()==id;}
 public static boolean enabled(){return enabled;}public static void setEnabled(boolean value){enabled=value;}
 public static int wingMode(){return wings;}public static void setWingMode(int value){wings=Math.clamp(value,0,2);}
 public static int accent(){return COLORS[color];}public static void cycleColor(){color=(color+1)%COLORS.length;}
 public static int colorIndex(){return color;}
 private SuitPreview(){}
}
