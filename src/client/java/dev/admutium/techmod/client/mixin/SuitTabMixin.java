package dev.admutium.techmod.client.mixin;
import dev.admutium.techmod.client.suit.SuitClient;import dev.admutium.techmod.suit.Suits;import net.minecraft.client.*;import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
@Mixin(Hud.class) public abstract class SuitTabMixin {
 @Redirect(method="extractTabList",at=@At(value="INVOKE",target="Lnet/minecraft/client/KeyMapping;isDown()Z"))private boolean tab(KeyMapping key){var p=Minecraft.getInstance().player;return p!=null&&!Suits.worn(p).isEmpty()?SuitClient.playerList!=null&&SuitClient.playerList.isDown():key.isDown();}
}
