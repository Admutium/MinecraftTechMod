package dev.admutium.techmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.*;

/** Minecraft 26.3 uses SDL scancodes; old GLFW integer defaults do not represent those keys. */
public final class KonturKeys {
 private record Legacy(KeyMapping mapping,int oldDefault,int correctDefault) {}
 private static final List<Legacy> LEGACY=new ArrayList<>();
 public static KeyMapping register(String name,int code,int oldCode,KeyMapping.Category category){var mapping=KeyMappingHelper.registerKeyMapping(new KeyMapping(name,code,category));LEGACY.add(new Legacy(mapping,oldCode,code));return mapping;}
 public static void init(){ClientLifecycleEvents.CLIENT_STARTED.register(KonturKeys::migrateLegacyBindings);}
 public static void migrateLegacyBindings(Minecraft client){boolean changed=false;for(var entry:LEGACY){var bound=KeyMappingHelper.getBoundKeyOf(entry.mapping);if(bound.getType()==InputConstants.Type.KEYBOARD&&bound.getValue()==entry.oldDefault){entry.mapping.setKey(InputConstants.Type.KEYBOARD.getOrCreate(entry.correctDefault));changed=true;}}if(changed){KeyMapping.resetMapping();client.options.save();}}
}
