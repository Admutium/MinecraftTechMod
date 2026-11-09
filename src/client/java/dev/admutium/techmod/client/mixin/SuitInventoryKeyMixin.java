package dev.admutium.techmod.client.mixin;
import dev.admutium.techmod.client.suit.SuitClient;import dev.admutium.techmod.suit.Suits;import net.minecraft.client.*;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
/** Intercept before vanilla opens the creative inventory and rewrites its equipment mirror. */
@Mixin(Minecraft.class) public abstract class SuitInventoryKeyMixin {
 @Redirect(method="handleKeybinds",at=@At(value="INVOKE",target="Lnet/minecraft/client/KeyMapping;consumeClick()Z"))private boolean inventory(KeyMapping key){boolean click=key.consumeClick();var c=(Minecraft)(Object)this;if(click&&key==c.options.keyInventory&&c.player!=null&&!Suits.worn(c.player).isEmpty()){SuitClient.action("inventory","");return false;}return click;}
}
