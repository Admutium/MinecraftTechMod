package dev.admutium.techmod.client.sound;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import org.slf4j.LoggerFactory;
public final class MachineAudioConfig {
 private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("kontur-client.json");
 public static double volume=.35;
 public static void load(){try{if(Files.exists(FILE)){var root=JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();if(root.has("machineVolume")){double value=root.get("machineVolume").getAsDouble();volume=Double.isFinite(value)?Math.clamp(value,0,1):.35;}}}catch(Exception e){LoggerFactory.getLogger("techmod").warn("Could not read Kontur audio settings; using defaults",e);}}
 public static void save(){try{Files.createDirectories(FILE.getParent());var o=new JsonObject();o.addProperty("machineVolume",volume);Files.writeString(FILE,new GsonBuilder().setPrettyPrinting().create().toJson(o)+"\n");}catch(Exception e){LoggerFactory.getLogger("techmod").warn("Could not save Kontur audio settings",e);}}
 private MachineAudioConfig(){}
}
