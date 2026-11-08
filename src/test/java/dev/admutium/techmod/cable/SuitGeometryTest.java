package dev.admutium.techmod.cable;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
public final class SuitGeometryTest {
 private JsonObject geometry() throws Exception{return JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/techmod/suit/prototype.json"))).getAsJsonObject();}
 @Test void everyBoneHasAnExistingParentAndEveryCubeHasValidBounds() throws Exception{
  var d=geometry();var seen=new HashSet<String>();int count=0;
  for(var value:d.getAsJsonArray("parts")){var p=value.getAsJsonObject();String name=p.get("name").getAsString();assertTrue(seen.add(name),"Duplicate bone "+name);if(!p.get("parent").isJsonNull())assertTrue(seen.contains(p.get("parent").getAsString()),"Parent must be defined first");
   for(var c1:p.getAsJsonArray("cubes")){var c=c1.getAsJsonObject();String pass=c.get("pass").getAsString();assertTrue(Set.of("base","signal","field").contains(pass));for(int i=0;i<3;i++){float size=c.getAsJsonArray("to").get(i).getAsFloat()-c.getAsJsonArray("from").get(i).getAsFloat();assertTrue(size>=0&&Float.isFinite(size));if(i!=2||!pass.equals("field"))assertTrue(size>0);}count++;}
  }assertTrue(count<300,"Preview geometry must stay bounded");assertTrue(seen.containsAll(Set.of("head","body","left_arm","right_arm","left_leg","right_leg","rainbow","gravity")));
 }
 @Test void allUnwrappedTexturesFitInAtlasAndUseIntegerUvOrigins() throws Exception{
  var d=geometry();float width=d.get("texture_width").getAsFloat(),height=d.get("texture_height").getAsFloat();
  for(var value:d.getAsJsonArray("parts"))for(var entry:value.getAsJsonObject().getAsJsonArray("cubes")){var c=entry.getAsJsonObject();float scale=c.get("uv_scale").getAsFloat(),u=c.getAsJsonArray("uv").get(0).getAsFloat(),v=c.getAsJsonArray("uv").get(1).getAsFloat();assertEquals(Math.round(u),u,.0001);assertEquals(Math.round(v),v,.0001);float w=c.getAsJsonArray("to").get(0).getAsFloat()-c.getAsJsonArray("from").get(0).getAsFloat(),h=c.getAsJsonArray("to").get(1).getAsFloat()-c.getAsJsonArray("from").get(1).getAsFloat(),z=c.getAsJsonArray("to").get(2).getAsFloat()-c.getAsJsonArray("from").get(2).getAsFloat();assertTrue(u>=0&&v>=0&&scale>0);assertTrue((u+2*w+2*z)*scale<=width+.01);assertTrue((v+h+z)*scale<=height+.01);}
 }
}
