package net.fabricmc.fabric.api.recipe.v1;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.world.World;

/**
 * General-purpose Fabric-provided extensions for {@link ServerRecipeManager} class.
 */
public interface FabricServerRecipeManager {
	/**
	 * Creates a stream of all recipe entries of the given {@code type} that match the
	 * given {@code input} and {@code world}.
	 *
	 * <p>If {@code input.isEmpty()} returns true, the returned stream will be always empty.
	 *
	 * @return the stream of matching recipes
	 */
	default <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
		return Stream.of();
	}

	/**
	 * @return the collection of recipe entries of given type
	 */
	default <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
		return List.of();
	}
}
