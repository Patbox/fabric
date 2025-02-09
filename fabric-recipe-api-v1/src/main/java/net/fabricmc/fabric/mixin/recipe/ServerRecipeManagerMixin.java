package net.fabricmc.fabric.mixin.recipe;

import java.util.Collection;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.recipe.v1.FabricServerRecipeManager;

@Mixin(ServerRecipeManager.class)
public abstract class ServerRecipeManagerMixin implements FabricServerRecipeManager {
	@Shadow
	private PreparedRecipes preparedRecipes;

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
		return this.preparedRecipes.getAll(type);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
		return this.preparedRecipes.find(type, input, world);
	}
}
