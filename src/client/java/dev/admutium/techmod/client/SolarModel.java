package dev.admutium.techmod.client;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
/** Both casing and floor wire are baked into the chunk, never rebuilt each frame. */
public record SolarModel(BlockStateModel casing,BlockStateModel cable) implements BlockStateModel {
 @Override public void emitQuads(QuadEmitter e,BlockAndTintGetter w,BlockPos p,BlockState s,RandomSource r,Predicate<@Nullable Direction> c){casing.emitQuads(e,w,p,s,r,c);cable.emitQuads(e,w,p,s,r,c);}
 @Override public void collectParts(RandomSource r,List<BlockStateModelPart> parts){}
 @Override public Material.Baked particleMaterial(){return casing.particleMaterial();}
 @Override public int materialFlags(){return casing.materialFlags()|cable.materialFlags();}
 public record Unbaked(Identifier model) implements CustomUnbakedBlockStateModel {
  public static final MapCodec<Unbaked> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model)).apply(i,Unbaked::new));
  @Override public MapCodec<? extends CustomUnbakedBlockStateModel> codec(){return CODEC;}
  @Override public void resolveDependencies(Resolver r){r.markDependency(model);}
  @Override public BlockStateModel bake(ModelBaker b){return new SolarModel(new SingleVariant(new Variant(model).bake(b)),new CableModel.Unbaked().bake(b));}
 }
}
