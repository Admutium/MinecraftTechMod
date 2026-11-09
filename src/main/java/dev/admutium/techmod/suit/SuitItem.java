package dev.admutium.techmod.suit;
import dev.admutium.techmod.energy.Energy;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
public final class SuitItem extends Item {
 public SuitItem(Properties p){super(p.stacksTo(1).fireResistant().rarity(Rarity.EPIC));}
 @Override public InteractionResult use(Level w,Player p,InteractionHand hand){if(!w.isClientSide())p.sendOverlayMessage(Component.translatable("suit.techmod.station_required"));return InteractionResult.FAIL;}
 @Override public void appendHoverText(ItemStack stack,Item.TooltipContext c,net.minecraft.world.item.component.TooltipDisplay d,java.util.function.Consumer<Component> out,TooltipFlag f){var s=stack.getOrDefault(Suits.DATA,SuitData.EMPTY);out.accept(Component.literal(Energy.format(s.energy())+" / "+Energy.format(s.capacity())+" ТЕ"));out.accept(Component.translatable("suit.techmod.upgrades_count",java.util.Arrays.stream(SuitUpgrade.values()).mapToInt(s::level).sum()));if(s.sealed())out.accept(Component.translatable("suit.techmod.sealed"));}
}
