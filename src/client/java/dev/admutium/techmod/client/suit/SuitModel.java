package dev.admutium.techmod.client.suit;
import com.google.gson.*;
import dev.admutium.techmod.TechMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import java.io.*;
import java.util.*;
/** Resource-pack editable geometry, baked once per renderer reload, never rebuilt per frame. */
public final class SuitModel extends Model<SuitPose> {
 public enum Pass {BASE("base"),SIGNAL("signal"),FIELD("field");final String id;Pass(String id){this.id=id;}}
 public static final net.minecraft.resources.Identifier TEXTURE=TechMod.id("textures/entity/kontur_suit.png");
 private final ModelPart head,body,rightArm,leftArm,rightLeg,leftLeg,rainbow,gravity;
 public static JsonObject definition(){try(var reader=Minecraft.getInstance().getResourceManager().getResourceOrThrow(TechMod.id("suit/prototype.json")).openAsReader()){return JsonParser.parseReader(reader).getAsJsonObject();}catch(IOException e){throw new IllegalStateException("Cannot load Kontur suit geometry",e);}}
 public SuitModel(JsonObject definition,boolean slim,Pass pass){super(bake(definition,slim,pass),RenderTypes::entityCutout);head=root.getChild("head");body=root.getChild("body");rightArm=root.getChild("right_arm");leftArm=root.getChild("left_arm");rightLeg=root.getChild("right_leg");leftLeg=root.getChild("left_leg");rainbow=body.getChild("rainbow");gravity=body.getChild("gravity");}
 private static float f(JsonArray a,int i){return a.get(i).getAsFloat();}
 private static ModelPart bake(JsonObject d,boolean slim,Pass pass){
  Map<String,JsonObject> definitions=new LinkedHashMap<>();Map<String,List<String>> children=new HashMap<>();
  for(var entry:d.getAsJsonArray("parts")){var p=entry.getAsJsonObject();String name=p.get("name").getAsString();definitions.put(name,p);String parent=p.get("parent").isJsonNull()?"":p.get("parent").getAsString();children.computeIfAbsent(parent,k->new ArrayList<>()).add(name);}
  Map<String,ModelPart> roots=new LinkedHashMap<>();for(String name:children.get(""))roots.put(name,bone(name,definitions,children,d,slim,pass));return new ModelPart(List.of(),roots);
 }
 private static ModelPart bone(String name,Map<String,JsonObject> definitions,Map<String,List<String>> children,JsonObject d,boolean slim,Pass pass){
  var p=definitions.get(name);var cubes=new ArrayList<ModelPart.Cube>();boolean arm=slim&&(name.equals("right_arm")||name.equals("left_arm"));
  for(var entry:p.getAsJsonArray("cubes")){var c=entry.getAsJsonObject();if(!c.get("pass").getAsString().equals(pass.id))continue;
   var a=c.getAsJsonArray("from");var b=c.getAsJsonArray("to");var uv=c.getAsJsonArray("uv");float scale=c.get("uv_scale").getAsFloat(),x=f(a,0),x2=f(b,0);
   var faces=java.util.EnumSet.allOf(net.minecraft.core.Direction.class);if(c.has("faces")){faces.clear();for(var face:c.getAsJsonArray("faces"))faces.add(net.minecraft.core.Direction.valueOf(face.getAsString().toUpperCase(java.util.Locale.ROOT)));}
   cubes.add(new ModelPart.Cube(Math.round(f(uv,0)),Math.round(f(uv,1)),x,f(a,1),f(a,2),x2-x,f(b,1)-f(a,1),f(b,2)-f(a,2),0,0,0,false,d.get("texture_width").getAsFloat()/scale,d.get("texture_height").getAsFloat()/scale,faces));
  }
  Map<String,ModelPart> nested=new LinkedHashMap<>();for(String child:children.getOrDefault(name,List.of()))nested.put(child,bone(child,definitions,children,d,slim,pass));ModelPart result;
  if(arm){
   // Scale a child shell, keeping the original UV dimensions and every nested shoulder detail aligned.
   var shell=new ModelPart(cubes,nested);var fit=new PartPose(name.equals("left_arm")?-.25f:.25f,0,0,0,0,0,.75f,1,1);shell.setInitialPose(fit);shell.loadPose(fit);result=new ModelPart(List.of(),Map.of("shell",shell));
  }else result=new ModelPart(cubes,nested);
  var a=p.getAsJsonArray("pivot");var r=p.getAsJsonArray("rotation");var pose=PartPose.offsetAndRotation(f(a,0),f(a,1),f(a,2),f(r,0),f(r,1),f(r,2));result.setInitialPose(pose);result.loadPose(pose);return result;
 }
 @Override public void setupAnim(SuitPose p){
  resetPose();head.loadPose(p.head());body.loadPose(p.body());rightArm.loadPose(p.rightArm());leftArm.loadPose(p.leftArm());rightLeg.loadPose(p.rightLeg());leftLeg.loadPose(p.leftLeg());
  boolean full=true;head.visible=body.visible=rightLeg.visible=leftLeg.visible=rightArm.visible=leftArm.visible=true;
  rainbow.visible=full&&p.wingMode()==1;gravity.visible=full&&p.wingMode()==2;
 }
}
