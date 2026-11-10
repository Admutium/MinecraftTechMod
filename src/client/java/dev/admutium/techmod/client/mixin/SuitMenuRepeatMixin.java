package dev.admutium.techmod.client.mixin;
import dev.admutium.techmod.client.suit.SuitClient;import dev.admutium.techmod.suit.Suits;import net.minecraft.client.*;import net.minecraft.client.input.KeyEvent;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Native repeat is not a fresh command. Handle it before both screen and gameplay dispatch. */
@Mixin(KeyboardHandler.class) public abstract class SuitMenuRepeatMixin {
 @Inject(method="keyPress",at=@At("HEAD"),cancellable=true) private void repeat(long window,int action,KeyEvent event,CallbackInfo ci){var c=Minecraft.getInstance();if(action==2&&c.player!=null&&!Suits.worn(c.player).isEmpty()&&SuitClient.menuKey!=null&&SuitClient.menuKey.matches(event))ci.cancel();}
}
